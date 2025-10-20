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


@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@EnableMethodSecurity
@Tag(name = "Transactions", description = "APIs for managing transactions and transfers")
@Slf4j
public class TransactionController {
    private final TransactionServiceImpl transactionService;

    @PostMapping("/send")
    @Operation(
            summary = "Transfer money between cards",
            description = "Allows a user to transfer money from their own card to another card."
    )
    public ResponseEntity<TransactionResponseDTO> transfer(@RequestBody @Valid TransactionCreateDTO dto) {
        log.info("Transfer transaction request: {}", dto);
        return ResponseEntity.ok(transactionService.transfer(dto));
    }

    @PostMapping("/admin/send")
    @Operation(
            summary = "Admin transfer between any cards",
            description = "Allows an admin to transfer money between any two cards."
    )
    public ResponseEntity<TransactionResponseDTO> transferAdmin(@RequestBody @Valid TransactionCreateDTO dto) {
        log.info("Admin transfer transaction request: {}", dto);
        return ResponseEntity.ok(transactionService.transferByAdmin(dto));
    }

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
