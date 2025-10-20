package code.uz.bankcard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CardBalanceDTO {
    private String cardNumber;
    private BigDecimal balance;
}
