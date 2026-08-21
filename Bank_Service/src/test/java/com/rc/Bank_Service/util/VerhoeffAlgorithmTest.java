package com.rc.Bank_Service.util;

import com.rc.Bank_Service.util.VerhoeffAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VerhoeffAlgorithmTest {

    @Test
    @DisplayName("Should generate valid Aadhaar numbers that pass Verhoeff validation")
    void testGenerateValidAadhaar() {
        for (int i = 0; i < 100; i++) {
            String aadhaar = VerhoeffAlgorithm.generateValidAadhaar();
            assertTrue(VerhoeffAlgorithm.validateAadhaar(aadhaar), "Generated Aadhaar " + aadhaar + " failed validation!");
        }
    }
}
