package com.revature.project2.services;

import com.revature.project2.models.EnvelopeHistory;
import com.revature.project2.repositories.EnvelopeHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvelopeHistoryService {
    private final EnvelopeHistoryRepository envelopeHistoryRepository;
    Logger logger = LoggerFactory.getLogger(EnvelopeHistoryService.class);

    public EnvelopeHistoryService(EnvelopeHistoryRepository envelopeHistoryRepository) {
        this.envelopeHistoryRepository = envelopeHistoryRepository;
    }

    public Page<EnvelopeHistory> getAllEnvelopeHistory(Pageable pageable) {
        logger.info("Retrieving all envelope history");
        return envelopeHistoryRepository.findAll(pageable);
    }

    public List<EnvelopeHistory> getEnvelopeHistoryByEnvelopeId(Integer envelopeId) {
        logger.info("Retrieving envelope history by id: " + envelopeId);
        List<EnvelopeHistory> envelopeHistory = envelopeHistoryRepository.findByEnvelope_EnvelopeId(envelopeId);
        logger.info("Retrieved envelope history with id: " + envelopeHistory);
        return envelopeHistory;
    }

    public EnvelopeHistory createEnvelopeHistory(EnvelopeHistory envelopeHistory) {
        logger.info("Creating envelope history: " + envelopeHistory);
        return envelopeHistoryRepository.save(envelopeHistory);
    }
}
