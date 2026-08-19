package com.rc.Bank_Service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentRequest {

    @NotBlank(message = "Account Number is required")
    private String accountNumber;

    private String recipient;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum payment amount is ₹1.00")
    private BigDecimal amount;

    private String remarks;

    private String mpin;

    private String transferMode; // UPI, IMPS, NEFT, RTGS, NETBANKING

    private String ifscCode;

    private String beneficiaryName;

    public PaymentRequest() {}

    public PaymentRequest(String accountNumber, String recipient, BigDecimal amount, String remarks, String mpin) {
        this.accountNumber = accountNumber;
        this.recipient = recipient;
        this.amount = amount;
        this.remarks = remarks;
        this.mpin = mpin;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getMpin() { return mpin; }
    public void setMpin(String mpin) { this.mpin = mpin; }

    public String getTransferMode() { return transferMode; }
    public void setTransferMode(String transferMode) { this.transferMode = transferMode; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
}
