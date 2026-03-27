package com.revature.project2.controllers;

import com.revature.project2.models.DTOs.EnvelopeDTO;
import com.revature.project2.models.DTOs.PaginatedResponse;
import com.revature.project2.models.DTOs.TransferFundDTO;
import com.revature.project2.models.Envelope;
import com.revature.project2.models.Transaction;
import com.revature.project2.services.EnvelopeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/envelopes")
public class EnvelopeController {
    private final EnvelopeService envelopeService;

    public EnvelopeController(EnvelopeService envelopeService) {
        this.envelopeService = envelopeService;
    }

    @PostMapping
    public ResponseEntity<Envelope> createEnvelope(@Valid @RequestBody EnvelopeDTO envelopeDTO) {
        Envelope envelope = envelopeService.createEnvelope(envelopeDTO);
        return ResponseEntity.ok(envelope);
    }

    @GetMapping
    public ResponseEntity<?> getEnvelopes(
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (userId != null) {
            List<Envelope> envelopes = envelopeService.getEnvelopeByUserId(userId);
            return ResponseEntity.ok(envelopes);
        }
        Page<Envelope> envelopes = envelopeService.getAllEnvelopes(PageRequest.of(page, size));
        return ResponseEntity.ok(PaginatedResponse.from(envelopes));
    }

    @GetMapping("/{envelopeId}")
    public ResponseEntity<Envelope> getEnvelopeById(@PathVariable Integer envelopeId, Authentication authentication) {
        Envelope envelope = envelopeService.getEnvelopeById(envelopeId, authentication.getName());
        return ResponseEntity.ok(envelope);
    }

    @DeleteMapping("/{envelopeId}")
    public ResponseEntity<Void> deleteEnvelope(@PathVariable Integer envelopeId, Authentication authentication) {
        envelopeService.deleteEnvelope(envelopeId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferEnvelope(@Valid @RequestBody TransferFundDTO transferFundDTO, Authentication authentication) {
        envelopeService.transferEnvelope(transferFundDTO, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/allocate/{envelopeId}")
    public ResponseEntity<Transaction> allocateMoney(@PathVariable Integer envelopeId, @RequestBody Transaction transaction, Authentication authentication) {
        Transaction result = envelopeService.allocateMoney(envelopeId, transaction, authentication.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/spend/{envelopeId}")
    public ResponseEntity<Transaction> spendMoney(@PathVariable Integer envelopeId, @RequestBody Transaction transaction, Authentication authentication) {
        Transaction result = envelopeService.spendMoney(envelopeId, transaction, authentication.getName());
        return ResponseEntity.ok(result);
    }
}
