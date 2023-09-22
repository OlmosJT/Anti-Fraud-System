package antifraud.repository;

import antifraud.model.CreditCard;
import antifraud.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<CreditCard, Long> {
    Optional<CreditCard> findByNumber(String cardNumber);
    Boolean existsByNumber(String cardNumber);

    void deleteByNumber(String cardNumber);

}
