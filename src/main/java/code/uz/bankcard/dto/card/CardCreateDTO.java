package code.uz.bankcard.dto.card;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CardCreateDTO {
    @NotNull(message = "Profile ID required")
    private UUID profileId;
    @NotBlank(message = "Card Number required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be exactly 16 digits")
    private String cardNumber;
    @NotNull(message = "InitialBalance required")
    private BigDecimal initialBalance;
}
