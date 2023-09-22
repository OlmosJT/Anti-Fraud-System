package antifraud.service;

import antifraud.exception.ConflictException;
import antifraud.exception.OperationException;
import antifraud.model.CreditCard;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public interface CreditCardService {
    CreditCard addToStolenCards(String cardNumber) throws OperationException, ConflictException;
    Boolean validateCardNumber(String number);

    void deleteFromStolenCards(String cardNumber) throws OperationException, EntityNotFoundException;

    List<CreditCard> getStolenCards();

    Boolean hasCardStolen(String cardNumber);
}
