package com.rc.Bank_Service.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SampleRegressionFailureTest {

    @Test
    void testTransferInsufficientBalanceFailure() {
        // Simulates a Genuine Functional Business Logic Defect
        assertEquals(4000, 5000, "AssertionError: Expected balance 4000 after transfer but was 5000");
    }

    @Test
    void testLoginButtonCssSelectorFailure() {
        // Simulates a Test Script Locator Issue
        fail("NoSuchElementException: Cannot locate element with xpath //button[@id='submit-login-v2']");
    }

    @Test
    void testDatabaseTimeoutFailure() {
        // Simulates an Infrastructure Environment Issue
        fail("ConnectException: Connection refused to database server localhost:5432");
    }
}
