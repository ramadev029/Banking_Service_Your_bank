package com.rc.Bank_Service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InsuranceDTO {

    private String policyNumber;
    private String planName;
    private BigDecimal coverageAmount;
    private BigDecimal monthlyPremium;
    private String status;
    private LocalDateTime createdAt;

    public InsuranceDTO() {}

    public InsuranceDTO(String policyNumber, String planName, BigDecimal coverageAmount,
                        BigDecimal monthlyPremium, String status, LocalDateTime createdAt) {
        this.policyNumber = policyNumber;
        this.planName = planName;
        this.coverageAmount = coverageAmount;
        this.monthlyPremium = monthlyPremium;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public BigDecimal getCoverageAmount() { return coverageAmount; }
    public void setCoverageAmount(BigDecimal coverageAmount) { this.coverageAmount = coverageAmount; }

    public BigDecimal getMonthlyPremium() { return monthlyPremium; }
    public void setMonthlyPremium(BigDecimal monthlyPremium) { this.monthlyPremium = monthlyPremium; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
