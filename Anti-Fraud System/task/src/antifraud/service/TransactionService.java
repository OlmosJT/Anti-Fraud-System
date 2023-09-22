package antifraud.service;

import antifraud.dto.TransactionDTO;
import antifraud.model.CreditCard;
import antifraud.model.SusIpAddress;
import antifraud.model.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    Map<String, String> processTransaction(Long amount, String ip, String number, String region, LocalDateTime date);

    List<CreditCard> getStolenCards();

    void deleteFromStolenCards(String number);

    CreditCard addToStolenCards(String number);

    void removeIPFromBlackList(String ip);

    SusIpAddress addIPToBlackList(String ip);

    List<SusIpAddress> getBlackListedIPs();

    boolean validateCardNumber(String number);

    boolean validateIP(String ip);

    TransactionDTO processFeedBack(long transactionId, TransactionStatus feedback);

    List<TransactionDTO> getAllTransactions();

    List<TransactionDTO> getAllTransactionsByCard(String cardNumber);
}
