package code.uz.bankcard.dto.card;


import code.uz.bankcard.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardResponseDTO {
    private UUID id;
    private String maskedNumber;
    private String ownerName;
    private LocalDateTime expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private String note;
}
