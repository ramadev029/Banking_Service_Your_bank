package com.rc.Bank_Service.dto;

import java.math.BigDecimal;
import java.util.List;

public class CustomerDashboardDTO {

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
    private List<TransactionDTO> transactions;
    private List<LoanDTO> loans;
    private List<InsuranceDTO> insurances;

    public CustomerDashboardDTO() {}

    public CustomerDashboardDTO(String cifNumber, String fullName, String email, String phoneNumber,
                                String accountNumber, String branchName, String ifscCode, String accountType,
                                BigDecimal balance, String upiVpa, String kycStatus, BigDecimal accountLimit,
                                DebitCardDTO debitCard, List<TransactionDTO> transactions,
                                List<LoanDTO> loans, List<InsuranceDTO> insurances) {
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
        this.transactions = transactions;
        this.loans = loans;
        this.insurances = insurances;
    }

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

    public List<TransactionDTO> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionDTO> transactions) { this.transactions = transactions; }

    public List<LoanDTO> getLoans() { return loans; }
    public void setLoans(List<LoanDTO> loans) { this.loans = loans; }

    public List<InsuranceDTO> getInsurances() { return insurances; }
    public void setInsurances(List<InsuranceDTO> insurances) { this.insurances = insurances; }
}
