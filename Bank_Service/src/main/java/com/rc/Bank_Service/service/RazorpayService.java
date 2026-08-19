package com.rc.Bank_Service.service;

import com.rc.Bank_Service.dto.PaymentRequest;
import com.rc.Bank_Service.dto.RazorpayVerifyRequest;
import com.rc.Bank_Service.dto.TransactionDTO;
import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.Transaction;
import com.rc.Bank_Service.repository.AccountRepository;
import com.rc.Bank_Service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RazorpayService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Value("${razorpay.key_id:rzp_test_YourBankKey123}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret:YourBankSecretKey123}")
    private String razorpayKeySecret;

    @Autowired
    public RazorpayService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Object> createRazorpayOrder(PaymentRequest request) {
        String orderId = "order_rzp_" + UUID.randomUUID().toString().substring(0, 10);
        BigDecimal amountInPaise = request.getAmount().multiply(new BigDecimal("100"));

        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("orderId", orderId);
        orderDetails.put("currency", "INR");
        orderDetails.put("amount", amountInPaise);
        orderDetails.put("keyId", razorpayKeyId);
        orderDetails.put("accountNumber", request.getAccountNumber());
        return orderDetails;
    }

    @Transactional
    public TransactionDTO verifyRazorpayPayment(RazorpayVerifyRequest request, BigDecimal amount, String category, String remarks) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber().trim())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountNumber()));

        BigDecimal depositAmount = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) ? amount : new BigDecimal("1000.00");

        // Add funds received via Razorpay Gateway to customer account
        BigDecimal newBalance = account.getBalance().add(depositAmount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        String txnCategory = (category != null && !category.isBlank()) ? category : "RAZORPAY_GATEWAY";
        String txnRemarks = (remarks != null && !remarks.isBlank()) ? remarks : "Funds added via Razorpay Payment Gateway (ID: " + request.getRazorpayPaymentId() + ")";

        String txnId = "TXN-RZP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction txn = new Transaction(
                txnId,
                account,
                "CREDIT",
                txnCategory,
                depositAmount,
                newBalance,
                "Razorpay Gateway",
                account.getUpiVpa(),
                txnRemarks
        );
        txn.setRazorpayPaymentId(request.getRazorpayPaymentId());
        txn.setRazorpayOrderId(request.getRazorpayOrderId());

        Transaction savedTxn = transactionRepository.save(txn);

        return new TransactionDTO(
                savedTxn.getTransactionId(),
                savedTxn.getType(),
                savedTxn.getCategory(),
                savedTxn.getAmount(),
                savedTxn.getBalanceAfter(),
                savedTxn.getSenderVpa(),
                savedTxn.getReceiverVpa(),
                savedTxn.getStatus(),
                savedTxn.getRemarks(),
                savedTxn.getCreatedAt()
        );
    }

    // Overloaded method for backward compatibility
    @Transactional
    public TransactionDTO verifyRazorpayPayment(RazorpayVerifyRequest request, BigDecimal amount) {
        return verifyRazorpayPayment(request, amount, "RAZORPAY_GATEWAY", null);
    }
}
