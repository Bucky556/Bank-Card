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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;


    public List<ProfileResponseDTO> getProfiles() {
        List<ProfileEntity> all = profileRepository.findAll();
        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        return all.stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public AppResponse<String> changeStatus(UUID profileId, GeneralStatus status) {
        profileRepository.updateStatusById(status, profileId);

        Optional<ProfileEntity> byId = profileRepository.findById(profileId);
        if (byId.isEmpty()){
            throw new NotFoundException("profile not found");
        }
        ProfileEntity profileEntity = byId.get();
        if (profileEntity.getStatus() == GeneralStatus.BLOCKED){
            throw new BadException("Profile is already blocked");
        }

        return new AppResponse<>("Status changed");
    }

    public ProfileResponseDTO getProfileById(UUID profileId) {
        ProfileEntity profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        return toDTO(profile);
    }

    @Override
    public AppResponse<String> delete(UUID profileId) {
        profileRepository.changeVisibleById(profileId);
        return new AppResponse<>("Profile deleted");
    }

    private ProfileResponseDTO toDTO(ProfileEntity profileEntity) {
        ProfileResponseDTO profileResponseDTO = new ProfileResponseDTO();
        profileResponseDTO.setId(profileEntity.getId());
        profileResponseDTO.setName(profileEntity.getName());
        profileResponseDTO.setUsername(profileEntity.getUsername());
        if (profileEntity.getRole() != null) {
            List<Role> roleList = profileEntity.getRole()
                    .stream()
                    .map(RoleEntity::getRole)
                    .toList();
            profileResponseDTO.setRoles(roleList);
        }
        return profileResponseDTO;
    }
}
