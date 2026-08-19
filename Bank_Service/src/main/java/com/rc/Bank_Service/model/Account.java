package com.rc.Bank_Service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "cif_number", nullable = false, length = 20)
    private String cifNumber;

    @Column(name = "account_number", nullable = false, unique = true, length = 12)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_code", nullable = false)
    private Branch branch;

    @Column(name = "ifsc_code", nullable = false, length = 11)
    private String ifscCode;

    @Column(name = "account_type", nullable = false, length = 30)
    private String accountType = "SAVINGS_REGULAR";

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "upi_vpa", nullable = false, unique = true)
    private String upiVpa;

    @Column(name = "account_status", nullable = false, length = 20)
    private String accountStatus = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Account() {}

    public Account(User user, String cifNumber, String accountNumber, Branch branch, String ifscCode,
                   String accountType, BigDecimal balance, String upiVpa) {
        this.user = user;
        this.cifNumber = cifNumber;
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.ifscCode = ifscCode;
        this.accountType = accountType;
        this.balance = balance;
        this.currency = "INR";
        this.upiVpa = upiVpa;
        this.accountStatus = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCifNumber() { return cifNumber; }
    public void setCifNumber(String cifNumber) { this.cifNumber = cifNumber; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getUpiVpa() { return upiVpa; }
    public void setUpiVpa(String upiVpa) { this.upiVpa = upiVpa; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
