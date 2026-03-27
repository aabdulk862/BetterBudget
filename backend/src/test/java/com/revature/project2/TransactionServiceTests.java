package com.revature.project2;

import com.revature.project2.exceptions.BusinessException;
import com.revature.project2.models.Envelope;
import com.revature.project2.models.EnvelopeHistory;
import com.revature.project2.models.Transaction;
import com.revature.project2.models.User;
import com.revature.project2.models.mappers.TransactionDTOMapper;
import com.revature.project2.repositories.EnvelopeRepository;
import com.revature.project2.repositories.TransactionRepository;
import com.revature.project2.services.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

public class TransactionServiceTests {
    private TransactionRepository transactionRepository;
    private EnvelopeRepository envelopeRepository;
    private TransactionService transactionService;

    @BeforeEach
    void contextLoads(){
        transactionRepository = Mockito.mock(TransactionRepository.class);
        envelopeRepository = Mockito.mock(EnvelopeRepository.class);
        transactionService = new TransactionService(transactionRepository, envelopeRepository, new TransactionDTOMapper());
        SecurityContextHolder.clearContext();
    }

    @Test
    void test_updateTransactionTitle(){
        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(new BigDecimal("100.00"));
        Transaction updateTransaction = new Transaction();
        updateTransaction.setTransactionAmount(new BigDecimal("100.00"));
        updateTransaction.setTitle("newTitle");
        ArgumentCaptor<Transaction> transactionCapture = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.findById(0)).thenReturn(Optional.of(transaction));
        when(transactionRepository.findById(1)).thenReturn(Optional.empty());
        when(transactionRepository.save(transactionCapture.capture())).thenReturn(updateTransaction);
        //NOTE: incorrect output, but 1.) we just want to see what this saved 2.) returning getcapture output errors
        Transaction responseTransaction = transactionService.updateTransactionTitle(0, "newTitle");
        Transaction savedTransaction = transactionCapture.getValue();
        Assertions.assertTrue(responseTransaction.getTitle().equals("newTitle"));
        Assertions.assertEquals(0, responseTransaction.getTransactionAmount().compareTo(new BigDecimal("100.00")));

        Assertions.assertTrue(savedTransaction.getTitle().equals("newTitle"));
        Assertions.assertEquals(0, savedTransaction.getTransactionAmount().compareTo(new BigDecimal("100.00")));

