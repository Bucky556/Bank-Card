package code.uz.bankcard.entity;


import code.uz.bankcard.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@Getter
@Setter
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card")
    private CardEntity fromCardId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card")
    private CardEntity toCardId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "sent_date")
    private LocalDateTime sentDate;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    @Column(name = "visible")
    private Boolean visible = true;
}
