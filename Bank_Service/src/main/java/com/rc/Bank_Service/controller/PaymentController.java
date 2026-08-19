package com.rc.Bank_Service.controller;

import com.rc.Bank_Service.dto.PaymentRequest;
import com.rc.Bank_Service.dto.RazorpayVerifyRequest;
import com.rc.Bank_Service.dto.TransactionDTO;
import com.rc.Bank_Service.service.RazorpayService;
import com.rc.Bank_Service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final TransactionService transactionService;
    private final RazorpayService razorpayService;

    @Autowired
    public PaymentController(TransactionService transactionService, RazorpayService razorpayService) {
        this.transactionService = transactionService;
        this.razorpayService = razorpayService;
    }

    @PostMapping("/upi")
    public ResponseEntity<TransactionDTO> processUpiTransfer(@Valid @RequestBody PaymentRequest request) {
        TransactionDTO transaction = transactionService.processUpiTransfer(request);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/razorpay/create-order")
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(@Valid @RequestBody PaymentRequest request) {
        Map<String, Object> orderDetails = razorpayService.createRazorpayOrder(request);
        return ResponseEntity.ok(orderDetails);
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<TransactionDTO> verifyRazorpayPayment(
            @Valid @RequestBody RazorpayVerifyRequest request,
            @RequestParam(value = "amount", required = false) BigDecimal amount,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "remarks", required = false) String remarks) {
        TransactionDTO transaction = razorpayService.verifyRazorpayPayment(request, amount, category, remarks);
        return ResponseEntity.ok(transaction);
    }
}
