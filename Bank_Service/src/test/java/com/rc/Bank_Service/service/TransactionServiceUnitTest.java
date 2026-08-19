package com.rc.Bank_Service.service;

import com.rc.Bank_Service.dto.PaymentRequest;
import com.rc.Bank_Service.dto.TransactionDTO;
import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.Transaction;
import com.rc.Bank_Service.model.User;
import com.rc.Bank_Service.repository.AccountRepository;
import com.rc.Bank_Service.repository.TransactionRepository;
import com.rc.Bank_Service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceUnitTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransactionService transactionService;

    private User senderUser;
    private User recipientUser;
    private Account senderAccount;
    private Account recipientAccount;

    @BeforeEach
    void setUp() {
        senderUser = new User("000101100001", "Singam Ramcharan", "ramcharan@example.com", "hash", "9876543211", "ABCPE1234F", "234567890123", LocalDate.of(1995, 5, 15), "MALE", "Bengaluru");
        senderUser.setMpinHash("$2a$12$hashedMpin984021");

        recipientUser = new User("000101100002", "Rama Krishna", "ramakrishna@example.com", "hash", "9876543212", "XYZPE5678K", "345678901234", LocalDate.of(1992, 8, 20), "MALE", "Hyderabad");

        senderAccount = new Account(senderUser, "000101100001", "000101100001", null, "YBNK0000001", "SAVINGS_REGULAR", new BigDecimal("5000.00"), "ramcharan@ybank");
        recipientAccount = new Account(recipientUser, "000101100002", "000101100002", null, "YBNK0000001", "SAVINGS_REGULAR", new BigDecimal("2000.00"), "ramakrishna@ybank");
    }

    @Test
    @DisplayName("Should successfully transfer funds between two accounts with valid MPIN")
    void testProcessUpiTransferSuccess() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountNumber("000101100001");
        request.setRecipient("000101100002");
        request.setAmount(new BigDecimal("1000.00"));
        request.setMpin("984021");
        request.setTransferMode("UPI");
        request.setRemarks("Rent");

        when(accountRepository.findByAccountNumber("000101100001")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByAccountNumber("000101100002")).thenReturn(Optional.of(recipientAccount));
        when(passwordEncoder.matches("984021", "$2a$12$hashedMpin984021")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDTO result = transactionService.processUpiTransfer(request);

        assertNotNull(result);
        assertEquals("Singam Ramcharan", result.getSenderName());
        assertEquals("Rama Krishna", result.getReceiverName());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals(new BigDecimal("4000.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("3000.00"), recipientAccount.getBalance());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when MPIN is invalid")
    void testProcessUpiTransferInvalidMpin() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountNumber("000101100001");
        request.setRecipient("000101100002");
        request.setAmount(new BigDecimal("1000.00"));
        request.setMpin("000000"); // Bad MPIN

        when(accountRepository.findByAccountNumber("000101100001")).thenReturn(Optional.of(senderAccount));
        when(passwordEncoder.matches("000000", "$2a$12$hashedMpin984021")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.processUpiTransfer(request);
        });

        assertEquals("Invalid 6-Digit MPIN! Transaction authorization failed.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when sender has insufficient funds")
    void testProcessUpiTransferInsufficientBalance() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountNumber("000101100001");
        request.setRecipient("000101100002");
        request.setAmount(new BigDecimal("10000.00")); // More than 5000 balance
        request.setMpin("984021");

        when(accountRepository.findByAccountNumber("000101100001")).thenReturn(Optional.of(senderAccount));
        when(passwordEncoder.matches("984021", "$2a$12$hashedMpin984021")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.processUpiTransfer(request);
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attempting self-transfer")
    void testProcessUpiTransferSelfTransfer() {
        PaymentRequest request = new PaymentRequest();
        request.setAccountNumber("000101100001");
        request.setRecipient("000101100001"); // Self transfer
        request.setAmount(new BigDecimal("500.00"));
        request.setMpin("984021");

        when(accountRepository.findByAccountNumber("000101100001")).thenReturn(Optional.of(senderAccount));
        when(passwordEncoder.matches("984021", "$2a$12$hashedMpin984021")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.processUpiTransfer(request);
        });

        assertEquals("Self-transfer not permitted. Recipient must be a different customer account.", exception.getMessage());
    }
}
