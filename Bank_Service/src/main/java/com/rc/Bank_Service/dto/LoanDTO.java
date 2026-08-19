package com.rc.Bank_Service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanDTO {

    private String loanId;
    private String loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private int tenureMonths;
    private BigDecimal monthlyEmi;
    private BigDecimal remainingBalance;
    private String status;
    private LocalDateTime createdAt;

    public LoanDTO() {}

    public LoanDTO(String loanId, String loanType, BigDecimal principalAmount, BigDecimal interestRate,
                   int tenureMonths, BigDecimal monthlyEmi, BigDecimal remainingBalance, String status, LocalDateTime createdAt) {
        this.loanId = loanId;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.monthlyEmi = monthlyEmi;
        this.remainingBalance = remainingBalance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public BigDecimal getMonthlyEmi() { return monthlyEmi; }
    public void setMonthlyEmi(BigDecimal monthlyEmi) { this.monthlyEmi = monthlyEmi; }

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
