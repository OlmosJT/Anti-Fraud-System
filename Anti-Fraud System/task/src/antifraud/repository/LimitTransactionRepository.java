package antifraud.repository;

import antifraud.model.TransactionLimit;
import antifraud.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LimitTransactionRepository extends JpaRepository<TransactionLimit, TransactionStatus> {
    Optional<TransactionLimit> findByName(TransactionStatus status);
}
