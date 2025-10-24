package code.uz.bankcard.service.Impl;

import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.filter.CardFilterDTO;
import code.uz.bankcard.dto.filter.FilterResultDTO;
import code.uz.bankcard.dto.card.CardResponseDTO;
import code.uz.bankcard.dto.card.CardAdminUpdateDTO;
import code.uz.bankcard.dto.card.CardCreateDTO;
import code.uz.bankcard.entity.CardEntity;
import code.uz.bankcard.entity.ProfileEntity;
import code.uz.bankcard.enums.CardStatus;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.exception.BadException;
import code.uz.bankcard.exception.NotFoundException;
import code.uz.bankcard.repository.CardRepository;
import code.uz.bankcard.repository.FilterRepository;
import code.uz.bankcard.repository.ProfileRepository;
import code.uz.bankcard.service.CardService;
import code.uz.bankcard.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for managing bank cards.
 * <p>
 * Handles:
 * <ul>
 *     <li>Create, retrieve, update, delete cards</li>
 *     <li>User and Admin access</li>
 *     <li>Card filtering and status changes</li>
 *     <li>Masked card display for security</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ProfileRepository profileRepository;
    private final FilterRepository filterRepository;

    // ========================= User Methods =========================

    /**
     * Create a new card for a specific profile.
     *
     * @param dto CardCreateDTO containing profileId, cardNumber, and initialBalance
     * @return CardResponseDTO with created card details
     * @throws NotFoundException if profile does not exist
     */
    public CardResponseDTO createCard(@Valid CardCreateDTO dto) {
        ProfileEntity profile = profileRepository.findByIdAndVisibleTrue(dto.getProfileId())
                .orElseThrow(() -> new NotFoundException("Card profile not found"));

        CardEntity card = new CardEntity();
        card.setCardNumber(dto.getCardNumber());
        card.setBalance(dto.getInitialBalance());
        card.setCreatedDate(LocalDateTime.now());
        card.setExpiryDate(LocalDateTime.now().plusYears(4));
        card.setStatus(CardStatus.ACTIVE);
        card.setProfile(profile);

        cardRepository.save(card);
        return getCardResponse(card);
    }

    /**
     * Get all cards for the currently authenticated user.
     *
     * @param page page number
     * @param size page size
     * @return Page of CardResponseDTO with masked card numbers
     */
    public Page<CardResponseDTO> getAll(int page, int size) {
        UUID profileId = SecurityUtil.getID();
        PageRequest pageRequest = PageRequest.of(page, size);

        List<CardEntity> cards = cardRepository.findAllByProfileIdAndVisibleTrueOrderByCreatedDateDesc(profileId, pageRequest);
        if (cards.isEmpty()) return new PageImpl<>(Collections.emptyList());

        List<CardResponseDTO> response = cards.stream().map(this::getCardResponseMasked).toList();
        return new PageImpl<>(response, pageRequest, response.size());
    }

    /**
     * Get card by ID for the current user.
     *
     * @param cardId UUID of the card
     * @return CardResponseDTO
     * @throws NotFoundException if card does not exist
     * @throws BadException if card is blocked or expired
     */
    public CardResponseDTO getById(UUID cardId) {
        UUID profileId = SecurityUtil.getID();
        CardEntity card = cardRepository.findByIdAndProfileIdAndVisibleTrue(cardId, profileId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        checkCardStatus(card);
        return getCardResponse(card);
    }

    /**
     * Get the current balance of a user's card.
     *
     * @param cardId UUID of the card
     * @return CardResponseDTO with masked number and balance
     */
    public CardResponseDTO getBalance(UUID cardId) {
        UUID profileId = SecurityUtil.getID();
        CardEntity card = cardRepository.findByIdAndVisibleTrue(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!card.getProfile().getId().equals(profileId))
            throw new BadException("This card does not belong to this profile");

        return getCardResponseMasked(card);
    }

    /**
     * Request to block a card.
     *
     * @param cardId UUID of the card
     * @return AppResponse confirmation
     */
    @Transactional
    public AppResponse<String> requestCardBlock(UUID cardId) {
        UUID profileId = SecurityUtil.getID();
        CardEntity card = cardRepository.findByIdAndVisibleTrue(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!card.getProfile().getId().equals(profileId))
            throw new BadException("This card does not belong to your profile");

        if (card.getStatus() == CardStatus.BLOCKED)
            throw new BadException("Card is already blocked");

        cardRepository.updateStatusById(CardStatus.REQUEST_BLOCK, cardId);
        return new AppResponse<>("Request sent!");
    }

    /**
     * Delete a card (logical delete by setting visible=false).
     *
     * @param cardId UUID of the card
     * @return AppResponse confirmation
     */
    public AppResponse<String> delete(UUID cardId) {
        UUID profileId = SecurityUtil.getID();
        boolean isAdmin = SecurityUtil.hasRole(Role.ROLE_ADMIN);

        CardEntity card = cardRepository.findByIdAndVisibleTrue(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!isAdmin && !card.getProfile().getId().equals(profileId))
            throw new BadException("This card does not belong to this profile");

        cardRepository.changeVisibleById(cardId);
        return new AppResponse<>("Successfully deleted");
    }

    // ========================= Admin Methods =========================

    /**
     * Get all cards in the system (admin only).
     */
    public Page<CardResponseDTO> getAllByAdmin(int page, int size) {
        if (!SecurityUtil.hasRole(Role.ROLE_ADMIN))
            throw new BadException("Admin privileges are required");

        PageRequest pageRequest = PageRequest.of(page, size);
        List<CardEntity> cards = cardRepository.findAll();
        if (cards.isEmpty()) return new PageImpl<>(Collections.emptyList());

        List<CardResponseDTO> response = cards.stream().map(this::getCardResponse).toList();
        return new PageImpl<>(response, pageRequest, response.size());
    }

    /**
     * Get card by ID (admin only).
     */
    public CardResponseDTO getCardByAdmin(UUID cardId) {
        if (!SecurityUtil.hasRole(Role.ROLE_ADMIN))
            throw new BadException("Admin privileges are required");

        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));
        return getCardResponse(card);
    }

    /**
     * Update card by admin (status, balance, expiry date).
     */
    public CardResponseDTO updateCard(CardAdminUpdateDTO dto, UUID cardId) {
        CardEntity card = cardRepository.findByIdAndVisibleTrue(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (dto.getStatus() != null) card.setStatus(dto.getStatus());
        if (dto.getBalance() != null) card.setBalance(dto.getBalance());

        if (dto.getExpiryDate() != null) {
            card.setExpiryDate(dto.getExpiryDate());
            if (card.getExpiryDate().isAfter(LocalDateTime.now()) && card.getStatus() != CardStatus.BLOCKED)
                card.setStatus(CardStatus.ACTIVE);
            if (card.getExpiryDate().isBefore(LocalDateTime.now()))
                card.setStatus(CardStatus.EXPIRED);
        }

        cardRepository.save(card);
        return getCardResponse(card);
    }

    /**
     * Filter cards for user/admin.
     */
    public PageImpl<CardResponseDTO> filter(@Valid CardFilterDTO dto, int page, int size) {
        UUID profileId = SecurityUtil.getID();
        boolean isAdmin = SecurityUtil.hasRole(Role.ROLE_ADMIN);

        profileRepository.findByIdAndVisibleTrue(profileId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        FilterResultDTO<CardEntity> filter = filterRepository.filter(dto, profileId, isAdmin, page, size);
        List<CardResponseDTO> response = filter.getList().stream().map(this::getCardResponseMasked).toList();

        return new PageImpl<>(response, PageRequest.of(page, size), filter.getTotalCount());
    }

    /**
     * Change status of a card (admin or user with permission).
     */
    public AppResponse<String> changeStatus(UUID cardId, CardStatus status) {
        cardRepository.updateStatusById(status, cardId);
        return new AppResponse<>("Status successfully changed");
    }

    // ========================= Helper Methods =========================

    /**
     * Check card status for operations.
     */
    public void checkCardStatus(CardEntity card) {
        if (card.getStatus() == CardStatus.BLOCKED)
            throw new BadException("This card is blocked. Operation not allowed.");
        if (card.getExpiryDate().isBefore(LocalDateTime.now())) {
            card.setStatus(CardStatus.EXPIRED);
            throw new BadException("This card is expired. Operation not allowed.");
        }
    }

    /**
     * Build CardResponseDTO with all details.
     */
    private CardResponseDTO getCardResponse(CardEntity card) {
        CardResponseDTO dto = new CardResponseDTO();
        dto.setId(card.getId());
        dto.setOwnerName(card.getProfile().getName());
        dto.setBalance(card.getBalance());
        dto.setMaskedNumber(card.getCardNumber());
        dto.setStatus(card.getStatus());
        dto.setExpiryDate(card.getExpiryDate());
        return dto;
    }

    /**
     * Build CardResponseDTO with masked number.
     */
    private CardResponseDTO getCardResponseMasked(CardEntity card) {
        CardResponseDTO dto = getCardResponse(card);
        dto.setMaskedNumber(getMaskedNumber(card.getCardNumber()));
        if (card.getStatus() == CardStatus.BLOCKED)
            dto.setNote("This card is blocked. Operation not allowed.");
        if (card.getStatus() == CardStatus.EXPIRED)
            dto.setNote("This card is expired. Operation not allowed.");
        return dto;
    }

    /**
     * Mask card number for security.
     */
    private String getMaskedNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
