package com.revature.project2.services;

import com.revature.project2.exceptions.BusinessException;
import com.revature.project2.models.DTOs.TransactionDTO;
import com.revature.project2.models.Envelope;
import com.revature.project2.models.Transaction;
import com.revature.project2.models.mappers.TransactionDTOMapper;
import com.revature.project2.repositories.EnvelopeRepository;
import com.revature.project2.repositories.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final EnvelopeRepository envelopeRepository;
    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionDTOMapper transactionDTOMapper;

    public TransactionService(TransactionRepository transactionRepository, EnvelopeRepository envelopeRepository, TransactionDTOMapper transactionDTOMapper) {
        this.transactionRepository = transactionRepository;
        this.envelopeRepository = envelopeRepository;
        this.transactionDTOMapper = transactionDTOMapper;
    }

    private void verifyOwnership(Envelope envelope, String authenticatedUsername) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return;
        }
        if (!envelope.getUser().getUsername().equals(authenticatedUsername)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public Transaction updateTransactionTitle(Integer id, String newTitle) {
        logger.info("Updating transaction title for transaction with id: " + id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(2001, "Transaction not found"));
        transaction.setTitle(newTitle);
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransactionDescription(Integer id, String newDescription) {
        logger.info("Updating transaction description for transaction with id: " + id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(2001, "Transaction not found"));
        transaction.setTransactionDescription(newDescription);
        return transactionRepository.save(transaction);
    }

    public Transaction createTransaction(Transaction transaction) {
        logger.info("Creating transaction: " + transaction);
        if (transaction.getTitle() == null || transaction.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Transaction title cannot be null or empty");
        }
        if (transaction.getTransactionDescription() == null || transaction.getTransactionDescription().isEmpty()) {
            throw new IllegalArgumentException("Transaction description cannot be null or empty");
        }
        //Additional business rule checks can be added here

        try {
            return transactionRepository.save(transaction);
        } catch (Exception e) {
            throw new BusinessException(2002, "Failed to save transaction: " + e.getMessage());
        }
    }

    public Page<Transaction> getAllTransactions(Pageable pageable) {
        logger.info("Retrieving all transactions");
        return transactionRepository.findAll(pageable);
    }

    public Transaction updateTransactionCategory(Integer id, String newCategory) {
        logger.info("Updating transaction Category for transaction with id: " + id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(654, "Transaction not found"));
        if (newCategory == null || newCategory.isEmpty()) {
            throw new BusinessException(607,"Transaction category cannot be null or empty");
        }
        transaction.setCategory(newCategory);
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByEnvelopeId(Integer envelopeId, String authenticatedUsername) {
        logger.info("Retrieving transaction by envelope id: " + envelopeId);
        Envelope envelope = envelopeRepository.findById(envelopeId)
                .orElseThrow(() -> new BusinessException(1001, "Envelope not found"));
        verifyOwnership(envelope, authenticatedUsername);
        return transactionRepository.findByEnvelope_EnvelopeId(envelopeId);
    }

}