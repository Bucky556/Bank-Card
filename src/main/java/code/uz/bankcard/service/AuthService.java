package code.uz.bankcard.service;

import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.auth.AuthDTO;
import code.uz.bankcard.dto.auth.ProfileResponseDTO;
import code.uz.bankcard.dto.auth.RegisterDTO;
import jakarta.validation.Valid;


public interface AuthService {
    AppResponse<String> register(@Valid RegisterDTO dto);

    ProfileResponseDTO login(@Valid AuthDTO dto);
}
