package code.uz.bankcard.controller;

import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.CardBalanceDTO;
import code.uz.bankcard.dto.filter.CardFilterDTO;
import code.uz.bankcard.dto.card.CardResponseDTO;
import code.uz.bankcard.dto.card.CardCreateDTO;
import code.uz.bankcard.service.Impl.CardServiceImpl;
import code.uz.bankcard.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/card")
@RequiredArgsConstructor
@EnableMethodSecurity
@Tag(name = "User Card Management", description = "APIs for users to manage their own bank cards")
@Slf4j
public class CardController {
    private final CardServiceImpl cardService;

    @PostMapping("/create")
    @Operation(summary = "Create a new card", description = "Allows a user to create a new bank card")
    public ResponseEntity<CardResponseDTO> create(@RequestBody @Valid CardCreateDTO dto) {
        log.info("Create card requested by profile ID: {}", dto.getProfileId());
        return ResponseEntity.ok(cardService.createCard(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all cards with pagination", description = "Returns a paginated list of all cards owned by the user")
    public ResponseEntity<Page<CardResponseDTO>> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "3") int size
    ) {
        return ResponseEntity.ok(cardService.getAll(PageUtil.getCurrentPage(page), size));
    }

    @GetMapping("/{cardID}")
    @Operation(summary = "Get card by ID", description = "Returns details of a specific card by its UUID")
    public ResponseEntity<CardResponseDTO> getCardById(@PathVariable UUID cardID) {
        return ResponseEntity.ok(cardService.getById(cardID));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @Operation(summary = "Delete a card", description = "Allows a user or admin to delete a specific card by its ID")
    public ResponseEntity<AppResponse<String>> delete(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.delete(cardId));
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter cards with pagination", description = "Returns a paginated list of cards based on filtering criteria")
    public ResponseEntity<PageImpl<CardResponseDTO>> filter(@RequestBody @Valid CardFilterDTO dto,
                                                            @RequestParam(value = "page", defaultValue = "1") int page,
                                                            @RequestParam(value = "size", defaultValue = "5") int size) {
        return ResponseEntity.ok(cardService.filter(dto, PageUtil.getCurrentPage(page), size));
    }

    @GetMapping("/balance/{cardId}")
    @Operation(summary = "View Balance", description = "User can see balance of card")
    public ResponseEntity<CardResponseDTO> getCardBalance(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId));
    }
}
