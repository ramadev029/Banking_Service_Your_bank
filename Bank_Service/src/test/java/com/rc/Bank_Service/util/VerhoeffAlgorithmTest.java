package com.rc.Bank_Service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VerhoeffAlgorithmTest {

    @Test
    @DisplayName("Should reject invalid Aadhaar numbers with bad checksums or bad formats")
    void testInvalidAadhaarNumbers() {
        assertFalse(VerhoeffAlgorithm.validateAadhaar("123456789012")); // Starts with 1
        assertFalse(VerhoeffAlgorithm.validateAadhaar("12345")); // Bad length
        assertFalse(VerhoeffAlgorithm.validateAadhaar(null)); // Null
    }
}
