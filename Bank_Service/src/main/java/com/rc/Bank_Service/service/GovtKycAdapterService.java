package com.rc.Bank_Service.service;

import com.rc.Bank_Service.dto.AadhaarOtpRequest;
import com.rc.Bank_Service.dto.AadhaarOtpResponse;
import com.rc.Bank_Service.util.JaroWinklerDistance;
import com.rc.Bank_Service.util.VerhoeffAlgorithm;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GovtKycAdapterService {

    // Concurrent in-memory store for active eKYC OTP transactions
    private final Map<String, String> activeOtpStore = new ConcurrentHashMap<>();
    private final Map<String, String> verifiedTokenStore = new ConcurrentHashMap<>();

    /**
     * Legal Age Validation (>= 18 Years)
     */
    public void validateLegalAge(LocalDate dob) {
        if (dob == null) {
            throw new IllegalArgumentException("Date of Birth is required.");
        }
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException(
                "Instant Digital Account creation is legally restricted to individuals 18 years or older (Current Age: " 
                + age + "). Minors must apply at a physical branch with a parent/guardian."
            );
        }
    }

    /**
     * PAN Entity Type Verification (4th character MUST be 'P' for Individual)
     */
    public void validatePanEntity(String panNumber) {
        if (panNumber == null || panNumber.trim().length() != 10) {
            throw new IllegalArgumentException("Invalid PAN Number format.");
        }
        char entityChar = Character.toUpperCase(panNumber.trim().charAt(3));
        if (entityChar != 'P') {
            throw new IllegalArgumentException(
                "The PAN provided (" + panNumber.trim().toUpperCase() + ") belongs to a Company/Firm/HUF (Entity code '" + entityChar + "'). "
                + "Personal Savings Accounts require an Individual Personal PAN (4th character 'P')."
            );
        }
    }

    /**
     * Jaro-Winkler String Similarity Name Matching (>= 80% Match)
     */
    public void validateNameSimilarity(String customerEnteredName, String panTaxRecordName) {
        double score = JaroWinklerDistance.compareNames(customerEnteredName, panTaxRecordName);
        if (score < 0.80) {
            throw new IllegalArgumentException(
                String.format("Identity Verification Failed! Entered name '%s' does not match the registered tax name '%s' (Confidence Score: %.0f%%, Required: 80%%+).",
                    customerEnteredName, panTaxRecordName, score * 100)
            );
        }
    }

    /**
     * Triggers 2-Factor Aadhaar OTP
     */
    public AadhaarOtpResponse triggerAadhaarOtp(AadhaarOtpRequest request) {
        String aadhaar = request.getAadhaarNumber().trim();
        if (!VerhoeffAlgorithm.validateAadhaar(aadhaar)) {
            throw new IllegalArgumentException("Invalid Aadhaar Number! Checksum validation failed.");
        }

        String txnId = "TXN-EKYC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String defaultSandboxOtp = "123456"; // Standardized Sandbox 2FA OTP

        activeOtpStore.put(txnId, defaultSandboxOtp);

        String last4 = aadhaar.substring(aadhaar.length() - 4);
        String maskedMobile = "******" + (9000 + Integer.parseInt(last4) % 1000);

        return new AadhaarOtpResponse(
            txnId,
            maskedMobile,
            "2FA Aadhaar OTP sent to registered mobile " + maskedMobile + " (Sandbox Test OTP: 123456)",
            false,
            null
        );
    }

    /**
     * Verifies 2-Factor Aadhaar OTP
     */
    public AadhaarOtpResponse verifyAadhaarOtp(AadhaarOtpRequest request) {
        String txnId = request.getTxnId();
        String userOtp = request.getOtp();

        if (txnId == null || !activeOtpStore.containsKey(txnId)) {
            throw new IllegalArgumentException("Invalid or expired eKYC transaction session. Please request a new OTP.");
        }

        String expectedOtp = activeOtpStore.get(txnId);
        if (!expectedOtp.equals(userOtp != null ? userOtp.trim() : "")) {
            throw new IllegalArgumentException("Invalid Aadhaar OTP! Please check the 6-digit code and try again.");
        }

        activeOtpStore.remove(txnId);
        String ekycToken = "EKYC-TOKEN-VERIFIED-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        verifiedTokenStore.put(ekycToken, "VERIFIED");

        return new AadhaarOtpResponse(
            txnId,
            null,
            "Aadhaar eKYC successfully verified!",
            true,
            ekycToken
        );
    }

    public boolean isTokenValid(String token) {
        return token != null && verifiedTokenStore.containsKey(token);
    }
}
