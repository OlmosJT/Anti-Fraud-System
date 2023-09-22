package antifraud.service.impl;

import antifraud.dto.TransactionDTO;
import antifraud.exception.ConflictException;
import antifraud.exception.OperationException;
import antifraud.exception.UnprocessableEntityException;
import antifraud.model.*;
import antifraud.repository.LimitTransactionRepository;
import antifraud.repository.RegionRepository;
import antifraud.repository.TransactionRepository;
import antifraud.service.CreditCardService;
import antifraud.service.IpService;
import antifraud.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final CreditCardService cardService;
    private final IpService ipService;
    private final TransactionRepository transactionRepository;
    private final LimitTransactionRepository limitRepository;
    private final RegionRepository regionRepository;

    public TransactionServiceImpl(CreditCardService cardService, IpService ipService, TransactionRepository transactionRepository, LimitTransactionRepository limitRepository, RegionRepository regionRepository) {
        this.cardService = cardService;
        this.ipService = ipService;
        this.transactionRepository = transactionRepository;
        this.limitRepository = limitRepository;
        this.regionRepository = regionRepository;
    }

    @Transactional
    @Override
    public Map<String, String> processTransaction(
            Long amount,
            String ip,
            String cardNumber,
            String regionCode,
            LocalDateTime date
    ) throws OperationException, EntityNotFoundException {
        Set<InfoStatus> infoSet = new HashSet<>();
        TransactionStatus result = TransactionStatus.ALLOWED;

        if (cardService.hasCardStolen(cardNumber)) {
            result = TransactionStatus.PROHIBITED;
            infoSet.add(InfoStatus.CARD_NUMBER);
        }

        if (ipService.isSuspiciousIp(ip)) {
            result = TransactionStatus.PROHIBITED;
            infoSet.add(InfoStatus.IP);
        }
        List<Transaction> transactions = transactionRepository.findAllTransactionInLastHour(cardNumber, date.minusHours(1), date);
        Set<String> processedRegionCodes = transactions.stream().map(t -> t.getRegion().getCode()).collect(Collectors.toSet());
        Set<String> processedIpCodes = transactions.stream().map(Transaction::getIp).collect(Collectors.toSet());

        if (processedRegionCodes.size() > 2) {
            result = TransactionStatus.PROHIBITED;
            infoSet.add(InfoStatus.REGION_CORRELATION);
        } else if (processedRegionCodes.size() == 2 && !processedRegionCodes.contains(regionCode)) {
            if (!result.equals(TransactionStatus.PROHIBITED)) {
                result = TransactionStatus.MANUAL_PROCESSING;
            }
            infoSet.add(InfoStatus.REGION_CORRELATION);
        }

        if (processedIpCodes.size() > 2 /*&& !processedIpCodes.contains(ip)*/) {
            result = TransactionStatus.PROHIBITED;
            infoSet.add(InfoStatus.Ip_CORRELATION);
        } else if (processedIpCodes.size() == 2 && !processedIpCodes.contains(ip)) {
            if (!result.equals(TransactionStatus.PROHIBITED))
                result = TransactionStatus.MANUAL_PROCESSING;
            infoSet.add(InfoStatus.Ip_CORRELATION);
        }

        AtomicReference<Double> allowedLimit = new AtomicReference<>((double) 200);
        AtomicReference<Double> manualLimit = new AtomicReference<>((double) 1500);

        limitRepository.findByName(TransactionStatus.ALLOWED).ifPresent(entity -> {
            allowedLimit.set(entity.getLimitAmount());
        });

        limitRepository.findByName(TransactionStatus.MANUAL_PROCESSING).ifPresent(entity -> {
            manualLimit.set(entity.getLimitAmount());
        });

        if (amount > manualLimit.get()) {
            result = TransactionStatus.PROHIBITED;
            infoSet.add(InfoStatus.AMOUNT);
        } else if (amount > allowedLimit.get() && !result.equals(TransactionStatus.PROHIBITED)) {
            result = TransactionStatus.MANUAL_PROCESSING;
            infoSet.add(InfoStatus.AMOUNT);
        } else if (result.equals(TransactionStatus.ALLOWED)) {
            infoSet.add(InfoStatus.NONE);
        }


        Region region = regionRepository.findByCode(regionCode).orElseThrow(() -> new EntityNotFoundException("Region not found!"));
        String joinedString = infoSet.stream().map(InfoStatus::getValue).sorted().collect(Collectors.joining(", "));
        Transaction transaction = Transaction.builder()
                .amount(amount)
                .ip(ip)
                .cardNumber(cardNumber)
                .region(region)
                .date(date)
                .status(result)
                .info(joinedString)
                .build();

        transactionRepository.save(transaction);

        return Map.of("result", result.name(), "info", joinedString);
    }

    @Override
    public List<CreditCard> getStolenCards() {
        return cardService.getStolenCards();
    }

    @Override
    public void deleteFromStolenCards(String number) {
        cardService.deleteFromStolenCards(number);
    }

    @Override
    public CreditCard addToStolenCards(String number) {
        return cardService.addToStolenCards(number);
    }

    @Override
    public void removeIPFromBlackList(String ip) {
        ipService.removeIPFromBlackList(ip);
    }

    @Override
    public SusIpAddress addIPToBlackList(String ip) {
        return ipService.addIPToBlackList(ip);
    }

    @Override
    public List<SusIpAddress> getBlackListedIPs() {
        return ipService.getBlackListedIPs();
    }

    @Override
    public boolean validateCardNumber(String number) {
        return cardService.validateCardNumber(number);
    }

    @Override
    public boolean validateIP(String ip) {
        return ipService.validateIP(ip);
    }

    @Override
    public TransactionDTO processFeedBack(
            long transactionId,
            TransactionStatus feedback
    ) throws EntityNotFoundException, ConflictException, UnprocessableEntityException {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction not found!"));

        if (transaction.getFeedback() != null) {
            throw new ConflictException("Feedback has already submitted to transaction");
        }

        if (transaction.getStatus().equals(feedback)) {
            throw new UnprocessableEntityException("Transaction result and feedback has same value!");
        }

        switch (feedback) {
            case ALLOWED -> {
                if (transaction.getStatus().equals(TransactionStatus.PROHIBITED)) {
                    increaseLimit(transaction, TransactionStatus.MANUAL_PROCESSING, feedback);
                    increaseLimit(transaction, TransactionStatus.ALLOWED, feedback);
                } else if (transaction.getStatus().equals(TransactionStatus.MANUAL_PROCESSING)) {
                    increaseLimit(transaction, TransactionStatus.ALLOWED, feedback);
                }
            }
            case MANUAL_PROCESSING -> {
                switch (transaction.getStatus()) {
                    case ALLOWED -> {
                        decreaseLimit(transaction, TransactionStatus.ALLOWED, feedback);
                    }
                    case PROHIBITED -> {
                        increaseLimit(transaction, TransactionStatus.MANUAL_PROCESSING, feedback);
                    }

                }
            }
            case PROHIBITED -> {
                if (transaction.getStatus().equals(TransactionStatus.ALLOWED)) {
                    decreaseLimit(transaction, TransactionStatus.ALLOWED, feedback);
                    decreaseLimit(transaction, TransactionStatus.MANUAL_PROCESSING, feedback);
                } else if (transaction.getStatus().equals(TransactionStatus.MANUAL_PROCESSING)) {
                    decreaseLimit(transaction, TransactionStatus.MANUAL_PROCESSING, feedback);
                }
            }
        }

        return new TransactionDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getIp(),
                transaction.getCardNumber(),
                transaction.getRegion().getCode(),
                transaction.getDate(),
                transaction.getStatus(),
                (transaction.getFeedback() == null) ? "": transaction.getFeedback().name()
        );
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll(Sort.by("id")).stream().map(transaction -> new TransactionDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getIp(),
                transaction.getCardNumber(),
                transaction.getRegion().getCode(),
                transaction.getDate(),
                transaction.getStatus(),
                (transaction.getFeedback() == null) ? "": transaction.getFeedback().name()
        )).collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getAllTransactionsByCard(String cardNumber) throws EntityNotFoundException {
        if (!transactionRepository.existsByCardNumber(cardNumber)) {
            throw new EntityNotFoundException("Transactions not found for credit-card: " + cardNumber);
        }
        return transactionRepository.findAllByCardNumberOrderById(cardNumber)
                .stream().map(transaction -> new TransactionDTO(
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getIp(),
                        transaction.getCardNumber(),
                        transaction.getRegion().getCode(),
                        transaction.getDate(),
                        transaction.getStatus(),
                        (transaction.getFeedback() == null) ? "": transaction.getFeedback().name()
                )).collect(Collectors.toList());
    }


    private void increaseLimit(Transaction transaction, TransactionStatus limit, TransactionStatus feedback) {
        limitRepository.findByName(limit).ifPresent(it -> {

            var entity = it.maxUp(transaction.getAmount());
            limitRepository.save(entity);

            transaction.setFeedback(feedback);
            transactionRepository.save(transaction);
        });
    }

    private void decreaseLimit(Transaction transaction, TransactionStatus limit, TransactionStatus feedback) {
        limitRepository.findByName(limit).ifPresent(it -> {

            var entity = it.maxDown(transaction.getAmount());
            limitRepository.save(entity);

            transaction.setFeedback(feedback);
            transactionRepository.save(transaction);
        });
    }

}
