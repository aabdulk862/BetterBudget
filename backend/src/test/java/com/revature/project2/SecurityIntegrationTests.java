package com.revature.project2;

import com.revature.project2.models.Envelope;
import com.revature.project2.models.Transaction;
import com.revature.project2.models.User;
import com.revature.project2.models.DTOs.TransferFundDTO;
import com.revature.project2.repositories.EnvelopeRepository;
import com.revature.project2.repositories.UserRepository;
import com.revature.project2.services.EnvelopeHistoryService;
import com.revature.project2.services.EnvelopeService;
import com.revature.project2.services.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Security integration tests verifying:
 * - Unauthenticated requests are rejected (Requirements 14.2)
 * - Users cannot access other users' envelopes (Requirements 14.3)
 * - Manager role bypasses ownership checks (Requirements 14.3)
 */
public class SecurityIntegrationTests {

    private EnvelopeRepository envelopeRepository;
    private UserRepository userRepository;
    private TransactionService transactionService;
    private EnvelopeHistoryService envelopeHistoryService;
    private EnvelopeService envelopeService;

    private static final String OWNER_USERNAME = "owner";
    private static final String OTHER_USERNAME = "otheruser";
    private static final String MANAGER_USERNAME = "manager";

    private User ownerUser;
    private Envelope ownerEnvelope;

    @BeforeEach
    void setUp() {
        envelopeRepository = Mockito.mock(EnvelopeRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        transactionService = Mockito.mock(TransactionService.class);
        envelopeHistoryService = Mockito.mock(EnvelopeHistoryService.class);
        envelopeService = new EnvelopeService(envelopeRepository, userRepository, transactionService, envelopeHistoryService);

        ownerUser = new User();
        ownerUser.setUserId(1);
        ownerUser.setUsername(OWNER_USERNAME);

        ownerEnvelope = new Envelope(10, ownerUser, "Groceries", new BigDecimal("500.00"), new BigDecimal("1000.00"));

        when(envelopeRepository.findById(10)).thenReturn(Optional.of(ownerEnvelope));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(String username, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // =========================================================================
    // 1. Unauthenticated access tests (no SecurityContext set)
    //    Validates: Requirement 14.2
    // =========================================================================
    @Nested
    @DisplayName("Unauthenticated access - no SecurityContext")
    class UnauthenticatedAccessTests {

        @Test
        @DisplayName("getEnvelopeById without auth throws AccessDeniedException")
        void getEnvelopeById_noAuth_throwsAccessDenied() {
            // No SecurityContext set — verifyOwnership checks authentication.getAuthorities()
            // which will be null, so the ownership check compares usernames and fails
            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.getEnvelopeById(10, "anonymous"));
        }

        @Test
        @DisplayName("deleteEnvelope without auth throws AccessDeniedException")
        void deleteEnvelope_noAuth_throwsAccessDenied() {
            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.deleteEnvelope(10, "anonymous"));
            verify(envelopeRepository, never()).delete(any(Envelope.class));
        }

        @Test
        @DisplayName("allocateMoney without auth throws AccessDeniedException")
        void allocateMoney_noAuth_throwsAccessDenied() {
            Transaction tx = new Transaction();
            tx.setTransactionAmount(new BigDecimal("50.00"));

            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.allocateMoney(10, tx, "anonymous"));
            verify(envelopeRepository, never()).save(any(Envelope.class));
        }

        @Test
        @DisplayName("spendMoney without auth throws AccessDeniedException")
        void spendMoney_noAuth_throwsAccessDenied() {
            Transaction tx = new Transaction();
            tx.setTransactionAmount(new BigDecimal("50.00"));

            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.spendMoney(10, tx, "anonymous"));
            verify(envelopeRepository, never()).save(any(Envelope.class));
        }

