package code.uz.bankcard.service;

import code.uz.bankcard.dto.transaction.TransactionCreateDTO;
import code.uz.bankcard.dto.transaction.TransactionResponseDTO;
import code.uz.bankcard.entity.CardEntity;
import code.uz.bankcard.entity.ProfileEntity;
import code.uz.bankcard.entity.TransactionEntity;
import code.uz.bankcard.enums.CardStatus;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.enums.TransactionStatus;
import code.uz.bankcard.exception.BadException;
import code.uz.bankcard.repository.CardRepository;
import code.uz.bankcard.repository.TransactionRepository;
import code.uz.bankcard.service.Impl.TransactionServiceImpl;
import code.uz.bankcard.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @BeforeEach
    void setup() {
        Mockito.mockStatic(SecurityUtil.class);
    }

    @Test
    void Transfer() {
        UUID profileId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        TransactionCreateDTO dto = new TransactionCreateDTO();
        dto.setFromCardId(fromCardId);
        dto.setToCardId(toCardId);
        dto.setAmount(new BigDecimal("100"));

        CardEntity fromCard = new CardEntity();
        fromCard.setId(fromCardId);
        fromCard.setBalance(new BigDecimal("500"));
        fromCard.setStatus(CardStatus.ACTIVE);
        ProfileEntity fromProfile = new ProfileEntity();
        fromProfile.setId(profileId);
        fromCard.setProfile(fromProfile);

        CardEntity toCard = new CardEntity();
        toCard.setId(toCardId);
        toCard.setBalance(new BigDecimal("200"));
        toCard.setStatus(CardStatus.ACTIVE);
        ProfileEntity toProfile = new ProfileEntity();
        toProfile.setId(UUID.randomUUID());
        toCard.setProfile(toProfile);

        when(SecurityUtil.getID()).thenReturn(profileId);
        when(SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(false);
        when(cardRepository.findByIdAndVisibleTrue(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndVisibleTrue(toCardId)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(cardRepository.save(fromCard)).thenReturn(fromCard);
        when(cardRepository.save(toCard)).thenReturn(toCard);

        TransactionResponseDTO response = transactionService.transfer(dto);

        assertEquals(new BigDecimal("400"), fromCard.getBalance());
        assertEquals(new BigDecimal("300"), toCard.getBalance());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
    }

    @Test
    void InsufficientBalance() {
        UUID profileId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        TransactionCreateDTO dto = new TransactionCreateDTO();
        dto.setFromCardId(fromCardId);
        dto.setToCardId(toCardId);
        dto.setAmount(new BigDecimal("600"));

        CardEntity fromCard = new CardEntity();
        fromCard.setId(fromCardId);
        fromCard.setBalance(new BigDecimal("500"));
        fromCard.setStatus(CardStatus.ACTIVE);
        ProfileEntity fromProfile = new ProfileEntity();
        fromProfile.setId(profileId);
        fromCard.setProfile(fromProfile);

        CardEntity toCard = new CardEntity();
        toCard.setId(toCardId);
        toCard.setBalance(new BigDecimal("200"));
        toCard.setStatus(CardStatus.ACTIVE);
        ProfileEntity toProfile = new ProfileEntity();
        toProfile.setId(UUID.randomUUID());
        toCard.setProfile(toProfile);

        when(SecurityUtil.getID()).thenReturn(profileId);
        when(SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(false);
        when(cardRepository.findByIdAndVisibleTrue(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndVisibleTrue(toCardId)).thenReturn(Optional.of(toCard));

        BadException exception = assertThrows(BadException.class, () -> transactionService.transfer(dto));
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void TransferByAdminSuccess() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        TransactionCreateDTO dto = new TransactionCreateDTO();
        dto.setFromCardId(fromCardId);
        dto.setToCardId(toCardId);
        dto.setAmount(new BigDecimal("100"));

        CardEntity fromCard = new CardEntity();
        fromCard.setId(fromCardId);
        fromCard.setBalance(new BigDecimal("500"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setProfile(new ProfileEntity());

        CardEntity toCard = new CardEntity();
        toCard.setId(toCardId);
        toCard.setBalance(new BigDecimal("200"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setProfile(new ProfileEntity());

        when(SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(true);
        when(cardRepository.findByIdAndVisibleTrue(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndVisibleTrue(toCardId)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(cardRepository.save(fromCard)).thenReturn(fromCard);
        when(cardRepository.save(toCard)).thenReturn(toCard);

        TransactionResponseDTO response = transactionService.transferByAdmin(dto);
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
    }

    @Test
    void TransferByAdminNotAdmin() {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        TransactionCreateDTO dto = new TransactionCreateDTO();
        dto.setFromCardId(fromCardId);
        dto.setToCardId(toCardId);
        dto.setAmount(new BigDecimal("100"));

        CardEntity fromCard = new CardEntity();
        fromCard.setId(fromCardId);
        fromCard.setBalance(new BigDecimal("500"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setProfile(new ProfileEntity());

        CardEntity toCard = new CardEntity();
        toCard.setId(toCardId);
        toCard.setBalance(new BigDecimal("200"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setProfile(new ProfileEntity());

        when(SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(false);
        when(cardRepository.findByIdAndVisibleTrue(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndVisibleTrue(toCardId)).thenReturn(Optional.of(toCard));

        BadException exception = assertThrows(BadException.class, () -> transactionService.transferByAdmin(dto));
        assertEquals("Only admins can transfer", exception.getMessage());
    }

    @Test
    void testGetAllHistory() {
        UUID profileId = UUID.randomUUID();
        when(SecurityUtil.getID()).thenReturn(profileId);

        CardEntity fromCard = new CardEntity();
        fromCard.setCardNumber("1111-2222-3333-4444");
        ProfileEntity fromProfile = new ProfileEntity();
        fromProfile.setId(profileId);
        fromCard.setProfile(fromProfile);

        CardEntity toCard = new CardEntity();
        toCard.setCardNumber("5555-6666-7777-8888");
        ProfileEntity toProfile = new ProfileEntity();
        toProfile.setId(UUID.randomUUID());
        toCard.setProfile(toProfile);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(1);
        transaction.setFromCardId(fromCard);
        transaction.setToCardId(toCard);
        transaction.setAmount(new BigDecimal("100"));
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setSentDate(LocalDateTime.now());

        when(transactionRepository.findByFromCardId_Profile_IdOrToCardId_Profile_Id(profileId, profileId))
                .thenReturn(List.of(transaction));

        Page<TransactionResponseDTO> result = transactionService.getAllHistory(0, 10);

        assertEquals(1, result.getContent().size());
        TransactionResponseDTO dto = result.getContent().get(0);
        assertEquals("1111-2222-3333-4444", dto.getFromCardNumber());
        assertEquals("5555-6666-7777-8888", dto.getToCardNumber());
        assertEquals(new BigDecimal("100"), dto.getAmount());
        assertEquals(TransactionStatus.SUCCESS, dto.getStatus());
    }


    @Test
    void testGetAllTransactionsByAdmin_Success() {
        when(SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(true);

        CardEntity fromCard = new CardEntity();
        fromCard.setCardNumber("9999-8888-7777-6666");

        CardEntity toCard = new CardEntity();
        toCard.setCardNumber("1111-0000-2222-3333");

        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(2);
        transaction.setFromCardId(fromCard);
        transaction.setToCardId(toCard);
        transaction.setAmount(new BigDecimal("50"));
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setSentDate(LocalDateTime.now());

        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        Page<TransactionResponseDTO> result = transactionService.getAllTransactionsByAdmin(0, 10);

        assertEquals(1, result.getContent().size());
        TransactionResponseDTO dto = result.getContent().get(0);
        assertEquals("9999-8888-7777-6666", dto.getFromCardNumber());
        assertEquals("1111-0000-2222-3333", dto.getToCardNumber());
        assertEquals(new BigDecimal("50"), dto.getAmount());
        assertEquals(TransactionStatus.SUCCESS, dto.getStatus());
    }


    @Test
    void testGetAllTransactionsByAdmin_NotAdmin() {
        when(SecurityUtil.hasRole(Role.ROLE_ADMIN)).thenReturn(false);

        BadException exception = assertThrows(BadException.class, () ->
                transactionService.getAllTransactionsByAdmin(0, 10)
        );

        assertEquals("Only admins can view all transactions", exception.getMessage());
    }

}
