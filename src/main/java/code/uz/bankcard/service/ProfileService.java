package code.uz.bankcard.service;

import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.auth.ProfileResponseDTO;
import code.uz.bankcard.enums.GeneralStatus;

import java.util.List;
import java.util.UUID;

public interface ProfileService {
    List<ProfileResponseDTO> getProfiles();

    ProfileResponseDTO getProfileById(UUID profileId);

    AppResponse<String> changeStatus(UUID profileId, GeneralStatus status);

    AppResponse<String> delete(UUID profileId);
}
