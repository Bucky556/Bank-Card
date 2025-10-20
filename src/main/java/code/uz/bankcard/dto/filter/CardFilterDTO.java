package code.uz.bankcard.dto.filter;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardFilterDTO {
    @NotBlank(message = "Card Number required")
    private String cardNumber;
}
