package antifraud.repository;

import antifraud.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.cardNumber = :cardNumber AND t.date >= :startDate AND t.date < :endDate")
    List<Transaction> findAllTransactionInLastHour(
            @Param("cardNumber") String cardNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Transaction> findAllByCardNumberOrderById(String cardNumber);
    boolean existsByCardNumber(String cardNumber);


    List<Transaction> findAllByCardNumberAndDateGreaterThanEqual(String cardNumber, LocalDateTime date);

    List<Transaction> findAllByCardNumberAndDateBetween(String cardNumber, LocalDateTime startDate, LocalDateTime endDate);

}
