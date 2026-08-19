package com.rc.Bank_Service.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class SignUpRequest {

    @NotBlank(message = "Full legal name is required")
    @Size(min = 2, max = 100, message = "Full legal name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email address format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @NotBlank(message = "PAN Number is mandatory for digital account opening")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN format (e.g. ABCDE1234F)")
    private String panNumber;

    @NotBlank(message = "Aadhaar Number is mandatory for digital eKYC verification")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Invalid 12-digit Aadhaar format")
    private String aadhaarNumber;

    @NotNull(message = "Date of Birth is required")
    @Past(message = "Date of Birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender selection is required")
    private String gender;

    @NotBlank(message = "Residential address is required")
    private String address;

    private String accountType = "SAVINGS_REGULAR";

    @Pattern(regexp = "^[0-9]{6}$", message = "MPIN must be exactly 6 digits")
    private String mpin;

    public SignUpRequest() {}

    public SignUpRequest(String fullName, String email, String password, String phoneNumber, String panNumber,
                         String aadhaarNumber, LocalDate dateOfBirth, String gender, String address, String accountType, String mpin) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.panNumber = panNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.accountType = accountType != null ? accountType : "SAVINGS_REGULAR";
        this.mpin = mpin;
    }

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

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getMpin() { return mpin; }
    public void setMpin(String mpin) { this.mpin = mpin; }
}
