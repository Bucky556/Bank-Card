package code.uz.bankcard.service.Impl;

import code.uz.bankcard.entity.RoleEntity;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.repository.RoleRepository;
import code.uz.bankcard.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link RoleService} that handles
 * assigning roles to user profiles.
 * <p>
 * This service is responsible for mapping {@link Role} enums
 * to {@link RoleEntity} objects and persisting them via {@link RoleRepository}.
 * </p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Create roles for a given profile</li>
 * </ul>
 *
 * @author Nodir
 * @version 1.0
 * @since 2025-10-25
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository profileRoleRepository;

    /**
     * Assigns a list of roles to a profile.
     * <p>
     * Each {@link Role} provided is mapped to a {@link RoleEntity},
     * the current timestamp is set, and then all roles are saved
     * in bulk using {@link RoleRepository # saveAll(List)}.
     * </p>
     *
     * @param profileId the UUID of the profile to which roles will be assigned
     * @param roles a list of {@link Role} enums to assign to the profile
     */
    public void create(UUID profileId, List<Role> roles) {
        List<RoleEntity> roleEntities = roles.stream()
                .map(role -> {
                    RoleEntity roleEntity = new RoleEntity();
                    roleEntity.setProfileId(profileId);
                    roleEntity.setCreatedDate(LocalDateTime.now());
                    roleEntity.setRole(role);
                    return roleEntity;
                })
                .toList();
        profileRoleRepository.saveAll(roleEntities);
    }
}
