package code.uz.bankcard.dto.transaction;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransactionCreateDTO {
    @NotNull(message = "Card Id required")
    private UUID fromCardId;
    @NotNull(message = "Card Id required")
    private UUID toCardId;
    @NotNull(message = "Amount required")
    @DecimalMin(value = "0.1", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
