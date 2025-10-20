package code.uz.bankcard.repository;


import code.uz.bankcard.entity.RoleEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
public interface RoleRepository extends CrudRepository<RoleEntity, Integer> {
    @Modifying
    void deleteByProfileId(UUID profileId);

    List<RoleEntity> findAllByProfileId(UUID profileId);
}
