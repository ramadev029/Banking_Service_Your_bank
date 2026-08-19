package com.rc.Bank_Service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cif_number", nullable = false, unique = true, length = 20)
    private String cifNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number", nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @Column(name = "pan_number", nullable = false, unique = true, length = 10)
    private String panNumber;

    @Column(name = "aadhaar_number", nullable = false, unique = true, length = 50)
    private String aadhaarNumber;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "kyc_status", nullable = false)
    private String kycStatus = "LIMITED_KYC_EKYC";

    @Column(name = "account_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal accountLimit = new BigDecimal("100000.00");

    @Column(name = "role", nullable = false)
    private String role = "ROLE_CUSTOMER";

    @Column(name = "mpin_hash", length = 255)
    private String mpinHash;

    @Column(name = "mpin_failed_attempts", nullable = false)
    private int mpinFailedAttempts = 0;

    @Column(name = "mpin_locked_until")
    private LocalDateTime mpinLockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String cifNumber, String fullName, String email, String password, String phoneNumber,
                String panNumber, String aadhaarNumber, LocalDate dateOfBirth, String gender, String address) {
        this.cifNumber = cifNumber;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.panNumber = panNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.kycStatus = "LIMITED_KYC_EKYC";
        this.accountLimit = new BigDecimal("100000.00");
        this.role = "ROLE_CUSTOMER";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCifNumber() { return cifNumber; }
    public void setCifNumber(String cifNumber) { this.cifNumber = cifNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

    public BigDecimal getAccountLimit() { return accountLimit; }
    public void setAccountLimit(BigDecimal accountLimit) { this.accountLimit = accountLimit; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMpinHash() { return mpinHash; }
    public void setMpinHash(String mpinHash) { this.mpinHash = mpinHash; }

    public int getMpinFailedAttempts() { return mpinFailedAttempts; }
    public void setMpinFailedAttempts(int mpinFailedAttempts) { this.mpinFailedAttempts = mpinFailedAttempts; }

    public LocalDateTime getMpinLockedUntil() { return mpinLockedUntil; }
    public void setMpinLockedUntil(LocalDateTime mpinLockedUntil) { this.mpinLockedUntil = mpinLockedUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
