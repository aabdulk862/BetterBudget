package com.revature.project2.services;

import com.revature.project2.exceptions.BusinessException;
import com.revature.project2.models.DTOs.EnvelopeDTO;
import com.revature.project2.models.DTOs.TransferFundDTO;
import com.revature.project2.models.Envelope;
import com.revature.project2.models.EnvelopeHistory;
import com.revature.project2.models.Transaction;
import com.revature.project2.models.User;
import com.revature.project2.repositories.EnvelopeRepository;
import com.revature.project2.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnvelopeService {
    private final Logger logger = LoggerFactory.getLogger(EnvelopeService.class);
    private final EnvelopeRepository envelopeRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final EnvelopeHistoryService envelopeHistoryService;

    public EnvelopeService(EnvelopeRepository envelopeRepository, UserRepository userRepository,
                           TransactionService transactionService, EnvelopeHistoryService envelopeHistoryService) {
        this.envelopeRepository = envelopeRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.envelopeHistoryService = envelopeHistoryService;
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

    public Envelope createEnvelope(EnvelopeDTO envelopeDTO) {
        logger.info("Creating envelope: {}", envelopeDTO);
        if (envelopeDTO.envelopeDescription().isEmpty() || envelopeDTO.balance() == null ||
                envelopeDTO.maxLimit() == null || envelopeDTO.userId() == null) {
            throw new BusinessException(1005, "Envelope fields cannot be null");
        }
        if (envelopeDTO.balance().compareTo(BigDecimal.ZERO) < 0 || envelopeDTO.maxLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(1006, "Amount and max limit cannot be negative");
        }
        Optional<User> user = userRepository.findById(envelopeDTO.userId());
        if (user.isEmpty()) {
            throw new BusinessException(1007, "User does not exist");
        }

        Envelope envelope = new Envelope();
        envelope.setEnvelopeDescription(envelopeDTO.envelopeDescription());
        envelope.setBalance(envelopeDTO.balance());
        envelope.setMaxLimit(envelopeDTO.maxLimit());
        envelope.setUser(user.get());

        logger.info("Creating envelope: " + envelope);

        Envelope savedEnvelope = envelopeRepository.save(envelope);
        savedEnvelope.getUser().setPassword(null);

        return savedEnvelope;
    }

    public Envelope getEnvelopeById(Integer id, String authenticatedUsername) {
        logger.info("Retrieving envelope with id: {}", id);
        Optional<Envelope> envelope = envelopeRepository.findById(id);
        if (envelope.isEmpty()) {
            throw new BusinessException(1001, "Envelope not found with id: " + id);
        }
        verifyOwnership(envelope.get(), authenticatedUsername);
        envelope.get().getUser().setPassword(null);

        return envelope.get();
    }

    public Page<Envelope> getAllEnvelopes(Pageable pageable) {
        Page<Envelope> envelopes = envelopeRepository.findAll(pageable);
        logger.info("Retrieving all envelopes, Envelope count: {}", envelopes.getTotalElements());
        envelopes.forEach(envelope -> envelope.getUser().setPassword(null));
        return envelopes;
    }

    public void deleteEnvelope(Integer id, String authenticatedUsername) {
        logger.info("Deleting envelope with id: {}", id);
        Optional<Envelope> envelope = envelopeRepository.findById(id);
        if (envelope.isEmpty()) {
            throw new BusinessException(1001, "Envelope not found with id: " + id);
        }
        verifyOwnership(envelope.get(), authenticatedUsername);
        envelopeRepository.delete(envelope.get());
    }

    @Transactional
    public void transferEnvelope(TransferFundDTO transferFundDTO, String authenticatedUsername) {
        logger.info("Transferring amount from envelope with id: {} to envelope with id: {}",
                transferFundDTO.fromId(), transferFundDTO.toId());
        Optional<Envelope> fromEnvelope = envelopeRepository.findById(transferFundDTO.fromId());
        Optional<Envelope> toEnvelope = envelopeRepository.findById(transferFundDTO.toId());
        if (fromEnvelope.isEmpty() || toEnvelope.isEmpty()) {
            throw new BusinessException(1001, "Envelope not found");
        }
        verifyOwnership(fromEnvelope.get(), authenticatedUsername);
        verifyOwnership(toEnvelope.get(), authenticatedUsername);
        if (transferFundDTO.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(1004, "Amount must be greater than 0");
        }
        if (fromEnvelope.get().getBalance().compareTo(transferFundDTO.amount()) < 0) {
            throw new BusinessException(1002, "Insufficient funds in envelope with id: " + transferFundDTO.fromId());
        }

        if (toEnvelope.get().getBalance().add(transferFundDTO.amount()).compareTo(toEnvelope.get().getMaxLimit()) > 0) {
            throw new BusinessException(1003, "Amount exceeds max limit of envelope with id: " + transferFundDTO.toId());
        }

        fromEnvelope.get().setBalance(fromEnvelope.get().getBalance().subtract(transferFundDTO.amount()));
        toEnvelope.get().setBalance(toEnvelope.get().getBalance().add(transferFundDTO.amount()));

        envelopeRepository.save(fromEnvelope.get());
        envelopeRepository.save(toEnvelope.get());

        Transaction fromTransaction = new Transaction();
        fromTransaction.setTitle(transferFundDTO.transactionTitle());
        fromTransaction.setTransactionDescription(transferFundDTO.transactionDescription());
        fromTransaction.setEnvelope(fromEnvelope.get());
        fromTransaction.setDatetime(LocalDateTime.now());
        fromTransaction.setCategory("Envelope Fund Transfer");

        // Make the transaction amount negative to indicate spending on frontend
        fromTransaction.setTransactionAmount(transferFundDTO.amount().negate());

        fromTransaction.setEnvelope(fromEnvelope.get());
        transactionService.createTransaction(fromTransaction);

        Transaction toTransaction = new Transaction();
        toTransaction.setTitle(transferFundDTO.transactionTitle());
        toTransaction.setTransactionDescription(transferFundDTO.transactionDescription());
        toTransaction.setEnvelope(fromEnvelope.get());
        toTransaction.setDatetime(LocalDateTime.now());
        toTransaction.setCategory("Envelope Fund Transfer");
        toTransaction.setTransactionAmount(transferFundDTO.amount());
        toTransaction.setEnvelope(toEnvelope.get());
        transactionService.createTransaction(toTransaction);

        EnvelopeHistory fromEnvelopeHistory = new EnvelopeHistory();
        fromEnvelopeHistory.setEnvelope(fromEnvelope.get());
        fromEnvelopeHistory.setEnvelopeAmount(transferFundDTO.amount());
        fromEnvelopeHistory.setTransaction(fromTransaction);
        envelopeHistoryService.createEnvelopeHistory(fromEnvelopeHistory);

        EnvelopeHistory toEnvelopeHistory = new EnvelopeHistory();
        toEnvelopeHistory.setEnvelope(toEnvelope.get());
        toEnvelopeHistory.setEnvelopeAmount(transferFundDTO.amount());
        toEnvelopeHistory.setTransaction(toTransaction);
        envelopeHistoryService.createEnvelopeHistory(toEnvelopeHistory);
    }

    @Transactional
    public Transaction allocateMoney(Integer envelopeId, Transaction transaction, String authenticatedUsername) {
        logger.info("Allocating amount to envelope with id: {}", envelopeId);
        Optional<Envelope> envelope = envelopeRepository.findById(envelopeId);
        if (envelope.isEmpty()) {
            throw new BusinessException(1001, "Envelope not found with id: " + envelopeId);
        }
        verifyOwnership(envelope.get(), authenticatedUsername);
        if (transaction.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(1004, "Amount must be greater than 0");
        }
        if (envelope.get().getBalance().add(transaction.getTransactionAmount()).compareTo(envelope.get().getMaxLimit()) > 0) {
            throw new BusinessException(1003, "Amount exceeds max limit of envelope with id: " + envelopeId);
        }

        envelope.get().setBalance(envelope.get().getBalance().add(transaction.getTransactionAmount()));
        envelopeRepository.save(envelope.get());

        transaction.setEnvelope(envelope.get());
        transaction.setDatetime(LocalDateTime.now());
        transaction.setCategory(transaction.getCategory());
        Transaction savedTransaction = transactionService.createTransaction(transaction);

        EnvelopeHistory currentEnvelopeHistory = new EnvelopeHistory();
        currentEnvelopeHistory.setEnvelope(envelope.get());
        currentEnvelopeHistory.setEnvelopeAmount(envelope.get().getBalance());
        currentEnvelopeHistory.setTransaction(savedTransaction);
        envelopeHistoryService.createEnvelopeHistory(currentEnvelopeHistory);

        return savedTransaction;
    }

    @Transactional
    public Transaction spendMoney(Integer envelopeId, Transaction transaction, String authenticatedUsername) {
        logger.info("Spending amount from envelope with id: {}", envelopeId);
        Optional<Envelope> envelope = envelopeRepository.findById(envelopeId);
        if (envelope.isEmpty()) {
            throw new BusinessException(1001, "Envelope not found with id: " + envelopeId);
        }
        verifyOwnership(envelope.get(), authenticatedUsername);
        if (transaction.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(1004, "Amount must be greater than 0");
        }
        if (envelope.get().getBalance().compareTo(transaction.getTransactionAmount()) < 0) {
            throw new BusinessException(1002, "Insufficient funds in envelope with id: " + envelopeId);
        }

        envelope.get().setBalance(envelope.get().getBalance().subtract(transaction.getTransactionAmount()));
        envelopeRepository.save(envelope.get());

        transaction.setEnvelope(envelope.get());
        transaction.setDatetime(LocalDateTime.now());
        transaction.setCategory(transaction.getCategory());
        // Make the transaction amount negative to indicate spending on frontend
        transaction.setTransactionAmount(transaction.getTransactionAmount().negate());
        Transaction savedTransaction = transactionService.createTransaction(transaction);

        EnvelopeHistory currentEnvelopeHistory = new EnvelopeHistory();
        currentEnvelopeHistory.setEnvelope(envelope.get());
        currentEnvelopeHistory.setEnvelopeAmount(envelope.get().getBalance());
        currentEnvelopeHistory.setTransaction(savedTransaction);
        envelopeHistoryService.createEnvelopeHistory(currentEnvelopeHistory);

        return savedTransaction;
    }

    public List<Envelope> getEnvelopeByUserId(Integer userId) {
        logger.info("Retrieving envelope by user id: {}", userId);
        //Check if user exists
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new BusinessException(1007, "User does not exist");
        }
        List<Envelope> envelopes = envelopeRepository.findByUser_UserId(userId);
        envelopes.forEach(envelope -> envelope.getUser().setPassword(null));
        return envelopes;
    }
}
