package com.rc.Bank_Service.service;

import com.rc.Bank_Service.dto.PaymentRequest;
import com.rc.Bank_Service.dto.TransactionDTO;
import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.Transaction;
import com.rc.Bank_Service.model.User;
import com.rc.Bank_Service.repository.AccountRepository;
import com.rc.Bank_Service.repository.TransactionRepository;
import com.rc.Bank_Service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TransactionService(AccountRepository accountRepository,
                               TransactionRepository transactionRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TransactionDTO processUpiTransfer(PaymentRequest request) {
        if (request.getRecipient() == null || request.getRecipient().isBlank()) {
            throw new IllegalArgumentException("Recipient VPA or Account Number is required.");
        }

        String senderAccNum = request.getAccountNumber().trim();
        BigDecimal amount = request.getAmount();

        Account senderAcc = accountRepository.findByAccountNumber(senderAccNum)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found: " + senderAccNum));

        User senderUser = senderAcc.getUser();

        // Security Check 1: MPIN Authorization Check (OWASP A07)
        if (request.getMpin() != null && !request.getMpin().isBlank()) {
            if (senderUser.getMpinHash() != null && !passwordEncoder.matches(request.getMpin(), senderUser.getMpinHash())) {
                throw new IllegalArgumentException("Invalid 6-Digit MPIN! Transaction authorization failed.");
            }
        }

        // Security Check 2: Sufficient Balance Check
        if (senderAcc.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds! Available balance is ₹" + senderAcc.getBalance());
        }

        // Security Check 3: Find Recipient Account (via Account Number or UPI VPA)
        String recipientInput = request.getRecipient().trim();
        Optional<Account> recipientAccOpt = accountRepository.findByAccountNumber(recipientInput);
        if (recipientAccOpt.isEmpty()) {
            recipientAccOpt = accountRepository.findByUpiVpa(recipientInput);
        }

        if (recipientAccOpt.isEmpty()) {
            throw new IllegalArgumentException("Recipient account/VPA '" + recipientInput + "' not found in YourBank directory.");
        }

        Account recipientAcc = recipientAccOpt.get();
        User recipientUser = recipientAcc.getUser();

        // Security Check 4: Prevent Self-Transfer
        if (senderAcc.getAccountNumber().equals(recipientAcc.getAccountNumber())) {
            throw new IllegalArgumentException("Self-transfer not permitted. Recipient must be a different customer account.");
        }

        // Determine Mode & Category Tag
        String mode = (request.getTransferMode() != null && !request.getTransferMode().isBlank())
                ? request.getTransferMode().toUpperCase()
                : "UPI";

        String categoryTag = "UPI_TRANSFER";
        if ("IMPS".equals(mode) || "NEFT".equals(mode) || "RTGS".equals(mode)) {
            categoryTag = mode + "_BANK_TRANSFER";
        } else if ("NETBANKING".equals(mode)) {
            categoryTag = "NETBANKING_TRANSFER";
        }

        // 1. Deduct amount from Sender Account
        BigDecimal senderNewBalance = senderAcc.getBalance().subtract(amount);
        senderAcc.setBalance(senderNewBalance);
        accountRepository.save(senderAcc);

        // 2. Credit amount to Recipient Account
        BigDecimal recipientNewBalance = recipientAcc.getBalance().add(amount);
        recipientAcc.setBalance(recipientNewBalance);
        accountRepository.save(recipientAcc);

        String txnId = "TXN-" + mode + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String beneficiaryDisplay = (request.getBeneficiaryName() != null && !request.getBeneficiaryName().isBlank())
                ? request.getBeneficiaryName()
                : recipientUser.getFullName();

        String remarks = (request.getRemarks() != null && !request.getRemarks().isBlank())
                ? request.getRemarks()
                : mode + " Fund Transfer to " + beneficiaryDisplay;

        if (request.getIfscCode() != null && !request.getIfscCode().isBlank()) {
            remarks += " (IFSC: " + request.getIfscCode() + ")";
        }

        // 3. Record DEBIT Ledger Entry for Sender
        Transaction debitTxn = new Transaction(
                txnId,
                senderAcc,
                "DEBIT",
                categoryTag,
                amount,
                senderNewBalance,
                senderAcc.getUpiVpa(),
                recipientAcc.getUpiVpa(),
                remarks
        );

        // 4. Record CREDIT Ledger Entry for Recipient
        Transaction creditTxn = new Transaction(
                txnId + "-CR",
                recipientAcc,
                "CREDIT",
                categoryTag,
                amount,
                recipientNewBalance,
                senderAcc.getUpiVpa(),
                recipientAcc.getUpiVpa(),
                "Received " + mode + " Transfer from " + senderUser.getFullName() + " (" + remarks + ")"
        );

        transactionRepository.save(creditTxn);
        Transaction savedDebitTxn = transactionRepository.save(debitTxn);

        return new TransactionDTO(
                savedDebitTxn.getTransactionId(),
                savedDebitTxn.getType(),
                savedDebitTxn.getCategory(),
                savedDebitTxn.getAmount(),
                savedDebitTxn.getBalanceAfter(),
                savedDebitTxn.getSenderVpa(),
                savedDebitTxn.getReceiverVpa(),
                senderUser.getFullName(),
                recipientUser.getFullName(),
                savedDebitTxn.getStatus(),
                savedDebitTxn.getRemarks(),
                savedDebitTxn.getCreatedAt()
        );
    }
}
