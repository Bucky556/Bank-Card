package code.uz.bankcard.dto.transaction;

import code.uz.bankcard.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponseDTO {
    private Integer id;
    private String fromCardNumber;
    private String toCardNumber;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime sentDate;
}
