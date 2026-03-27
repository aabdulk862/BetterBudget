package com.revature.project2.controllers;

import com.revature.project2.models.DTOs.PaginatedResponse;
import com.revature.project2.models.EnvelopeHistory;
import com.revature.project2.services.EnvelopeHistoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/envelopes/history")
public class EnvelopHistoryController {
    private final EnvelopeHistoryService envelopeHistoryService;

    public EnvelopHistoryController(EnvelopeHistoryService envelopeHistoryService) {
        this.envelopeHistoryService = envelopeHistoryService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<EnvelopeHistory>> getAllEnvelopeHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EnvelopeHistory> histories = envelopeHistoryService.getAllEnvelopeHistory(PageRequest.of(page, size));
        return ResponseEntity.ok(PaginatedResponse.from(histories));
    }

    @GetMapping("/{envelopeId}")
    public ResponseEntity<List<EnvelopeHistory>> getEnvelopeHistoryByEnvelopeId(@PathVariable Integer envelopeId) {
        List<EnvelopeHistory> histories = envelopeHistoryService.getEnvelopeHistoryByEnvelopeId(envelopeId);
        return ResponseEntity.ok(histories);
    }

    @PostMapping
    public ResponseEntity<EnvelopeHistory> createEnvelopeHistory(@Valid @RequestBody EnvelopeHistory envelopeHistory) {
        EnvelopeHistory saved = envelopeHistoryService.createEnvelopeHistory(envelopeHistory);
        return ResponseEntity.ok(saved);
    }
}
