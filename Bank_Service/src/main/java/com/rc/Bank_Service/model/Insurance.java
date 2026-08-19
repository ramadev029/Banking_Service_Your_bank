package com.rc.Bank_Service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurances")
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true, length = 30)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "coverage_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "monthly_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPremium;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Insurance() {}

    public Insurance(String policyNumber, Account account, String planName,
                     BigDecimal coverageAmount, BigDecimal monthlyPremium) {
        this.policyNumber = policyNumber;
        this.account = account;
        this.planName = planName;
        this.coverageAmount = coverageAmount;
        this.monthlyPremium = monthlyPremium;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

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
