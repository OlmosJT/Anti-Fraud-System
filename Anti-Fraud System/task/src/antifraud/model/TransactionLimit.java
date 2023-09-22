package antifraud.model;

import jakarta.persistence.*;
import lombok.*;

import static java.lang.Math.ceil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name="limits_transaction")
public class TransactionLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionStatus name;
    private double limitAmount;

    public TransactionLimit maxUp(long transactionValue) {
        limitAmount = ceil(0.8 * this.limitAmount + 0.2 * transactionValue);
        return this;
    }

    public TransactionLimit maxDown(long transactionValue) {
        limitAmount = ceil(0.8 * this.limitAmount - 0.2 * transactionValue);
        return this;
    }
}
