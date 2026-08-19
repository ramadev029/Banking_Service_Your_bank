package com.rc.Bank_Service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AadhaarOtpRequest {

    @NotBlank(message = "Aadhaar Number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Invalid 12-digit Aadhaar format")
    private String aadhaarNumber;

    private String txnId;
    private String otp;

    public AadhaarOtpRequest() {}

    public AadhaarOtpRequest(String aadhaarNumber, String txnId, String otp) {
        this.aadhaarNumber = aadhaarNumber;
        this.txnId = txnId;
        this.otp = otp;
    }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
