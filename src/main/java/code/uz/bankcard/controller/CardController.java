package code.uz.bankcard.controller;

import code.uz.bankcard.dto.AppResponse;
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

import java.util.UUID;

/**
 * Controller for managing bank cards by users.
 * <p>
 * Provides endpoints for creating, retrieving, deleting, filtering cards,
 * checking balances, and requesting card blocks. All endpoints are scoped
 * to the authenticated user unless explicitly restricted by role.
 * </p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Create a new card</li>
 *     <li>Get all cards with pagination</li>
 *     <li>Retrieve card by ID</li>
 *     <li>Delete a card (soft delete)</li>
 *     <li>Filter cards using various criteria</li>
 *     <li>View card balance</li>
 *     <li>Request card block</li>
 * </ul>
 *
 * <p>Security:</p>
 * <ul>
 *     <li>Most endpoints require authentication</li>
 *     <li>Card block request requires ROLE_USER</li>
 * </ul>
 *
 * @author Nodir
 * @version 1.0
 * @since 2025-10-25
 */
@RestController
@RequestMapping("/api/v1/card")
@RequiredArgsConstructor
@EnableMethodSecurity
@Tag(name = "User Card Management", description = "APIs for users to manage their own bank cards")
@Slf4j
public class CardController {
    private final CardServiceImpl cardService;

    /**
     * Create a new bank card.
     *
     * @param dto DTO containing card creation information
     * @return Created CardResponseDTO
     */
    @PostMapping("/create")
    @Operation(summary = "Create a new card", description = "Allows a user to create a new bank card")
    public ResponseEntity<CardResponseDTO> create(@RequestBody @Valid CardCreateDTO dto) {
        log.info("Create card requested by profile ID: {}", dto.getProfileId());
        return ResponseEntity.ok(cardService.createCard(dto));
    }

    /**
     * Get all cards owned by the user with pagination.
     *
     * @param page Page number (default 1)
     * @param size Page size (default 3)
     * @return Paginated list of CardResponseDTO
     */
    @GetMapping("/list")
    @Operation(summary = "Get all cards with pagination", description = "Returns a paginated list of all cards owned by the user")
    public ResponseEntity<Page<CardResponseDTO>> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "3") int size
    ) {
        return ResponseEntity.ok(cardService.getAll(PageUtil.getCurrentPage(page), size));
    }

    /**
     * Get a specific card by its UUID.
     *
     * @param cardID UUID of the card
     * @return CardResponseDTO for the specified card
     */
    @GetMapping("/{cardID}")
    @Operation(summary = "Get card by ID", description = "Returns details of a specific card by its UUID")
    public ResponseEntity<CardResponseDTO> getCardById(@PathVariable UUID cardID) {
        return ResponseEntity.ok(cardService.getById(cardID));
    }

    /**
     * Delete a specific card (soft delete).
     *
     * @param cardId UUID of the card
     * @return AppResponse with operation result
     */
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @Operation(summary = "Delete a card", description = "Allows a user or admin to delete a specific card by its ID")
    public ResponseEntity<AppResponse<String>> delete(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.delete(cardId));
    }

    /**
     * Filter cards using criteria provided in DTO with pagination.
     *
     * @param dto  Filtering criteria
     * @param page Page number (default 1)
     * @param size Page size (default 5)
     * @return Paginated list of filtered CardResponseDTO
     */
    @PostMapping("/filter")
    @Operation(summary = "Filter cards with pagination", description = "Returns a paginated list of cards based on filtering criteria")
    public ResponseEntity<PageImpl<CardResponseDTO>> filter(@RequestBody @Valid CardFilterDTO dto,
                                                            @RequestParam(value = "page", defaultValue = "1") int page,
                                                            @RequestParam(value = "size", defaultValue = "5") int size) {
        return ResponseEntity.ok(cardService.filter(dto, PageUtil.getCurrentPage(page), size));
    }

    /**
     * View the balance of a specific card.
     *
     * @param cardId UUID of the card
     * @return CardResponseDTO containing balance information
     */
    @GetMapping("/balance/{cardId}")
    @Operation(summary = "View Balance", description = "User can see balance of card")
    public ResponseEntity<CardResponseDTO> getCardBalance(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId));
    }

    /**
     * Request to block a card.
     *
     * @param cardId UUID of the card
     * @return AppResponse with operation result
     */
    @PutMapping("/block-request/{cardId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Request card block", description = "User requests to block their card")
    public ResponseEntity<AppResponse<String>> requestBlock(@PathVariable UUID cardId) {
        log.info("Request card block requested by ID: {}", cardId);
        return ResponseEntity.ok(cardService.requestCardBlock(cardId));
    }
}
