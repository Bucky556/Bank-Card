package code.uz.bankcard.service;

import code.uz.bankcard.entity.TransactionEntity;
import code.uz.bankcard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionSaveService {
    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedTransaction(TransactionEntity entity) {
        transactionRepository.save(entity);
    }
}
