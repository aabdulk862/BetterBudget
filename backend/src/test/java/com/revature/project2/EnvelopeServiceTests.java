package com.revature.project2;

import com.revature.project2.exceptions.BusinessException;
import com.revature.project2.models.DTOs.EnvelopeDTO;
import com.revature.project2.models.DTOs.TransferFundDTO;
import com.revature.project2.models.Envelope;
import com.revature.project2.models.EnvelopeHistory;
import com.revature.project2.models.Transaction;
import com.revature.project2.models.User;
import com.revature.project2.repositories.EnvelopeRepository;
import com.revature.project2.repositories.UserRepository;
import com.revature.project2.services.EnvelopeHistoryService;
import com.revature.project2.services.EnvelopeService;
import com.revature.project2.services.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class EnvelopeServiceTests {
    private EnvelopeRepository envelopeRepository;
    private UserRepository userRepository;
    private TransactionService transactionService;
    private EnvelopeHistoryService envelopeHistoryService;
    private EnvelopeService envelopeService;

    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    public void loadContext(){
        envelopeRepository = Mockito.mock(EnvelopeRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        transactionService = Mockito.mock(TransactionService.class);
        envelopeHistoryService = Mockito.mock(EnvelopeHistoryService.class);

        envelopeService = new EnvelopeService(envelopeRepository, userRepository, transactionService, envelopeHistoryService);

        // Set up SecurityContext with ROLE_EMPLOYEE for ownership checks
        var auth = new UsernamePasswordAuthenticationToken(
                TEST_USERNAME, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void test_createEnvelope_valid(){
        EnvelopeDTO envelopeDTO = new EnvelopeDTO(100, "100", new BigDecimal("100.00"), new BigDecimal("100.00"));
        User user = new User();
        user.setUserId(100);
        when(userRepository.findById(100)).thenReturn(Optional.of(user));

        ArgumentCaptor<Envelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(Envelope.class);

        Envelope outEnvelope = new Envelope();
        outEnvelope.setEnvelopeDescription("Saved Envelope");
        outEnvelope.setUser(user);
        when(envelopeRepository.save(envelopeArgumentCaptor.capture())).thenReturn(outEnvelope);

        Envelope result = envelopeService.createEnvelope(envelopeDTO);

        Assertions.assertEquals("Saved Envelope", result.getEnvelopeDescription());
        Assertions.assertEquals(100, result.getUser().getUserId());

        Envelope savedEnvelope = envelopeArgumentCaptor.getValue();
        Assertions.assertEquals(new BigDecimal("100.00"), savedEnvelope.getBalance());
        Assertions.assertEquals(new BigDecimal("100.00"), savedEnvelope.getMaxLimit());
        Assertions.assertEquals("100", savedEnvelope.getEnvelopeDescription());
        Assertions.assertEquals(100, savedEnvelope.getUser().getUserId());
    }

    @Test
    public void test_createEnvelope_invalid_emptyDescription(){
        EnvelopeDTO envelopeDTO = new EnvelopeDTO(100, "", new BigDecimal("100.00"), new BigDecimal("100.00"));
        User user = new User();
        user.setUserId(100);
        when(userRepository.findById(100)).thenReturn(Optional.of(user));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.createEnvelope(envelopeDTO));
        verifyNoInteractions(envelopeRepository);
    }

    @Test
    public void test_createEnvelope_invalid_negativeBalance(){
        EnvelopeDTO envelopeDTO = new EnvelopeDTO(100, "100", new BigDecimal("-1.00"), new BigDecimal("100.00"));
        User user = new User();
        user.setUserId(100);
        when(userRepository.findById(100)).thenReturn(Optional.of(user));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.createEnvelope(envelopeDTO));
        verifyNoInteractions(envelopeRepository);
    }

    @Test
    public void test_createEnvelope_invalid_negativeMaxLimit(){
        EnvelopeDTO envelopeDTO = new EnvelopeDTO(100, "100", new BigDecimal("100.00"), new BigDecimal("-1.00"));
        User user = new User();
        user.setUserId(100);
        when(userRepository.findById(100)).thenReturn(Optional.of(user));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.createEnvelope(envelopeDTO));
        verifyNoInteractions(envelopeRepository);
    }

    @Test
    public void test_createEnvelope_invalid_userNotFound(){
        EnvelopeDTO envelopeDTO = new EnvelopeDTO(0, "100", new BigDecimal("100.00"), new BigDecimal("100.00"));
        when(userRepository.findById(0)).thenReturn(Optional.empty());

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.createEnvelope(envelopeDTO));
        verifyNoInteractions(envelopeRepository);
    }

    @Test
    public void test_getEnvelopeById(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("100.00"));

        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));
        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());

        Envelope result = envelopeService.getEnvelopeById(100, TEST_USERNAME);

        Assertions.assertEquals(new BigDecimal("100.00"), result.getBalance());
        Assertions.assertEquals(new BigDecimal("100.00"), result.getMaxLimit());
        Assertions.assertEquals("100", result.getEnvelopeDescription());
        Assertions.assertEquals(100, result.getUser().getUserId());

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.getEnvelopeById(0, TEST_USERNAME));
    }

    @Test
    public void test_getAllEnvelopes(){
        List<Envelope> envList = new ArrayList<>();

        User user = new User();
        user.setUserId(100);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("100.00"));

        envList.add(envelope);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Envelope> envelopePage = new PageImpl<>(envList, pageable, envList.size());
        when(envelopeRepository.findAll(pageable)).thenReturn(envelopePage);

        Page<Envelope> result = envelopeService.getAllEnvelopes(pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(new BigDecimal("100.00"), result.getContent().get(0).getBalance());
        Assertions.assertEquals(new BigDecimal("100.00"), result.getContent().get(0).getMaxLimit());
        Assertions.assertEquals("100", result.getContent().get(0).getEnvelopeDescription());
        Assertions.assertEquals(100, result.getContent().get(0).getUser().getUserId());
    }

    @Test
    public void test_deleteEnvelope(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("100.00"));

        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));
        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        ArgumentCaptor<Envelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(Envelope.class);
        doNothing().when(envelopeRepository).delete(envelopeArgumentCaptor.capture());

        envelopeService.deleteEnvelope(100, TEST_USERNAME);

        Envelope deletedEnvelope = envelopeArgumentCaptor.getValue();
        Assertions.assertEquals(new BigDecimal("100.00"), deletedEnvelope.getBalance());
        Assertions.assertEquals(new BigDecimal("100.00"), deletedEnvelope.getMaxLimit());
        Assertions.assertEquals("100", deletedEnvelope.getEnvelopeDescription());
        Assertions.assertEquals(100, deletedEnvelope.getUser().getUserId());

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.deleteEnvelope(0, TEST_USERNAME));
    }

    @Test
    public void test_transferEnvelope_valid(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelopeFrom = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));
        Envelope envelopeTo = new Envelope(200, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));
        TransferFundDTO transferFundDTO = new TransferFundDTO(100, 200, "TransactionTitle", "TransactionDesc", new BigDecimal("100.00"));

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelopeFrom));
        when(envelopeRepository.findById(200)).thenReturn(Optional.of(envelopeTo));

        ArgumentCaptor<Envelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(Envelope.class);
        ArgumentCaptor<EnvelopeHistory> envelopeHistoryArgumentCaptor = ArgumentCaptor.forClass(EnvelopeHistory.class);
        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);

        envelopeService.transferEnvelope(transferFundDTO, TEST_USERNAME);

        Mockito.verify(envelopeRepository, Mockito.times(2)).save(envelopeArgumentCaptor.capture());
        List<Envelope> envelopeList = envelopeArgumentCaptor.getAllValues();

        if ((envelopeList.get(0).getEnvelopeId()==200)&&(envelopeList.get(1).getEnvelopeId()==100)){
            Collections.swap(envelopeList,0,1);
        }
        if (!(envelopeList.get(0).getEnvelopeId()==100)||!(envelopeList.get(1).getEnvelopeId()==200)){
            Assertions.fail("Envelopes not saved correctly");
        }
        Envelope envFrom = envelopeList.get(0);
        Envelope envTo = envelopeList.get(1);
        Assertions.assertEquals(BigDecimal.ZERO.setScale(2), envFrom.getBalance());
        Assertions.assertEquals(new BigDecimal("200.00"), envTo.getBalance());

        Mockito.verify(transactionService, Mockito.times(2)).createTransaction(transactionArgumentCaptor.capture());
        List<Transaction> transactionList = transactionArgumentCaptor.getAllValues();

        if ((transactionList.get(0).getEnvelope().getEnvelopeId()==200)&&(transactionList.get(1).getEnvelope().getEnvelopeId()==100)){
            Collections.swap(transactionList,0,1);
        }
        if (!(transactionList.get(0).getEnvelope().getEnvelopeId()==100)||!(transactionList.get(1).getEnvelope().getEnvelopeId()==200)){
            Assertions.fail("Transactions not created correctly");
        }
        Transaction transTo = transactionList.get(0);
        Transaction transFrom = transactionList.get(1);
        Assertions.assertEquals(new BigDecimal("-100.00"), transTo.getTransactionAmount());
        Assertions.assertEquals("TransactionTitle", transTo.getTitle());
        Assertions.assertEquals("TransactionDesc", transTo.getTransactionDescription());
        Assertions.assertEquals(new BigDecimal("100.00"), transFrom.getTransactionAmount());
        Assertions.assertEquals("TransactionTitle", transFrom.getTitle());
        Assertions.assertEquals("TransactionDesc", transFrom.getTransactionDescription());

        Mockito.verify(envelopeHistoryService, Mockito.times(2)).createEnvelopeHistory(envelopeHistoryArgumentCaptor.capture());
    }

    @Test
    public void test_transferEnvelope_invalid_noFrom(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelopeTo = new Envelope(200, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));
        TransferFundDTO transferFundDTO = new TransferFundDTO(0, 200, "TransactionTitle", "TransactionDesc", new BigDecimal("100.00"));

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(200)).thenReturn(Optional.of(envelopeTo));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.transferEnvelope(transferFundDTO, TEST_USERNAME));
        Mockito.verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
        Mockito.verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        Mockito.verify(envelopeHistoryService, never()).createEnvelopeHistory(Mockito.any(EnvelopeHistory.class));
    }

    @Test
    public void test_transferEnvelope_invalid_noTo(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelopeFrom = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));
        TransferFundDTO transferFundDTO = new TransferFundDTO(100, 0, "TransactionTitle", "TransactionDesc", new BigDecimal("100.00"));

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelopeFrom));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.transferEnvelope(transferFundDTO, TEST_USERNAME));
        Mockito.verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
        Mockito.verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        Mockito.verify(envelopeHistoryService, never()).createEnvelopeHistory(Mockito.any(EnvelopeHistory.class));
    }

    @Test
    public void test_transferEnvelope_invalid_notEnoughBalance(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelopeFrom = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));
        Envelope envelopeTo = new Envelope(200, user, "100", new BigDecimal("100.00"), new BigDecimal("300.00"));
        TransferFundDTO transferFundDTO = new TransferFundDTO(100, 200, "TransactionTitle", "TransactionDesc", new BigDecimal("200.00"));

        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelopeFrom));
        when(envelopeRepository.findById(200)).thenReturn(Optional.of(envelopeTo));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.transferEnvelope(transferFundDTO, TEST_USERNAME));
        Mockito.verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
        Mockito.verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        Mockito.verify(envelopeHistoryService, never()).createEnvelopeHistory(Mockito.any(EnvelopeHistory.class));
    }

    @Test
    public void test_transferEnvelope_invalid_maxLimLow(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelopeFrom = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));
        Envelope envelopeTo = new Envelope(200, user, "100", new BigDecimal("100.00"), new BigDecimal("100.00"));
        TransferFundDTO transferFundDTO = new TransferFundDTO(100, 200, "TransactionTitle", "TransactionDesc", new BigDecimal("100.00"));

        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelopeFrom));
        when(envelopeRepository.findById(200)).thenReturn(Optional.of(envelopeTo));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.transferEnvelope(transferFundDTO, TEST_USERNAME));
        Mockito.verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
        Mockito.verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        Mockito.verify(envelopeHistoryService, never()).createEnvelopeHistory(Mockito.any(EnvelopeHistory.class));
    }

    @Test
    public void test_allocateMoney_valid(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));

        Envelope outEnvelope = new Envelope();
        outEnvelope.setUser(user);
        outEnvelope.setEnvelopeDescription("saveEnvelope");

        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(new BigDecimal("100.00"));
        transaction.setCategory("Bills");
        transaction.setTitle("allocateTransaction");

        Transaction saveTransaction = new Transaction();
        saveTransaction.setTitle("saveTransaction");

        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));

        ArgumentCaptor<Envelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(Envelope.class);
        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(envelopeRepository.save(envelopeArgumentCaptor.capture())).thenReturn(outEnvelope);
        when(transactionService.createTransaction(transactionArgumentCaptor.capture())).thenReturn(saveTransaction);

        Transaction result = envelopeService.allocateMoney(100, transaction, TEST_USERNAME);

        Envelope saveEnvelope = envelopeArgumentCaptor.getValue();
        Transaction createTransaction = transactionArgumentCaptor.getValue();
        Assertions.assertEquals(100, saveEnvelope.getEnvelopeId());
        Assertions.assertEquals(new BigDecimal("200.00"), saveEnvelope.getBalance());
        Assertions.assertEquals(new BigDecimal("100.00"), createTransaction.getTransactionAmount());
        Assertions.assertEquals("saveTransaction", result.getTitle());
    }

    @Test
    public void test_allocateMoney_invalid_noUser(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));

        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(new BigDecimal("100.00"));
        transaction.setCategory("Bills");
        transaction.setTitle("allocateTransaction");

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.allocateMoney(0, transaction, TEST_USERNAME));

        verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
    }

    @Test
    public void test_allocateMoney_invalid_maxLimTooLow(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));

        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(new BigDecimal("10000000000.00"));
        transaction.setCategory("Bills");
        transaction.setTitle("allocateTransaction");

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.allocateMoney(100, transaction, TEST_USERNAME));

        verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
    }

    @Test
    public void test_allocateMoney_invalid_noTransfer(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));

        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(BigDecimal.ZERO);
        transaction.setCategory("Bills");
        transaction.setTitle("allocateTransaction");

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.allocateMoney(100, transaction, TEST_USERNAME));

        verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
    }

    @Test
    public void test_spendMoney_valid(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));

        Envelope outEnvelope = new Envelope();
        outEnvelope.setUser(user);
        outEnvelope.setEnvelopeDescription("saveEnvelope");

        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(new BigDecimal("100.00"));
        transaction.setCategory("Bills");
        transaction.setTitle("allocateTransaction");

        Transaction saveTransaction = new Transaction();
        saveTransaction.setTitle("saveTransaction");

        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));

        ArgumentCaptor<Envelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(Envelope.class);
        ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(envelopeRepository.save(envelopeArgumentCaptor.capture())).thenReturn(outEnvelope);
        when(transactionService.createTransaction(transactionArgumentCaptor.capture())).thenReturn(saveTransaction);

        Transaction result = envelopeService.spendMoney(100, transaction, TEST_USERNAME);

        Envelope saveEnvelope = envelopeArgumentCaptor.getValue();
        Transaction createTransaction = transactionArgumentCaptor.getValue();
        Assertions.assertEquals(100, saveEnvelope.getEnvelopeId());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(2), saveEnvelope.getBalance());
        Assertions.assertEquals(new BigDecimal("-100.00"), createTransaction.getTransactionAmount());
        Assertions.assertEquals("saveTransaction", result.getTitle());
    }

    @Test
    public void test_spendMoney_invalid_noUser(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));

        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(new BigDecimal("100.00"));
        transaction.setCategory("Bills");
        transaction.setTitle("allocateTransaction");

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.spendMoney(0, transaction, TEST_USERNAME));

        verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
    }

    @Test
    public void test_spendMoney_invalid_BalanceTooLow(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));

        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(new BigDecimal("10000000000.00"));
        transaction.setCategory("Bills");
        transaction.setTitle("allocateTransaction");

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.spendMoney(100, transaction, TEST_USERNAME));

        verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
    }

    @Test
    public void test_spendMoney_invalid_noTransfer(){
        User user = new User();
        user.setUserId(100);
        user.setUsername(TEST_USERNAME);
        Envelope envelope = new Envelope(100, user, "100", new BigDecimal("100.00"), new BigDecimal("200.00"));

        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(BigDecimal.ZERO);
        transaction.setCategory("Bills");
        transaction.setTitle("allocateTransaction");

        when(envelopeRepository.findById(0)).thenReturn(Optional.empty());
        when(envelopeRepository.findById(100)).thenReturn(Optional.of(envelope));

        Assertions.assertThrows(BusinessException.class, () -> envelopeService.spendMoney(100, transaction, TEST_USERNAME));

        verify(envelopeRepository, never()).save(Mockito.any(Envelope.class));
        verify(transactionService, never()).createTransaction(Mockito.any(Transaction.class));
    }
}
