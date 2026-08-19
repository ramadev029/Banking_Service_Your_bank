package com.rc.Bank_Service.dto;

import jakarta.validation.constraints.NotBlank;

public class RazorpayVerifyRequest {

    @NotBlank(message = "Account Number is required")
    private String accountNumber;

    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Signature is required")
    private String razorpaySignature;

    public RazorpayVerifyRequest() {}

    public RazorpayVerifyRequest(String accountNumber, String razorpayPaymentId, String razorpayOrderId, String razorpaySignature) {
        this.accountNumber = accountNumber;
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpaySignature = razorpaySignature;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
}
