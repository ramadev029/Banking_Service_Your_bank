package com.rc.Bank_Service.service;

import com.rc.Bank_Service.triage.repository.FailureClassificationRepository;
import com.rc.Bank_Service.triage.repository.FlakinessMetricsRepository;
import com.rc.Bank_Service.triage.service.JiraDefectDraftService;
import com.rc.Bank_Service.triage.service.TriageClassifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class TriageClassifierUnitTest {

    @Mock
    private FailureClassificationRepository failureClassificationRepository;

    @Mock
    private FlakinessMetricsRepository flakinessMetricsRepository;

    @Mock
    private JiraDefectDraftService jiraDefectDraftService;

    private TriageClassifierService triageClassifierService;

    @BeforeEach
    void setUp() {
        triageClassifierService = new TriageClassifierService(
                failureClassificationRepository,
                flakinessMetricsRepository,
                jiraDefectDraftService
        );
    }

    @Test
    void testClassifyEnvironmentIssue() {
        TriageClassifierService.ClassificationResult result = triageClassifierService.classifyFailure(
                "testDbTimeout",
                "Connection refused to database localhost:5432",
                "java.net.ConnectException"
        );

        assertNotNull(result);
        assertEquals("ENVIRONMENT_DATA_ISSUE", result.getCategory());
        assertEquals(0.95, result.getConfidenceScore());
    }

    @Test
    void testClassifyTestScriptIssue() {
        TriageClassifierService.ClassificationResult result = triageClassifierService.classifyFailure(
                "testButtonXPath",
                "NoSuchElementException: Cannot locate element with xpath //button[@id='submit']",
                "org.openqa.selenium.NoSuchElementException"
        );

        assertNotNull(result);
        assertEquals("TEST_SCRIPT_ISSUE", result.getCategory());
    }

    @Test
    void testClassifyGenuineDefect() {
        TriageClassifierService.ClassificationResult result = triageClassifierService.classifyFailure(
                "testTransferBalance",
                "AssertionError: Expected balance 4000 but was 5000",
                "java.lang.AssertionError"
        );

        assertNotNull(result);
        assertEquals("GENUINE_FUNCTIONAL_DEFECT", result.getCategory());
    }
}
