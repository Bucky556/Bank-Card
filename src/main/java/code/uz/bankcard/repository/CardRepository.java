package code.uz.bankcard.repository;

import code.uz.bankcard.entity.CardEntity;
import code.uz.bankcard.enums.CardStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
public interface CardRepository extends JpaRepository<CardEntity, UUID> {
    List<CardEntity> findAllByProfileIdAndVisibleTrueOrderByCreatedDateDesc(UUID profileId, Pageable pageable);

    Optional<CardEntity> findByIdAndProfileIdAndVisibleTrue(UUID id, UUID profileId);

    Optional<CardEntity> findByIdAndVisibleTrue(UUID id);

    @Modifying
    @Query("update CardEntity set visible = false where id = :id")
    void changeVisibleById(UUID id);

    @Query("update CardEntity c set c.status = :status where c.id = :id")
    @Modifying
    void updateStatusById(CardStatus status, UUID id);
}
