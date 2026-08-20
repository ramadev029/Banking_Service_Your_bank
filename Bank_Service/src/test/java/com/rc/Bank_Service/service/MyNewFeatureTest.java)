package com.rc.Bank_Service.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyNewFeatureTest {

    @Test
    void testNewPaymentGatewayTimeout() {
        fail("ConnectException: Connection timed out to gateway api.paygate.com:443 after 5000ms");
    }

    @Test
    void testNewAccountLimitValidationFailure() {
        assertEquals(50000, 25000, "AssertionError: Daily transfer limit exceeded for account CIF-9999");
    }
}