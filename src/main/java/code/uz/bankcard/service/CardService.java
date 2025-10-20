package code.uz.bankcard.service;

import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.card.CardAdminUpdateDTO;
import code.uz.bankcard.dto.card.CardCreateDTO;
import code.uz.bankcard.dto.card.CardResponseDTO;
import code.uz.bankcard.dto.filter.CardFilterDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.UUID;

public interface CardService {
    CardResponseDTO createCard(@Valid CardCreateDTO dto);

    Page<CardResponseDTO> getAll(int page, int size);

    CardResponseDTO getById(UUID cardID);

    CardResponseDTO updateCard(CardAdminUpdateDTO dto, UUID cardId);

    AppResponse<String> delete(UUID cardId);

    Page<CardResponseDTO> getAllByAdmin(int page, int size);

    CardResponseDTO getCardByAdmin(UUID cardID);

    PageImpl<CardResponseDTO> filter(@Valid CardFilterDTO dto, int page, int size);

}
