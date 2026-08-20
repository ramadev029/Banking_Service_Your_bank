package com.rc.Bank_Service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class EvaluatorPipelineTest {

    @Test
    @DisplayName("Failure 1: Genuine Functional Defect - Account Balance Exceeded")
    public void testTransferAmountExceedsBalanceFailure() {
        double currentBalance = 200.0;
        double transferAmount = 500.0;
        
        // Simulating TrustBank failure where $500 transfer on $200 balance was allowed
        double resultingBalance = currentBalance - transferAmount; // -300.0
        
        // Assertion fails because negative balance is illegal in banking logic
        assertTrue(resultingBalance >= 0.0, 
            "AssertionError: Expected balance >= 0.0 after transfer of $" + transferAmount + " but resulting balance was $" + resultingBalance);
    }

    @Test
    @DisplayName("Failure 2: Test Script Issue - Broken XPath Locator")
    public void testOpenTransferScreenButtonLocatorFailure() {
        boolean buttonFound = false;
        if (!buttonFound) {
            fail("org.openqa.selenium.NoSuchElementException: Cannot locate element with xpath //button[@id='transfer-btn'] - button was renamed to 'send-money-btn'");
        }
    }

    @Test
    @DisplayName("Failure 3: Environment Problem - Core Banking System Connection Timeout")
    public void testCoreBankingDatabaseConnectionTimeoutFailure() {
        boolean connectionEstablished = false;
        if (!connectionEstablished) {
            fail("java.net.SocketTimeoutException: Connection refused to core banking system database at port 5432 - server unreachable");
        }
    }

    @Test
    @DisplayName("Failure 4: Genuine Functional Defect - Invalid MPIN Auth Rejection")
    public void testInvalidMpinRejectionFailure() {
        boolean accessGranted = true; // Bug in auth endpoint: granted access on invalid MPIN
        assertFalse(accessGranted, 
            "AssertionError: Expected MPIN authentication rejection for invalid MPIN '111111' but access was GRANTED");
    }
}
