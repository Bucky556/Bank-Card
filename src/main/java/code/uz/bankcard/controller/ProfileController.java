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

/**
 * Controller for administrative management of user profiles.
 * <p>
 * Provides endpoints that allow admins to perform CRUD operations
 * and manage the status of profiles. All APIs under this controller
 * require the ROLE_ADMIN authority.
 * </p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Get all profiles</li>
 *     <li>Get profile by ID</li>
 *     <li>Change profile status (ACTIVE/BLOCKED)</li>
 *     <li>Delete profile (soft delete)</li>
 * </ul>
 *
 * <p>Security:</p>
 * <ul>
 *     <li>All endpoints require ROLE_ADMIN</li>
 * </ul>
 *
 * @author Nodir
 * @version 1.0
 * @since 2025-10-25
 */
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@EnableMethodSecurity
@Slf4j
@Tag(name = "Profile Controller", description = "APIs for admin to manage user profiles")
public class ProfileController {
    private final ProfileServiceImpl profileService;

    /**
     * Get the list of all profiles.
     *
     * @return List of ProfileResponseDTO
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get All Profiles", description = "Admin can retrieve the list of all profiles")
    public ResponseEntity<List<ProfileResponseDTO>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getProfiles());
    }

    /**
     * Get a profile by its UUID.
     *
     * @param profileId UUID of the profile
     * @return ProfileResponseDTO of the requested profile
     */
    @GetMapping("/{profileId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get Profile By ID", description = "Admin can retrieve a profile by its ID")
    public ResponseEntity<ProfileResponseDTO> getProfileById(@PathVariable("profileId") UUID profileId) {
        return ResponseEntity.ok(profileService.getProfileById(profileId));
    }

    /**
     * Change the status of a profile (ACTIVE/BLOCKED).
     *
     * @param profileId UUID of the profile
     * @param dto       DTO containing new status
     * @return AppResponse with operation result
     */
    @PutMapping("/status/{profileId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Change status of profiles", description = "This API allows admin to change status of profiles (ACTIVE/BLOCKED)")
    public ResponseEntity<AppResponse<String>> changeStatus(@PathVariable("profileId") UUID profileId,
                                                            @RequestBody ProfileStatusDTO dto) {
        return ResponseEntity.ok(profileService.changeStatus(profileId, dto.getStatus()));
    }

    /**
     * Delete a profile (soft delete).
     *
     * @param profileId UUID of the profile
     * @return AppResponse with operation result
     */
    @DeleteMapping("/delete/{profileId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete Profile", description = "This API allows admin to delete a profile (soft delete)")
    public ResponseEntity<AppResponse<String>> deleteProfile(@PathVariable("profileId") UUID profileId) {
        return ResponseEntity.ok(profileService.delete(profileId));
    }
}
