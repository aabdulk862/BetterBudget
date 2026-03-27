package com.revature.project2.controllers;

import com.revature.project2.models.DTOs.PaginatedResponse;
import com.revature.project2.models.DTOs.TransactionDTO;
import com.revature.project2.models.Transaction;
import com.revature.project2.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PatchMapping("/transactions/title/{id}")
    public ResponseEntity<?> updateTransactionTitle(@PathVariable Integer id, @RequestBody String newTitle) {
        try {
            return ResponseEntity.ok(transactionService.updateTransactionTitle(id, newTitle));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PatchMapping("/transactions/description/{id}")
    public ResponseEntity<?> updateTransactionDescription(@PathVariable Integer id, @RequestBody String newDescription) {
        try {
            return ResponseEntity.ok(transactionService.updateTransactionDescription(id, newDescription));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody Transaction transaction) {
        try {
            return ResponseEntity.ok(transactionService.createTransaction(transaction));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    //TODO: Spring Security
    @GetMapping("/transactions")
    public ResponseEntity<PaginatedResponse<Transaction>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Transaction> transactions = transactionService.getAllTransactions(PageRequest.of(page, size));
        return ResponseEntity.ok(PaginatedResponse.from(transactions));
    }

    @PatchMapping("/transactions/category/{id}")
    public ResponseEntity<?> updateTransactionCategory(@PathVariable Integer id, @RequestBody String newCategory) {

        // Call the service layer to update the transaction category and return the updated transaction
        return ResponseEntity.ok(transactionService.updateTransactionCategory(id, newCategory));
    }

    @GetMapping("/transactions/envelope/{envelopeId}")
    public ResponseEntity<List<Transaction>> getTransactionsByEnvelopeId(@PathVariable Integer envelopeId, Authentication authentication) {
        List<Transaction> transactions = transactionService.getTransactionsByEnvelopeId(envelopeId, authentication.getName());
        return ResponseEntity.ok(transactions);
    }


}