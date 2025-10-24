package code.uz.bankcard.service.Impl;

import code.uz.bankcard.dto.transaction.TransactionCreateDTO;
import code.uz.bankcard.dto.transaction.TransactionResponseDTO;
import code.uz.bankcard.entity.CardEntity;
import code.uz.bankcard.entity.TransactionEntity;
import code.uz.bankcard.enums.CardStatus;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.enums.TransactionStatus;
import code.uz.bankcard.exception.BadException;
import code.uz.bankcard.exception.NotFoundException;
import code.uz.bankcard.repository.CardRepository;
import code.uz.bankcard.repository.TransactionRepository;
import code.uz.bankcard.service.TransactionSaveService;
import code.uz.bankcard.service.TransactionService;
import code.uz.bankcard.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final TransactionSaveService transactionSaveService;

    /**
     * Transfer money from one card to another by the current user.
     * Only allowed if the user owns the source card and both cards are active.
     *
     * @param dto Transaction data transfer object containing fromCardId, toCardId, and amount
     * @return TransactionResponseDTO containing transaction details
     * @throws NotFoundException if either card is not found
     * @throws BadException if user tries to transfer from a card they do not own
     *                      or if the sender/recipient card is not active
     */
    @Transactional
    public TransactionResponseDTO transfer(@Valid TransactionCreateDTO dto) {
        UUID profileId = SecurityUtil.getID();
        boolean isAdmin = SecurityUtil.hasRole(Role.ROLE_ADMIN);

        CardEntity fromCard = cardRepository.findByIdAndVisibleTrue(dto.getFromCardId()).orElseThrow(() -> new NotFoundException("Sender card not found"));

        CardEntity toCard = cardRepository.findByIdAndVisibleTrue(dto.getToCardId()).orElseThrow(() -> new NotFoundException("Recipient card not found"));

        if (!isAdmin && !fromCard.getProfile().getId().equals(profileId)) {
            throw new BadException("You can only transfer from your own card");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new BadException("Sender Card is not active");
        }

        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new BadException("Recipient Card is not active");
        }

        return executeTransfer(dto, fromCard, toCard);
    }

    /**
     * Transfer money from one card to another by an admin.
     * Admin can transfer from/to any cards regardless of ownership.
     *
     * @param dto Transaction data transfer object containing fromCardId, toCardId, and amount
     * @return TransactionResponseDTO containing transaction details
     * @throws NotFoundException if either card is not found
     * @throws BadException if the user is not an admin
     */
    @Transactional
    public TransactionResponseDTO transferByAdmin(@Valid TransactionCreateDTO dto) {
        boolean isAdmin = SecurityUtil.hasRole(Role.ROLE_ADMIN);

        CardEntity fromCard = cardRepository.findByIdAndVisibleTrue(dto.getFromCardId()).orElseThrow(() -> new NotFoundException("Sender card not found"));

        CardEntity toCard = cardRepository.findByIdAndVisibleTrue(dto.getToCardId()).orElseThrow(() -> new NotFoundException("Recipient card not found"));

        if (!isAdmin) {
            throw new BadException("Only admins can transfer");
        }

        return executeTransfer(dto, fromCard, toCard);
    }

    /**
     * Get transaction history for the current user.
     *
     * @param page page number
     * @param size page size
     * @return Page of TransactionResponseDTO containing transactions for the user's cards
     */
    public Page<TransactionResponseDTO> getAllHistory(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        UUID profileId = SecurityUtil.getID();

        List<TransactionEntity> transactions = transactionRepository.findByFromCardId_Profile_IdOrToCardId_Profile_Id(profileId, profileId);

        List<TransactionResponseDTO> responseDTOList = transactions.stream()
                .map(t -> {
                    TransactionResponseDTO responseDTO = new TransactionResponseDTO();
                    responseDTO.setId(t.getId());
                    responseDTO.setFromCardNumber(t.getFromCardId().getCardNumber());
                    responseDTO.setToCardNumber(t.getToCardId().getCardNumber());
                    responseDTO.setAmount(t.getAmount());
                    responseDTO.setStatus(t.getStatus());
                    responseDTO.setSentDate(t.getSentDate());
                    return responseDTO;
                })
                .toList();

        return new PageImpl<>(responseDTOList, pageRequest, responseDTOList.size());
    }

    /**
     * Get all transactions in the system (admin only).
     *
     * @param page page number
     * @param size page size
     * @return Page of TransactionResponseDTO containing all transactions
     * @throws BadException if the current user is not an admin
     */
    public Page<TransactionResponseDTO> getAllTransactionsByAdmin(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        boolean isAdmin = SecurityUtil.hasRole(Role.ROLE_ADMIN);

        if (!isAdmin) {
            throw new BadException("Only admins can view all transactions");
        }

        List<TransactionEntity> all = transactionRepository.findAll();
        List<TransactionResponseDTO> responseDTOList = all.stream()
                .map(t -> {
                    TransactionResponseDTO dto = new TransactionResponseDTO();
                    dto.setId(t.getId());
                    dto.setFromCardNumber(t.getFromCardId().getCardNumber());
                    dto.setToCardNumber(t.getToCardId().getCardNumber());
                    dto.setAmount(t.getAmount());
                    dto.setStatus(t.getStatus());
                    dto.setSentDate(t.getSentDate());
                    return dto;
                })
                .toList();

        return new PageImpl<>(responseDTOList, pageRequest, responseDTOList.size());
    }

    /**
     * Execute the transfer between two cards and save the transaction.
     * If the balance is insufficient, save as failed transaction.
     *
     * @param dto      TransactionCreateDTO containing fromCardId, toCardId, and amount
     * @param fromCard source card
     * @param toCard   destination card
     * @return TransactionResponseDTO containing transaction details
     * @throws BadException if the source card balance is insufficient
     */
    @Transactional
    protected TransactionResponseDTO executeTransfer(TransactionCreateDTO dto, CardEntity fromCard, CardEntity toCard) {
        BigDecimal amount = dto.getAmount();
        TransactionEntity entity = new TransactionEntity();

        if (fromCard.getBalance().compareTo(amount) < 0) {
            entity.setFromCardId(fromCard);
            entity.setToCardId(toCard);
            entity.setAmount(amount);
            entity.setStatus(TransactionStatus.FAILED);
            entity.setSentDate(LocalDateTime.now());
            transactionSaveService.saveFailedTransaction(entity);

            throw new BadException("Insufficient balance");
        } else {
            fromCard.setBalance(fromCard.getBalance().subtract(amount));
            toCard.setBalance(toCard.getBalance().add(amount));
            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            entity.setFromCardId(fromCard);
            entity.setToCardId(toCard);
            entity.setAmount(amount);
            entity.setStatus(TransactionStatus.SUCCESS);
            entity.setSentDate(LocalDateTime.now());
            transactionRepository.save(entity);
        }

        return toDTO(entity, fromCard, toCard);
    }

    /**
     * Convert TransactionEntity to TransactionResponseDTO.
     *
     * @param entity   transaction entity
     * @param fromCard source card
     * @param toCard   destination card
     * @return TransactionResponseDTO
     */
    private TransactionResponseDTO toDTO(TransactionEntity entity, CardEntity fromCard, CardEntity toCard) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(entity.getId());
        dto.setFromCardNumber(fromCard.getCardNumber());
        dto.setToCardNumber(toCard.getCardNumber());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        dto.setSentDate(entity.getSentDate());
        return dto;
    }
}
