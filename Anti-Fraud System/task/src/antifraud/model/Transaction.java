package antifraud.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "transcations")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long amount;
    private String ip;
    @Column(name = "number")
    private String cardNumber;
    @ManyToOne(targetEntity = Region.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Region region;
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    private String info;

    @Enumerated(EnumType.STRING)
    private TransactionStatus feedback;

}
