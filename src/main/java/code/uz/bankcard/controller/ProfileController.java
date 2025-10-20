package code.uz.bankcard.controller;

import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.ProfileStatusDTO;
import code.uz.bankcard.dto.auth.ProfileResponseDTO;
import code.uz.bankcard.service.Impl.ProfileServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@EnableMethodSecurity
@Slf4j
@Tag(name = "Profile Controller", description = "Controlling APIs by admins")
public class ProfileController {
    private final ProfileServiceImpl profileService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get All Profiles",
            description = "Admin can retrieve the list of all profiles"
    )
    public ResponseEntity<List<ProfileResponseDTO>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getProfiles());
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get Profile By ID",
            description = "Admin can retrieve a profile by its ID"
    )
    public ResponseEntity<ProfileResponseDTO> getProfileById(@PathVariable("profileId") UUID profileId) {
        return ResponseEntity.ok(profileService.getProfileById(profileId));
    }

    @PutMapping("/status/{profileId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Change status of profiles",
            description = "This API allows admin to change status of profiles. (active, block)"
    )
    public ResponseEntity<AppResponse<String>> changeStatus(@PathVariable("profileId") UUID profileId,
                                                            @RequestBody ProfileStatusDTO dto) {
        return ResponseEntity.ok(profileService.changeStatus(profileId, dto.getStatus()));
    }

    @DeleteMapping("/delete/{profileId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Delete Profile",
            description = "This API allows admin to profile"
    )
    public ResponseEntity<AppResponse<String>> deleteProfile(@PathVariable("profileId") UUID profileId) {
        return ResponseEntity.ok(profileService.delete(profileId));
    }
}
