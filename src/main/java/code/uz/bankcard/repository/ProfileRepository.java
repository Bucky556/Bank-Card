package code.uz.bankcard.repository;

import code.uz.bankcard.entity.ProfileEntity;
import code.uz.bankcard.enums.GeneralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Transactional
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
    Optional<ProfileEntity> findByUsernameAndVisibleTrue(String username);

    Optional<ProfileEntity> findByIdAndVisibleTrue(UUID id);

    @Query("update ProfileEntity p set p.status = :status where p.id = :id")
    @Modifying
    void updateStatusById(GeneralStatus status, UUID id);

    @Query("update ProfileEntity p set p.visible = false where p.id = :profileId")
    @Modifying
    void changeVisibleById(UUID profileId);
}
