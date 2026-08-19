package com.rc.Bank_Service.dto;

import java.math.BigDecimal;

public class SignUpResponse {
    private String message;
    private Long userId;
    private String cifNumber;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String accountNumber;
    private String branchName;
    private String ifscCode;
    private String accountType;
    private BigDecimal balance;
    private String upiVpa;
    private String kycStatus;
    private BigDecimal accountLimit;
    private DebitCardDTO debitCard;

    public SignUpResponse() {}

    public SignUpResponse(String message, Long userId, String cifNumber, String fullName, String email,
                          String phoneNumber, String accountNumber, String branchName, String ifscCode,
                          String accountType, BigDecimal balance, String upiVpa, String kycStatus,
                          BigDecimal accountLimit, DebitCardDTO debitCard) {
        this.message = message;
        this.userId = userId;
        this.cifNumber = cifNumber;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accountNumber = accountNumber;
        this.branchName = branchName;
        this.ifscCode = ifscCode;
        this.accountType = accountType;
        this.balance = balance;
        this.upiVpa = upiVpa;
        this.kycStatus = kycStatus;
        this.accountLimit = accountLimit;
        this.debitCard = debitCard;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCifNumber() { return cifNumber; }
    public void setCifNumber(String cifNumber) { this.cifNumber = cifNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getUpiVpa() { return upiVpa; }
    public void setUpiVpa(String upiVpa) { this.upiVpa = upiVpa; }

    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

    public BigDecimal getAccountLimit() { return accountLimit; }
    public void setAccountLimit(BigDecimal accountLimit) { this.accountLimit = accountLimit; }

    public DebitCardDTO getDebitCard() { return debitCard; }
    public void setDebitCard(DebitCardDTO debitCard) { this.debitCard = debitCard; }
}
