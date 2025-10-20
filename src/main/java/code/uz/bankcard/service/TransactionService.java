package code.uz.bankcard.service;

import code.uz.bankcard.dto.transaction.TransactionCreateDTO;
import code.uz.bankcard.dto.transaction.TransactionResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

public interface TransactionService {
    TransactionResponseDTO transfer(@Valid TransactionCreateDTO dto);
    TransactionResponseDTO transferByAdmin(@Valid TransactionCreateDTO dto);
    Page<TransactionResponseDTO> getAllHistory(int page, int size);
    Page<TransactionResponseDTO> getAllTransactionsByAdmin(int page, int size);
}
