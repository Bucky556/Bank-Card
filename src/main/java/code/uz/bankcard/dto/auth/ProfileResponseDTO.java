package code.uz.bankcard.dto.auth;


import code.uz.bankcard.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponseDTO {
    private UUID id;
    private String name;
    private String username;
    private List<Role> roles;
    private String accessToken;
    private LocalDateTime createdDate;
}
