package code.uz.bankcard.controller;

import code.uz.bankcard.dto.transaction.TransactionCreateDTO;
import code.uz.bankcard.dto.transaction.TransactionResponseDTO;
import code.uz.bankcard.service.Impl.TransactionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for managing transactions and money transfers.
 * <p>
 * Provides endpoints for:
 * <ul>
 *     <li>Users to transfer money between their own cards</li>
 *     <li>Admins to transfer money between any two cards</li>
 *     <li>Users to view their transaction history</li>
 *     <li>Admins to view all transactions</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@EnableMethodSecurity
@Tag(name = "Transactions", description = "APIs for managing transactions and transfers")
@Slf4j
public class TransactionController {

    private final TransactionServiceImpl transactionService;

    /**
     * Transfers money between user's own cards.
     *
     * @param dto TransactionCreateDTO containing source card, destination card, and amount
     * @return ResponseEntity containing TransactionResponseDTO with transaction details
     */
    @PostMapping("/send")
    @Operation(
            summary = "Transfer money between cards",
            description = "Allows a user to transfer money from their own card to another card."
    )
    public ResponseEntity<TransactionResponseDTO> transfer(@RequestBody @Valid TransactionCreateDTO dto) {
        log.info("Transfer transaction request: {}", dto);
        return ResponseEntity.ok(transactionService.transfer(dto));
    }

    /**
     * Admin transfers money between any two cards.
     *
     * @param dto TransactionCreateDTO containing source card, destination card, and amount
     * @return ResponseEntity containing TransactionResponseDTO with transaction details
     */
    @PostMapping("/admin/send")
    @Operation(
            summary = "Admin transfer between any cards",
            description = "Allows an admin to transfer money between any two cards."
    )
    public ResponseEntity<TransactionResponseDTO> transferAdmin(@RequestBody @Valid TransactionCreateDTO dto) {
        log.info("Admin transfer transaction request: {}", dto);
        return ResponseEntity.ok(transactionService.transferByAdmin(dto));
    }

    /**
     * Retrieves transaction history for the logged-in user.
     *
     * @param page Page number (default is 1)
     * @param size Number of records per page (default is 10)
     * @return ResponseEntity containing a paginated list of TransactionResponseDTO
     */
    @GetMapping("/history")
    @Operation(
            summary = "View history",
            description = "Allows user to view transaction history"
    )
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactionHistory(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getAllHistory(page, size));
    }

    /**
     * Admins retrieve all transaction histories.
     *
     * @param page Page number (default is 1)
     * @param size Number of records per page (default is 10)
     * @return ResponseEntity containing a paginated list of TransactionResponseDTO
     */
    @GetMapping("/admin/transactions")
    @Operation(
            summary = "View Transaction by admins",
            description = "Allows admins to view all transaction history"
    )
    public ResponseEntity<Page<TransactionResponseDTO>> getAdminTransactions(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByAdmin(page, size));
    }
}
