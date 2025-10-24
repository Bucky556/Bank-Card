package code.uz.bankcard.service.Impl;

import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.auth.ProfileResponseDTO;
import code.uz.bankcard.entity.ProfileEntity;
import code.uz.bankcard.entity.RoleEntity;
import code.uz.bankcard.enums.GeneralStatus;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.exception.BadException;
import code.uz.bankcard.exception.NotFoundException;
import code.uz.bankcard.repository.ProfileRepository;
import code.uz.bankcard.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of ProfileService for managing user profiles.
 * Handles retrieval, status changes, and logical deletion.
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    /**
     * Get all profiles.
     *
     * @return list of ProfileResponseDTO
     */
    public List<ProfileResponseDTO> getProfiles() {
        List<ProfileEntity> allProfiles = profileRepository.findAll();
        if (allProfiles.isEmpty()) {
            return Collections.emptyList();
        }
        return allProfiles.stream().map(this::toDTO).toList();
    }

    /**
     * Get profile by ID.
     *
     * @param profileId UUID of profile
     * @return ProfileResponseDTO
     * @throws NotFoundException if profile does not exist
     */
    public ProfileResponseDTO getProfileById(UUID profileId) {
        ProfileEntity profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        return toDTO(profile);
    }

    /**
     * Change the status of a profile (e.g., ACTIVE, BLOCKED).
     * Throws exception if trying to block an already blocked profile.
     *
     * @param profileId UUID of profile
     * @param status new status
     * @return AppResponse confirmation
     */
    public AppResponse<String> changeStatus(UUID profileId, GeneralStatus status) {
        ProfileEntity profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        if (profile.getStatus() == status && status == GeneralStatus.BLOCKED) {
            throw new BadException("Profile is already blocked");
        }

        profileRepository.updateStatusById(status, profileId);
        return new AppResponse<>("Status changed");
    }

    /**
     * Logical delete a profile (set visible=false).
     *
     * @param profileId UUID of profile
     * @return AppResponse confirmation
     */
    public AppResponse<String> delete(UUID profileId) {
        profileRepository.changeVisibleById(profileId);
        return new AppResponse<>("Profile deleted");
    }

    /**
     * Convert ProfileEntity to ProfileResponseDTO.
     */
    private ProfileResponseDTO toDTO(ProfileEntity profile) {
        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(profile.getId());
        dto.setName(profile.getName());
        dto.setUsername(profile.getUsername());

        if (profile.getRole() != null && !profile.getRole().isEmpty()) {
            List<Role> roleList = profile.getRole().stream()
                    .map(RoleEntity::getRole)
                    .toList();
            dto.setRoles(roleList);
        } else {
            dto.setRoles(Collections.emptyList());
        }

        return dto;
    }
}
