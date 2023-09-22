package antifraud.service.impl;

import antifraud.exception.ConflictException;
import antifraud.exception.OperationException;
import antifraud.model.CreditCard;
import antifraud.repository.CardRepository;
import antifraud.service.CreditCardService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditCardServiceImpl implements CreditCardService {

    private final CardRepository cardRepository;

    public CreditCardServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    @Override
    public CreditCard addToStolenCards(String cardNumber) throws OperationException, ConflictException {
        if(!isValidCardNumber(cardNumber)) { throw new OperationException("Wrong card number format!"); }
        if(cardRepository.existsByNumber(cardNumber)) { throw new ConflictException("Card has already in stolen list!"); }
        var card = new CreditCard();
        card.setNumber(cardNumber);
        card = cardRepository.save(card);
        return card;
    }

    private Boolean isValidCardNumber(String cardNumber) {
        if(cardNumber.length() != 16) return false;
        if(!cardNumber.matches("^[\\d]{16}$")) return false;

        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10) == 0;
    }

    @Transactional
    @Override
    public void deleteFromStolenCards(String cardNumber) throws OperationException, EntityNotFoundException {
        if(!isValidCardNumber(cardNumber)) { throw new OperationException("Wrong card number format!"); }
        if(!cardRepository.existsByNumber(cardNumber)) { throw new EntityNotFoundException("Card %s not found.".formatted(cardNumber)); }

        cardRepository.deleteByNumber(cardNumber);
    }

    @Transactional
    @Override
    public List<CreditCard> getStolenCards() {
        return cardRepository.findAll(Sort.by(Sort.Order.asc("id")));
    }

    @Override
    public Boolean hasCardStolen(String cardNumber) {
        return cardRepository.existsByNumber(cardNumber);
    }

    @Override
    public Boolean validateCardNumber(String number) {
        return isValidCardNumber(number);
    }
}
