package com.rc.Bank_Service.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class EvaluatorCustomTest {

    //  Test Case 1: Evaluator tests a Functional Business Defect
    @Test
    void testCustomAccountBalanceFailure() {
        assertEquals(10000, 8500, "AssertionError: Expected balance 10000 after deposit but was 8500");
    }

    //  Test Case 2: Evaluator tests a Broken UI Locator (Script Issue)
    @Test
    void testCustomElementNotFoundFailure() {
        fail("NoSuchElementException: Cannot locate element with xpath //input[@name='accountNumberV2']");
    }

    // 🌐 Test Case 3: Evaluator tests a Network / Server Outage
    @Test
    void testCustomServerTimeoutFailure() {
        fail("ConnectException: Connection refused to payment server api.yourbank.com:8443");
    }
}