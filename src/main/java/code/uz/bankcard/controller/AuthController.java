package code.uz.bankcard.controller;

import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.auth.AuthDTO;
import code.uz.bankcard.dto.auth.ProfileResponseDTO;
import code.uz.bankcard.dto.auth.RegisterDTO;
import code.uz.bankcard.service.Impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth Controller", description = "Controller for authorization and authentication")
public class AuthController {
    private final AuthServiceImpl authService;

    @PostMapping("/register")
    @Operation(summary = "Profile registration", description = "Api for registering")
    public ResponseEntity<AppResponse<String>> register(@RequestBody @Valid RegisterDTO dto) {
        log.info("Registering user: {}", dto.getUsername());
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Profile login", description = "Api for login")
    public ResponseEntity<ProfileResponseDTO> login(@RequestBody @Valid AuthDTO dto) {
        log.info("Login: {}", dto.getUsername());
        return ResponseEntity.ok(authService.login(dto));
    }
}
