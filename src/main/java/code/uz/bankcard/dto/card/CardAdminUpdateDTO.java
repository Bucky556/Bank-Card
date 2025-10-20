package code.uz.bankcard.dto.card;


import code.uz.bankcard.enums.CardStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CardAdminUpdateDTO {
    private CardStatus status;
    private LocalDateTime expiryDate;
    @PositiveOrZero(message = "Balance cannot be negative")
    private BigDecimal balance;
}
