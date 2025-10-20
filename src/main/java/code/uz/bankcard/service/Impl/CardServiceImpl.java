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

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final ProfileRepository profileRepository;
    private final FilterRepository filterRepository;


    public CardResponseDTO createCard(@Valid CardCreateDTO dto) {
        Optional<ProfileEntity> byProfileId = profileRepository.findByIdAndVisibleTrue(dto.getProfileId());
        if (byProfileId.isEmpty()) {
            throw new NotFoundException("Card profile not found");
        }


        CardEntity cardEntity = new CardEntity();
        cardEntity.setCardNumber(dto.getCardNumber());
        cardEntity.setBalance(dto.getInitialBalance());
        cardEntity.setCreatedDate(LocalDateTime.now());
        cardEntity.setExpiryDate(LocalDateTime.now().plusYears(4));
        cardEntity.setStatus(CardStatus.ACTIVE);
        cardEntity.setProfile(byProfileId.get());
        cardRepository.save(cardEntity);

        return getCardResponse(cardEntity);
    }


    public Page<CardResponseDTO> getAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        UUID profileId = SecurityUtil.getID();
        List<CardEntity> allCard = cardRepository.findAllByProfileIdAndVisibleTrueOrderByCreatedDateDesc(profileId, pageRequest);
        if (allCard.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<CardResponseDTO> responseDTOList = allCard.stream()
                .map(this::getCardResponseMasked)
                .toList();

        return new PageImpl<>(responseDTOList, pageRequest, responseDTOList.size());
    }


    public CardResponseDTO getById(UUID cardID) {
        UUID profileId = SecurityUtil.getID();
        Optional<CardEntity> cardByProfile = cardRepository.findByIdAndProfileIdAndVisibleTrue(cardID, profileId);
        if (cardByProfile.isEmpty()) {
            throw new NotFoundException("Card not found");
        }

        checkCardStatus(cardByProfile.get());

        return getCardResponse(cardByProfile.get());
    }

    public CardResponseDTO updateCard(CardAdminUpdateDTO dto, UUID cardId) {
        Optional<CardEntity> byCardId = cardRepository.findByIdAndVisibleTrue(cardId);
        if (byCardId.isEmpty()) {
            throw new NotFoundException("Card not found");
        }
        CardEntity cardEntity = byCardId.get();

        if (dto.getStatus() != null) {
            cardEntity.setStatus(dto.getStatus());
        }

        if (dto.getExpiryDate() != null) {
            cardEntity.setExpiryDate(dto.getExpiryDate());

            if (cardEntity.getExpiryDate().isAfter(LocalDateTime.now()) && !(cardEntity.getStatus() == CardStatus.BLOCKED)) {
                cardEntity.setStatus(CardStatus.ACTIVE);
            }

            if (cardEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
                cardEntity.setStatus(CardStatus.EXPIRED);
            }
        }

        if (dto.getBalance() != null) {
            cardEntity.setBalance(dto.getBalance());
        }

        cardRepository.save(cardEntity);
        return getCardResponse(cardEntity);
    }

    public AppResponse<String> delete(UUID cardId) {
        UUID profileId = SecurityUtil.getID();
        boolean admin = SecurityUtil.hasRole(Role.ROLE_ADMIN);

        CardEntity card = cardRepository.findByIdAndVisibleTrue(cardId).orElseThrow(() -> new NotFoundException("Card not found"));

        if (!admin && !card.getProfile().getId().equals(profileId)) {
            throw new BadException("This card does not belong to this profile");
        }
        cardRepository.changeVisibleById(cardId);
        return new AppResponse<>("Successfully deleted");
    }

    public Page<CardResponseDTO> getAllByAdmin(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if (!SecurityUtil.hasRole(Role.ROLE_ADMIN)) {
            throw new BadException("Admin privileges are required");
        }

        List<CardEntity> allCard = cardRepository.findAll();
        if (allCard.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<CardResponseDTO> responseDTOList = allCard.stream()
                .map(this::getCardResponse)
                .toList();

        return new PageImpl<>(responseDTOList, pageRequest, responseDTOList.size());
    }

    public CardResponseDTO getCardByAdmin(UUID cardID) {
        if (!SecurityUtil.hasRole(Role.ROLE_ADMIN)) {
            throw new BadException("Admin privileges are required");
        }

        CardEntity cardEntity = cardRepository.findById(cardID).orElseThrow(() -> new NotFoundException("Card not found"));
        return getCardResponse(cardEntity);
    }

    public PageImpl<CardResponseDTO> filter(@Valid CardFilterDTO dto, int page, int size) {
        UUID profileId = SecurityUtil.getID();
        boolean isAdmin = SecurityUtil.hasRole(Role.ROLE_ADMIN);
        profileRepository.findByIdAndVisibleTrue(profileId).orElseThrow(() -> new NotFoundException("Profile not found"));

        FilterResultDTO<CardEntity> filter = filterRepository.filter(dto, profileId, isAdmin, page, size);
        List<CardResponseDTO> responseList = filter.getList()
                .stream()
                .map(this::getCardResponseMasked)
                .toList();

        return new PageImpl<>(responseList, PageRequest.of(page, size), filter.getTotalCount());
    }

    @Override
    public AppResponse<String> changeStatus(UUID cardId, CardStatus status) {
        cardRepository.updateStatusById(status,cardId);
        return new AppResponse<>("Status successfully changed");
    }

    @Override
    public CardResponseDTO getBalance(UUID cardId) {
        UUID profileId = SecurityUtil.getID();

        CardEntity cardEntity = cardRepository.findByIdAndVisibleTrue(cardId).orElseThrow(() -> new NotFoundException("Card not found"));

        if (!cardEntity.getProfile().getId().equals(profileId)) {
            throw new BadException("This card does not belong to this profile");
        }

        return getCardResponseMasked(cardEntity);
    }

    @Override
    @Transactional
    public AppResponse<String> requestCardBlock(UUID cardId) {
        UUID profileId = SecurityUtil.getID();

        CardEntity card = cardRepository.findByIdAndVisibleTrue(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!card.getProfile().getId().equals(profileId)) {
            throw new BadException("This card does not belong to your profile");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new BadException("Card is already blocked");
        }

        cardRepository.updateStatusById(CardStatus.REQUEST_BLOCK, cardId);

        return new AppResponse<>("Request sent!");
    }

    private CardResponseDTO getCardResponse(CardEntity cardEntity) {
        CardResponseDTO responseDTO = new CardResponseDTO();
        responseDTO.setId(cardEntity.getId());
        responseDTO.setOwnerName(cardEntity.getProfile().getName());
        responseDTO.setBalance(cardEntity.getBalance());
        responseDTO.setMaskedNumber(cardEntity.getCardNumber());
        responseDTO.setStatus(cardEntity.getStatus());
        responseDTO.setExpiryDate(cardEntity.getExpiryDate());
        return responseDTO;
    }

    private CardResponseDTO getCardResponseMasked(CardEntity cardEntity) {
        CardResponseDTO responseDTO = new CardResponseDTO();
        responseDTO.setId(cardEntity.getId());
        responseDTO.setOwnerName(cardEntity.getProfile().getName());
        responseDTO.setBalance(cardEntity.getBalance());
        responseDTO.setMaskedNumber(getMaskedNumber(cardEntity.getCardNumber()));
        responseDTO.setStatus(cardEntity.getStatus());
        responseDTO.setExpiryDate(cardEntity.getExpiryDate());

        if (cardEntity.getStatus() == CardStatus.BLOCKED) {
            responseDTO.setNote("This card is blocked. Operation not allowed.");
        } else if (cardEntity.getStatus() == CardStatus.EXPIRED) {
            responseDTO.setNote("This card is expired. Operation not allowed.");
        }

        return responseDTO;
    }

    public void checkCardStatus(CardEntity cardEntity) {
        if (cardEntity.getStatus() == CardStatus.BLOCKED) {
            throw new BadException("This card is blocked. Operation not allowed.");
        }

        if (cardEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            cardEntity.setStatus(CardStatus.EXPIRED);
            throw new BadException("This card is expired. Operation not allowed.");
        }
    }

    private String getMaskedNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
