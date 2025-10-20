package code.uz.bankcard.repository;

import code.uz.bankcard.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {
    List<TransactionEntity> findByFromCardId_Profile_IdOrToCardId_Profile_Id(UUID fromProfileId, UUID toProfileId);
}