        Assertions.assertThrows(RuntimeException.class, ()->transactionService.updateTransactionTitle(1, "newTitle"));
    }

    @Test
    void test_updateTransactionDescription(){
        Transaction transaction = new Transaction();
        transaction.setTransactionAmount(new BigDecimal("100.00"));
        Transaction updateTransaction = new Transaction();
        updateTransaction.setTransactionAmount(new BigDecimal("100.00"));
        updateTransaction.setTransactionDescription("Desc");
        ArgumentCaptor<Transaction> transactionCapture = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.findById(0)).thenReturn(Optional.of(transaction));
        when(transactionRepository.findById(1)).thenReturn(Optional.empty());
        when(transactionRepository.save(transactionCapture.capture())).thenReturn(updateTransaction);
        //NOTE: incorrect output, but 1.) we just want to see what this saved 2.) returning getcapture output errors
        Transaction responseTransaction = transactionService.updateTransactionDescription(0, "Desc");
        Transaction savedTransaction = transactionCapture.getValue();
        Assertions.assertTrue(responseTransaction.getTransactionDescription().equals("Desc"));
        Assertions.assertEquals(0, responseTransaction.getTransactionAmount().compareTo(new BigDecimal("100.00")));

        Assertions.assertTrue(savedTransaction.getTransactionDescription().equals("Desc"));
        Assertions.assertEquals(0, savedTransaction.getTransactionAmount().compareTo(new BigDecimal("100.00")));

        Assertions.assertThrows(RuntimeException.class, ()->transactionService.updateTransactionDescription(1, "newTitle"));
    }

    @Test
    void test_createTransaction_valid(){
        Transaction transaction = new Transaction();
        transaction.setTitle("Title");
        transaction.setTransactionDescription("Desc");
        ArgumentCaptor<Transaction> transactionCapture = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(transactionCapture.capture())).thenReturn(transaction);
        Transaction responseTransaction = transactionService.createTransaction(transaction);
        Transaction savedTransaction = transactionCapture.getValue();
        Assertions.assertTrue(responseTransaction.getTransactionDescription().equals("Desc"));
        Assertions.assertTrue(responseTransaction.getTitle().equals("Title"));
        Assertions.assertTrue(savedTransaction.getTransactionDescription().equals("Desc"));
        Assertions.assertTrue(savedTransaction.getTitle().equals("Title"));

    }

    @ParameterizedTest
    @CsvSource({",Desc","Title,"})
    void  test_createTransaction_invalid(String title, String desc){
        Transaction transaction = new Transaction();
        transaction.setTitle(title);
        transaction.setTransactionDescription(desc);

        Assertions.assertThrows(RuntimeException.class, ()->transactionService.createTransaction(transaction));

    }

    @Test
    void test_getAllTransactions(){
        List<Transaction> tList = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setTitle("Title");
        transaction.setTransactionDescription("Desc");
        tList.add(transaction);
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(tList, pageable, tList.size());
        when(transactionRepository.findAll(pageable)).thenReturn(page);
        Page<Transaction> responsePage = transactionService.getAllTransactions(pageable);
        Assertions.assertEquals(1, responsePage.getTotalElements());
        Assertions.assertEquals(1, responsePage.getContent().size());
        Assertions.assertTrue("Desc".equals(responsePage.getContent().get(0).getTransactionDescription()));
        Assertions.assertTrue("Title".equals(responsePage.getContent().get(0).getTitle()));
    }

     @Test
     void test_updateTransactionCategory(){
         Transaction transaction = new Transaction();
         transaction.setEnvelopeHistories(new ArrayList<EnvelopeHistory>());
         transaction.setTitle("Title");
         transaction.setTransactionDescription("Desc");
         Transaction outTransaction = new Transaction();
         outTransaction.setEnvelopeHistories(new ArrayList<EnvelopeHistory>());
         outTransaction.setTitle("Title");
         outTransaction.setTransactionDescription("Desc");
         outTransaction.setCategory("Bills");
         ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
         when(transactionRepository.findById(0)).thenReturn(Optional.of(transaction));
         when(transactionRepository.findById(1)).thenReturn(Optional.empty());
         when(transactionRepository.save(transactionArgumentCaptor.capture())).thenReturn(outTransaction);
         Transaction responseTransaction =  transactionService.updateTransactionCategory(0, "Bills");
         Transaction savedTransaction = transactionArgumentCaptor.getValue();
         Assertions.assertTrue(responseTransaction.getCategory().equals("Bills"));
         Assertions.assertTrue(responseTransaction.getTransactionDescription().equals("Desc"));
         Assertions.assertTrue(responseTransaction.getTitle().equals("Title"));

         Assertions.assertTrue(savedTransaction.getCategory().equals("Bills"));
         Assertions.assertTrue(savedTransaction.getTransactionDescription().equals("Desc"));
         Assertions.assertTrue(savedTransaction.getTitle().equals("Title"));

         Assertions.assertThrows(BusinessException.class,  ()->transactionService.updateTransactionCategory(1, "Bills"));
         Assertions.assertThrows(BusinessException.class,  ()->transactionService.updateTransactionCategory(0, ""));
     }

    @Test
    void test_getTransactionsByEnvelopeId(){
        User owner = new User();
        owner.setUsername("testuser");

        Envelope envelope = new Envelope();
        envelope.setEnvelopeId(0);
        envelope.setUser(owner);

        Envelope envelope1 = new Envelope();
        envelope1.setEnvelopeId(1);
        envelope1.setUser(owner);

        when(envelopeRepository.findById(0)).thenReturn(Optional.of(envelope));
        when(envelopeRepository.findById(1)).thenReturn(Optional.of(envelope1));

        List<Transaction> tList = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setTitle("Title");
        transaction.setTransactionDescription("Desc");
        tList.add(transaction);
        when(transactionRepository.findByEnvelope_EnvelopeId(0)).thenReturn(tList);
        when(transactionRepository.findByEnvelope_EnvelopeId(1)).thenReturn(new ArrayList<>());

        // Set up security context as the owner
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))));

        List<Transaction> responseList = transactionService.getTransactionsByEnvelopeId(0, "testuser");
        Assertions.assertEquals(1, responseList.size());
        Assertions.assertTrue("Desc".equals(responseList.get(0).getTransactionDescription()));
        Assertions.assertTrue("Title".equals(responseList.get(0).getTitle()));

        List<Transaction> emptyList = transactionService.getTransactionsByEnvelopeId(1, "testuser");
        Assertions.assertEquals(0, emptyList.size());
    }

    @Test
    void test_getTransactionsByEnvelopeId_accessDenied(){
        User owner = new User();
        owner.setUsername("otheruser");

        Envelope envelope = new Envelope();
        envelope.setEnvelopeId(0);
        envelope.setUser(owner);

        when(envelopeRepository.findById(0)).thenReturn(Optional.of(envelope));

        // Set up security context as a different user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))));

        Assertions.assertThrows(AccessDeniedException.class,
                () -> transactionService.getTransactionsByEnvelopeId(0, "testuser"));
    }

    @Test
    void test_getTransactionsByEnvelopeId_managerBypass(){
        User owner = new User();
        owner.setUsername("otheruser");

        Envelope envelope = new Envelope();
        envelope.setEnvelopeId(0);
        envelope.setUser(owner);

        when(envelopeRepository.findById(0)).thenReturn(Optional.of(envelope));

        List<Transaction> tList = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setTitle("Title");
        tList.add(transaction);
        when(transactionRepository.findByEnvelope_EnvelopeId(0)).thenReturn(tList);

        // Set up security context as a manager
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager", null, List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))));

        List<Transaction> responseList = transactionService.getTransactionsByEnvelopeId(0, "manager");
        Assertions.assertEquals(1, responseList.size());
    }

    @Test
    void test_getTransactionsByEnvelopeId_envelopeNotFound(){
        when(envelopeRepository.findById(999)).thenReturn(Optional.empty());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))));

        Assertions.assertThrows(BusinessException.class,
                () -> transactionService.getTransactionsByEnvelopeId(999, "testuser"));
    }


}