        @Test
        @DisplayName("transferEnvelope without auth throws AccessDeniedException")
        void transferEnvelope_noAuth_throwsAccessDenied() {
            User otherUser = new User();
            otherUser.setUserId(2);
            otherUser.setUsername("anonymous");
            Envelope toEnvelope = new Envelope(20, ownerUser, "Savings", new BigDecimal("200.00"), new BigDecimal("2000.00"));
            when(envelopeRepository.findById(20)).thenReturn(Optional.of(toEnvelope));

            TransferFundDTO dto = new TransferFundDTO(10, 20, "Transfer", "desc", new BigDecimal("50.00"));

            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.transferEnvelope(dto, "anonymous"));
            verify(envelopeRepository, never()).save(any(Envelope.class));
        }
    }

    // =========================================================================
    // 2. Wrong-user access tests (authenticated as different user)
    //    Validates: Requirement 14.3
    // =========================================================================
    @Nested
    @DisplayName("Wrong-user access - ROLE_EMPLOYEE accessing another user's resources")
    class WrongUserAccessTests {

        @BeforeEach
        void setUpOtherUser() {
            setSecurityContext(OTHER_USERNAME, "ROLE_EMPLOYEE");
        }

        @Test
        @DisplayName("getEnvelopeById as wrong user throws AccessDeniedException")
        void getEnvelopeById_wrongUser_throwsAccessDenied() {
            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.getEnvelopeById(10, OTHER_USERNAME));
        }

        @Test
        @DisplayName("deleteEnvelope as wrong user throws AccessDeniedException")
        void deleteEnvelope_wrongUser_throwsAccessDenied() {
            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.deleteEnvelope(10, OTHER_USERNAME));
            verify(envelopeRepository, never()).delete(any(Envelope.class));
        }

        @Test
        @DisplayName("allocateMoney as wrong user throws AccessDeniedException")
        void allocateMoney_wrongUser_throwsAccessDenied() {
            Transaction tx = new Transaction();
            tx.setTransactionAmount(new BigDecimal("50.00"));

            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.allocateMoney(10, tx, OTHER_USERNAME));
            verify(envelopeRepository, never()).save(any(Envelope.class));
        }

        @Test
        @DisplayName("spendMoney as wrong user throws AccessDeniedException")
        void spendMoney_wrongUser_throwsAccessDenied() {
            Transaction tx = new Transaction();
            tx.setTransactionAmount(new BigDecimal("50.00"));

            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.spendMoney(10, tx, OTHER_USERNAME));
            verify(envelopeRepository, never()).save(any(Envelope.class));
        }

        @Test
        @DisplayName("transferEnvelope as wrong user throws AccessDeniedException")
        void transferEnvelope_wrongUser_throwsAccessDenied() {
            Envelope toEnvelope = new Envelope(20, ownerUser, "Savings", new BigDecimal("200.00"), new BigDecimal("2000.00"));
            when(envelopeRepository.findById(20)).thenReturn(Optional.of(toEnvelope));

            TransferFundDTO dto = new TransferFundDTO(10, 20, "Transfer", "desc", new BigDecimal("50.00"));

            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.transferEnvelope(dto, OTHER_USERNAME));
            verify(envelopeRepository, never()).save(any(Envelope.class));
        }

        @Test
        @DisplayName("Resources remain unmodified after access denied")
        void resources_unmodified_afterAccessDenied() {
            BigDecimal originalBalance = ownerEnvelope.getBalance();

            Transaction tx = new Transaction();
            tx.setTransactionAmount(new BigDecimal("50.00"));

            assertThrows(AccessDeniedException.class,
                    () -> envelopeService.spendMoney(10, tx, OTHER_USERNAME));

            // Verify the envelope balance was not changed
            assertEquals(originalBalance, ownerEnvelope.getBalance());
            verify(envelopeRepository, never()).save(any(Envelope.class));
            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }
    }

    // =========================================================================
    // 3. Manager role bypass tests (ROLE_MANAGER can access any resource)
    //    Validates: Requirement 14.3 (manager bypass)
    // =========================================================================
    @Nested
    @DisplayName("Manager role bypass - ROLE_MANAGER accesses any user's resources")
    class ManagerBypassTests {

        @BeforeEach
        void setUpManager() {
            setSecurityContext(MANAGER_USERNAME, "ROLE_MANAGER");
        }

        @Test
        @DisplayName("Manager can getEnvelopeById for any user")
        void getEnvelopeById_manager_succeeds() {
            Envelope result = envelopeService.getEnvelopeById(10, MANAGER_USERNAME);
            assertNotNull(result);
            assertEquals(10, result.getEnvelopeId());
        }

        @Test
        @DisplayName("Manager can deleteEnvelope for any user")
        void deleteEnvelope_manager_succeeds() {
            assertDoesNotThrow(() -> envelopeService.deleteEnvelope(10, MANAGER_USERNAME));
            verify(envelopeRepository).delete(ownerEnvelope);
        }

        @Test
        @DisplayName("Manager can allocateMoney to any user's envelope")
        void allocateMoney_manager_succeeds() {
            Transaction tx = new Transaction();
            tx.setTransactionAmount(new BigDecimal("100.00"));
            tx.setTitle("Manager allocation");
            tx.setTransactionDescription("Manager adding funds");
            tx.setCategory("Admin");

            Transaction savedTx = new Transaction();
            savedTx.setTitle("Manager allocation");
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(savedTx);
            when(envelopeRepository.save(any(Envelope.class))).thenReturn(ownerEnvelope);

            Transaction result = envelopeService.allocateMoney(10, tx, MANAGER_USERNAME);
            assertNotNull(result);
            verify(envelopeRepository).save(any(Envelope.class));
        }

        @Test
        @DisplayName("Manager can spendMoney from any user's envelope")
        void spendMoney_manager_succeeds() {
            Transaction tx = new Transaction();
            tx.setTransactionAmount(new BigDecimal("100.00"));
            tx.setTitle("Manager spend");
            tx.setTransactionDescription("Manager spending");
            tx.setCategory("Admin");

            Transaction savedTx = new Transaction();
            savedTx.setTitle("Manager spend");
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(savedTx);
            when(envelopeRepository.save(any(Envelope.class))).thenReturn(ownerEnvelope);

            Transaction result = envelopeService.spendMoney(10, tx, MANAGER_USERNAME);
            assertNotNull(result);
            verify(envelopeRepository).save(any(Envelope.class));
        }

        @Test
        @DisplayName("Manager can transfer between any user's envelopes")
        void transferEnvelope_manager_succeeds() {
            Envelope toEnvelope = new Envelope(20, ownerUser, "Savings", new BigDecimal("200.00"), new BigDecimal("2000.00"));
            when(envelopeRepository.findById(20)).thenReturn(Optional.of(toEnvelope));
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(new Transaction());

            TransferFundDTO dto = new TransferFundDTO(10, 20, "Transfer", "desc", new BigDecimal("50.00"));

            assertDoesNotThrow(() -> envelopeService.transferEnvelope(dto, MANAGER_USERNAME));
            verify(envelopeRepository, times(2)).save(any(Envelope.class));
        }
    }
}
