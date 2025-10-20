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

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository profileRoleRepository;

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
