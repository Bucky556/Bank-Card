package code.uz.bankcard.controller;


import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.card.CardAdminUpdateDTO;
import code.uz.bankcard.dto.card.CardResponseDTO;
import code.uz.bankcard.dto.card.CardStatusDTO;
import code.uz.bankcard.service.Impl.CardServiceImpl;
import code.uz.bankcard.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for administrative management of bank cards.
 * <p>
 * Provides endpoints that allow admins to perform CRUD operations
 * and manage the status of cards. All APIs under this controller
 * require the ROLE_ADMIN authority.
 * </p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>View all cards with pagination</li>
 *     <li>Retrieve card details by ID</li>
 *     <li>Update card information</li>
 *     <li>Change card status (ACTIVE/BLOCKED)</li>
 * </ul>
 *
 * @author Nodir
 * @version 1.0
 * @since 2025-10-25
 */

@RestController
@RequestMapping("/api/v1/admin/card")
@RequiredArgsConstructor
@EnableMethodSecurity
@Tag(name = "Admin Card Management", description = "APIs for admins to manage bank cards")
@Slf4j
public class AdminCardController {
    private final CardServiceImpl cardService;


    @PutMapping("/{cardId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Update a card",
            description = "Allows an admin to update card details such as status or expiration date"
    )
    public ResponseEntity<CardResponseDTO> update(@RequestBody @Valid CardAdminUpdateDTO dto, @PathVariable UUID cardId) {
        log.info("Card updated with id: {}", cardId);
        return ResponseEntity.ok(cardService.updateCard(dto, cardId));
    }

    @GetMapping("/list")
    @Operation(
            summary = "Get all cards with pagination",
            description = "Returns a paginated list of all cards for administrative purposes"
    )
    public ResponseEntity<Page<CardResponseDTO>> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "3") int size
    ) {
        return ResponseEntity.ok(cardService.getAllByAdmin(PageUtil.getCurrentPage(page), size));
    }

    @GetMapping("/{cardID}")
    @Operation(
            summary = "Get card by ID",
            description = "Returns details of a specific card by its ID"
    )
    public ResponseEntity<CardResponseDTO> getCardById(@PathVariable UUID cardID) {
        return ResponseEntity.ok(cardService.getCardByAdmin(cardID));
    }

    @PutMapping("/status/{cardId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Change status of cards",
            description = "This API allows admin to change status of cards"
    )
    public ResponseEntity<AppResponse<String>> changeStatus(@PathVariable("cardId") UUID cardId,
                                                            @RequestBody CardStatusDTO dto) {
        log.info("Card status changed with id: {}", cardId);
        return ResponseEntity.ok(cardService.changeStatus(cardId, dto.getStatus()));
    }
}
