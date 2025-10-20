package code.uz.bankcard.dto;

import code.uz.bankcard.enums.GeneralStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileStatusDTO {
    private GeneralStatus status;
}
