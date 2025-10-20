package code.uz.bankcard.entity;

import code.uz.bankcard.enums.CardStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card")
@Getter
@Setter
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "card_number")
    private String cardNumber;
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "exp_date")
    private LocalDateTime expiryDate;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CardStatus status;
    @Column(name = "balance")
    private BigDecimal balance;
    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;
}
