package code.uz.bankcard.service;

import code.uz.bankcard.enums.Role;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    void create(UUID profileId, List<Role> roles);
}
