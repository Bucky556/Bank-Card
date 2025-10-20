package code.uz.bankcard.service;

import code.uz.bankcard.dto.card.CardAdminUpdateDTO;
import code.uz.bankcard.dto.card.CardCreateDTO;
import code.uz.bankcard.dto.card.CardResponseDTO;
import code.uz.bankcard.entity.CardEntity;
import code.uz.bankcard.entity.ProfileEntity;
import code.uz.bankcard.enums.CardStatus;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.exception.BadException;
import code.uz.bankcard.exception.NotFoundException;
import code.uz.bankcard.repository.CardRepository;
import code.uz.bankcard.repository.ProfileRepository;
import code.uz.bankcard.service.Impl.CardServiceImpl;
import code.uz.bankcard.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private UUID profileId;
    private ProfileEntity profileEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileId = UUID.randomUUID();
        profileEntity = new ProfileEntity();
        profileEntity.setId(profileId);
        profileEntity.setName("Test User");
    }

    @Test
    void createCard_Success() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setProfileId(profileId);
        dto.setCardNumber("1234567890123456");
        dto.setInitialBalance(BigDecimal.valueOf(1000.0));

        when(profileRepository.findByIdAndVisibleTrue(profileId)).thenReturn(Optional.of(profileEntity));

        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getID).thenReturn(profileId);
            utilities.when(() -> SecurityUtil.hasRole(any())).thenReturn(false);

            CardResponseDTO response = cardService.createCard(dto);

            assertEquals("Test User", response.getOwnerName());
            assertEquals(CardStatus.ACTIVE, response.getStatus());
            verify(cardRepository, times(1)).save(any(CardEntity.class));
        }
    }

    @Test
    void createCard_ProfileNotFound() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setProfileId(profileId);
        dto.setCardNumber("1234567890123456");
        dto.setInitialBalance(BigDecimal.valueOf(1000.0));

        when(profileRepository.findByIdAndVisibleTrue(profileId)).thenReturn(Optional.empty());

        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getID).thenReturn(profileId);
            utilities.when(() -> SecurityUtil.hasRole(any())).thenReturn(false);

            assertThrows(NotFoundException.class, () -> cardService.createCard(dto));
        }
    }

    @Test
    void getById_Success() {
        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setProfile(profileEntity);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryDate(LocalDateTime.now().plusDays(1));

        when(cardRepository.findByIdAndProfileIdAndVisibleTrue(any(), any())).thenReturn(Optional.of(card));

        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getID).thenReturn(profileId);
            utilities.when(() -> SecurityUtil.hasRole(any())).thenReturn(false);

            CardResponseDTO response = cardService.getById(card.getId());

            assertEquals(card.getId(), response.getId());
            assertEquals(CardStatus.ACTIVE, response.getStatus());
        }
    }

    @Test
    void getById_NotFound() {
        when(cardRepository.findByIdAndProfileIdAndVisibleTrue(any(), any())).thenReturn(Optional.empty());

        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getID).thenReturn(profileId);
            utilities.when(() -> SecurityUtil.hasRole(any())).thenReturn(false);

            assertThrows(NotFoundException.class, () -> cardService.getById(UUID.randomUUID()));
        }
    }

    @Test
    void updateCard_Success() {
        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setProfile(profileEntity);
        card.setBalance(BigDecimal.valueOf(1000.0));
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryDate(LocalDateTime.now().plusYears(1));

        when(cardRepository.findByIdAndVisibleTrue(card.getId())).thenReturn(Optional.of(card));

        CardAdminUpdateDTO dto = new CardAdminUpdateDTO();
        dto.setBalance(BigDecimal.valueOf(2000.0));
        dto.setStatus(CardStatus.BLOCKED);

        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getID).thenReturn(profileId);
            utilities.when(() -> SecurityUtil.hasRole(any())).thenReturn(true);

            CardResponseDTO response = cardService.updateCard(dto, card.getId());

            assertEquals(0, response.getBalance().compareTo(BigDecimal.valueOf(2000.0)));
            assertEquals(CardStatus.BLOCKED, response.getStatus());
            verify(cardRepository, times(1)).save(any(CardEntity.class));
        }
    }

    @Test
    void delete_NotOwner() {
        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        ProfileEntity otherProfile = new ProfileEntity();
        otherProfile.setId(UUID.randomUUID());
        card.setProfile(otherProfile);

        when(cardRepository.findByIdAndVisibleTrue(card.getId())).thenReturn(Optional.of(card));

        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getID).thenReturn(profileId);
            utilities.when(() -> SecurityUtil.hasRole(any())).thenReturn(false);

            assertThrows(BadException.class, () -> cardService.delete(card.getId()));
        }
    }

    @Test
    void getAllByAdmin_Success() {
        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setProfile(profileEntity);

        when(cardRepository.findAll()).thenReturn(List.of(card));

        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(() -> SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(true);

            Page<CardResponseDTO> response = cardService.getAllByAdmin(0, 10);

            assertEquals(1, response.getContent().size());
        }
    }

    @Test
    void getAllByAdmin_NotAdmin() {
        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(() -> SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(false);

            assertThrows(BadException.class, () -> cardService.getAllByAdmin(0, 10));
        }
    }

    @Test
    void getCardByAdmin_Success() {
        CardEntity card = new CardEntity();
        card.setId(UUID.randomUUID());
        card.setProfile(profileEntity);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(() -> SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(true);

            CardResponseDTO response = cardService.getCardByAdmin(card.getId());

            assertEquals(card.getId(), response.getId());
        }
    }

    @Test
    void getCardByAdmin_NotAdmin() {
        try (var utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(() -> SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(false);

            assertThrows(BadException.class, () -> cardService.getCardByAdmin(UUID.randomUUID()));
        }
    }

    @Test
    void checkCardStatus_Blocked() {
        CardEntity card = new CardEntity();
        card.setStatus(CardStatus.BLOCKED);

        assertThrows(BadException.class, () -> cardService.checkCardStatus(card));
    }

    @Test
    void checkCardStatus_Expired() {
        CardEntity card = new CardEntity();
        card.setExpiryDate(LocalDateTime.now().minusDays(1));
        card.setStatus(CardStatus.ACTIVE);

        assertThrows(BadException.class, () -> cardService.checkCardStatus(card));
        assertEquals(CardStatus.EXPIRED, card.getStatus());
    }
}
