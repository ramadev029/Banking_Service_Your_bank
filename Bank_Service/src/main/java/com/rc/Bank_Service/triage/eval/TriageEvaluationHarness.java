package com.rc.Bank_Service.triage.eval;

import com.rc.Bank_Service.triage.model.TestExecutionHistory;
import com.rc.Bank_Service.triage.model.TestRun;
import com.rc.Bank_Service.triage.repository.TestExecutionHistoryRepository;
import com.rc.Bank_Service.triage.repository.TestRunRepository;
import com.rc.Bank_Service.triage.service.FlakinessTrackerService;
import com.rc.Bank_Service.triage.service.TriageClassifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TriageEvaluationHarness {

    private final TriageClassifierService triageClassifierService;
    private final TestRunRepository testRunRepository;
    private final TestExecutionHistoryRepository testExecutionHistoryRepository;
    private final FlakinessTrackerService flakinessTrackerService;

    @Autowired
    public TriageEvaluationHarness(TriageClassifierService triageClassifierService,
                                   TestRunRepository testRunRepository,
                                   TestExecutionHistoryRepository testExecutionHistoryRepository,
                                   FlakinessTrackerService flakinessTrackerService) {
        this.triageClassifierService = triageClassifierService;
        this.testRunRepository = testRunRepository;
        this.testExecutionHistoryRepository = testExecutionHistoryRepository;
        this.flakinessTrackerService = flakinessTrackerService;
    }

    public static class EvaluationTestCase {
        private String testName;
        private String errorMessage;
        private String stackTrace;
        private String expectedCategory;

        public EvaluationTestCase(String testName, String errorMessage, String stackTrace, String expectedCategory) {
            this.testName = testName;
            this.errorMessage = errorMessage;
            this.stackTrace = stackTrace;
            this.expectedCategory = expectedCategory;
        }

        public String getTestName() { return testName; }
        public String getErrorMessage() { return errorMessage; }
        public String getStackTrace() { return stackTrace; }
        public String getExpectedCategory() { return expectedCategory; }
    }

    public static class EvaluationResult {
        private int totalCases;
        private int correctPredictions;
        private double accuracyPercentage;
        private Map<String, Map<String, Integer>> confusionMatrix;
        private Map<String, Double> precisionPerCategory;
        private Map<String, Double> recallPerCategory;
        private Map<String, Double> f1PerCategory;

        public EvaluationResult(int totalCases, int correctPredictions, double accuracyPercentage,
                                Map<String, Map<String, Integer>> confusionMatrix,
                                Map<String, Double> precisionPerCategory,
                                Map<String, Double> recallPerCategory,
                                Map<String, Double> f1PerCategory) {
            this.totalCases = totalCases;
            this.correctPredictions = correctPredictions;
            this.accuracyPercentage = accuracyPercentage;
            this.confusionMatrix = confusionMatrix;
            this.precisionPerCategory = precisionPerCategory;
            this.recallPerCategory = recallPerCategory;
            this.f1PerCategory = f1PerCategory;
        }

        public int getTotalCases() { return totalCases; }
        public int getCorrectPredictions() { return correctPredictions; }
        public double getAccuracyPercentage() { return accuracyPercentage; }
        public Map<String, Map<String, Integer>> getConfusionMatrix() { return confusionMatrix; }
        public Map<String, Double> getPrecisionPerCategory() { return precisionPerCategory; }
        public Map<String, Double> getRecallPerCategory() { return recallPerCategory; }
        public Map<String, Double> getF1PerCategory() { return f1PerCategory; }
    }

    public EvaluationResult runEvaluation() {
        List<EvaluationTestCase> dataset = getLabeledBenchmarkDataset();
        List<String> categories = List.of(
                "GENUINE_FUNCTIONAL_DEFECT",
                "FLAKY_UNSTABLE_TEST",
                "ENVIRONMENT_DATA_ISSUE",
                "TEST_SCRIPT_ISSUE"
        );

        Map<String, Map<String, Integer>> matrix = new HashMap<>();
        for (String actual : categories) {
            matrix.put(actual, new HashMap<>());
            for (String pred : categories) {
                matrix.get(actual).put(pred, 0);
            }
        }

        // Save Evaluation Test Run to Database
        TestRun evalRun = new TestRun("Module 7 Benchmark Evaluation Suite", dataset.size(), 0, dataset.size(), 8500);
        testRunRepository.save(evalRun);

        int correct = 0;
        for (EvaluationTestCase testCase : dataset) {
            // Save execution history record
            TestExecutionHistory history = new TestExecutionHistory(
                    testCase.getTestName(),
                    "TrustBankBenchmarkSuite",
                    "FAIL",
                    testCase.getErrorMessage(),
                    testCase.getStackTrace(),
                    120
            );
            testExecutionHistoryRepository.save(history);
            flakinessTrackerService.updateFlakinessScore(testCase.getTestName());

            com.rc.Bank_Service.triage.model.FailureClassification classification = triageClassifierService.classifyAndSave(
                    testCase.getTestName(),
                    testCase.getErrorMessage(),
                    testCase.getStackTrace(),
                    true,
                    "Module 7 Benchmark Evaluation Suite"
            );

            String predicted = classification.getCategory();
            String expected = testCase.getExpectedCategory();

            if (matrix.containsKey(expected) && matrix.get(expected).containsKey(predicted)) {
                matrix.get(expected).put(predicted, matrix.get(expected).get(predicted) + 1);
            }

            if (expected.equalsIgnoreCase(predicted)) {
                correct++;
            }
        }

        Map<String, Double> precision = new HashMap<>();
        Map<String, Double> recall = new HashMap<>();
        Map<String, Double> f1 = new HashMap<>();

        for (String cat : categories) {
            int tp = matrix.get(cat).get(cat);
            int fp = 0;
            int fn = 0;

            for (String otherCat : categories) {
                if (!otherCat.equals(cat)) {
                    fp += matrix.get(otherCat).get(cat);
                    fn += matrix.get(cat).get(otherCat);
                }
            }

            double precVal = (tp + fp) > 0 ? (double) tp / (tp + fp) : 1.0;
            double recVal = (tp + fn) > 0 ? (double) tp / (tp + fn) : 1.0;
            double f1Val = (precVal + recVal) > 0 ? 2 * (precVal * recVal) / (precVal + recVal) : 1.0;

            precision.put(cat, precVal * 100.0);
            recall.put(cat, recVal * 100.0);
            f1.put(cat, f1Val * 100.0);
        }

        double accuracy = dataset.size() > 0 ? ((double) correct / dataset.size()) * 100.0 : 100.0;
        return new EvaluationResult(dataset.size(), correct, accuracy, matrix, precision, recall, f1);
    }

    public List<EvaluationTestCase> getLabeledBenchmarkDataset() {
        List<EvaluationTestCase> list = new ArrayList<>();

        // Category 1: GENUINE_FUNCTIONAL_DEFECT (12 Labeled Benchmark Cases)
        list.add(new EvaluationTestCase("transfer_amount_cannot_exceed_balance", "AssertionError: Expected balance $200 after transfer attempt of $500 but was -$300", "java.lang.AssertionError: Balance negative", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testSignUpDuplicateAadhaarConflict", "HTTP 409 Conflict: An account is already registered with Aadhaar Number", "org.springframework.web.client.HttpClientErrorException$Conflict", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testMpinVerificationFailure", "AssertionError: Invalid MPIN accepted for account CIF984021", "java.lang.AssertionError: Accepted invalid 4-digit PIN", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testAadhaarVerhoeffChecksumValidation", "Status expected:<409> but was:<201>", "org.opentest4j.AssertionFailedError: Invalid Aadhaar bypassed check", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testFundTransferZeroAmountRejection", "AssertionError: Transfer of zero rupees allowed", "java.lang.AssertionError: Zero transfer allowed", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testDailyTransferLimitExceeded", "AssertionError: Daily transfer limit of 100,000 INR bypassed", "java.lang.AssertionError: Limit bypassed", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testDepositNegativeAmountRejection", "AssertionError: Negative deposit of -1000 accepted", "java.lang.AssertionError: Negative balance credited", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testDebitCardVirtualPinMismatch", "Status expected:<400> but was:<200>", "org.opentest4j.AssertionFailedError", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testLoanEmiInterestCalculation", "AssertionError: Expected EMI 4520.00 but calculated 3200.00", "java.lang.AssertionError: Interest logic bug", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testInsuranceCoveragePolicyMismatch", "AssertionError: Policy active without premium deduction", "java.lang.AssertionError: Premium missing", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testAccountStatusSuspendedTransferRejection", "Status expected:<403> but was:<200>", "org.opentest4j.AssertionFailedError: Suspended account transferred funds", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testStatementDateRangeFilter", "AssertionError: Expected 3 transactions but received 0", "java.lang.AssertionError: Date boundary error", "GENUINE_FUNCTIONAL_DEFECT"));

        // Category 2: FLAKY_UNSTABLE_TEST (12 Labeled Benchmark Cases)
        list.add(new EvaluationTestCase("balance_updates_after_transfer", "AssertionError: Expected balance 7500.00 but was 8000.00 (Async Screen Refresh Delayed)", "org.opentest4j.AssertionFailedError: Screen refresh pending", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testTransactionHistoryAsyncPolling", "ConditionTimeoutException: Condition was not fulfilled within 2 seconds", "org.awaitility.core.ConditionTimeoutException", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testNotificationToastAnimation", "ElementNotInteractableException: element not interactable (animating)", "org.openqa.selenium.ElementNotInteractableException", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testOtpTimerCountdown", "AssertionError: OTP timer expected 59s but was 60s", "java.lang.AssertionError: Race condition in timer", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testRazorpayWebhookAckDelay", "TimeoutException: Webhook acknowledgement not received within 1500ms", "java.util.concurrent.TimeoutException", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testDashboardChartRenderTiming", "NullPointerException: Chart container height undefined during mount", "java.lang.NullPointerException: Component state transient", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testMpinModalTransition", "StaleElementReferenceException: Element is no longer attached to DOM", "org.openqa.selenium.StaleElementReferenceException", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testSessionTimeoutPopupAsync", "AssertionError: Session popup missing (rendered 100ms late)", "java.lang.AssertionError", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testPdfDownloadStreamComplete", "IOException: Premature end of Content-Length delimited stream", "java.io.IOException: Intermittent stream close", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testCustomerProfileImageLoad", "TimeoutException: Image load timed out", "java.util.concurrent.TimeoutException", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testAccountBalanceBadgeRefresh", "AssertionError: Expected badge update in 500ms", "java.lang.AssertionError: Async render race", "FLAKY_UNSTABLE_TEST"));
        list.add(new EvaluationTestCase("testBeneficiaryListOrderFlake", "AssertionError: Sorting order mismatch due to concurrent insert", "java.lang.AssertionError", "FLAKY_UNSTABLE_TEST"));

        // Category 3: ENVIRONMENT_DATA_ISSUE (12 Labeled Benchmark Cases)
        list.add(new EvaluationTestCase("testCoreBankingSystemConnection", "ConnectException: Connection refused to core banking system localhost:5432", "java.net.ConnectException: Connection refused", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testPaymentGatewayServiceUnavailable", "HTTP 503 Service Unavailable: Core Banking Gateway Unreachable", "org.springframework.web.client.HttpServerErrorException$ServiceUnavailable", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testDatabaseConnectionPoolTimeout", "HikariPool-1 - Connection is not available, request timed out after 30000ms", "com.zaxxer.hikari.pool.HikariPool$PoolInitializationException", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testSocketTimeoutExceptionCoreBank", "SocketTimeoutException: Read timed out to core banking API", "java.net.SocketTimeoutException: Read timed out", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testRedisCacheHostUnreachable", "RedisConnectionException: Unable to connect to redis-cluster:6379", "io.lettuce.core.RedisConnectionException", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testKafkaMessageBrokerDown", "TimeoutException: Failed to update metadata after 60000 ms", "org.apache.kafka.common.errors.TimeoutException", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testPostgresqlStorageQuotaExceeded", "PSQLException: FATAL: disk full on database server", "org.postgresql.util.PSQLException", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testExternalCibilScoreApi500", "HTTP 500 Internal Server Error: CIBIL Score API Unreachable", "org.springframework.web.client.HttpServerErrorException$InternalServerError", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testSmtpMailServerRefused", "MailSendException: Mail server connection failed; nested exception is java.net.ConnectException: Connection refused", "org.springframework.mail.MailSendException", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testAadhaarVaultServiceTimeout", "SocketTimeoutException: Could not connect to UIDAI Vault Server", "java.net.SocketTimeoutException", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testRazorpaySandBoxGatewayTimeout", "HTTP 504 Gateway Timeout: Razorpay Sandbox API unreachable", "org.springframework.web.client.HttpServerErrorException$GatewayTimeout", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testSystemClockDesynchronization", "ClockSkewException: Client and Server time offset exceeds 300 seconds", "java.lang.IllegalStateException: Clock skew", "ENVIRONMENT_DATA_ISSUE"));

        // Category 4: TEST_SCRIPT_ISSUE (12 Labeled Benchmark Cases)
        list.add(new EvaluationTestCase("open_transfer_screen", "NoSuchElementException: Cannot locate element with xpath //button[@id='transfer-btn'] (Renamed to send-money-btn)", "org.openqa.selenium.NoSuchElementException: Element transfer-btn missing", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testLoginButtonXPathMismatch", "NoSuchElementException: Cannot locate element with css selector #old-login-btn", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testDashboardNavSelectorInvalid", "InvalidSelectorException: The given selector //div[contains(@class, legacy-nav)] is invalid", "org.openqa.selenium.InvalidSelectorException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testSignUpFormInputFieldIdChange", "NoSuchElementException: Unable to find element with id 'input_aadhaar_legacy'", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testMpinModalInputCssMismatch", "NoSuchElementException: Cannot locate element with class '.mpin-digit-legacy'", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testAccountCardComponentXPathChanged", "StaleElementReferenceException: Element is no longer attached to DOM after UI overhaul", "org.openqa.selenium.StaleElementReferenceException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testStatementDownloadButtonRenamed", "NoSuchElementException: Unable to locate element //button[text()='Download PDF'] (Renamed to 'Export Statement')", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testDebitCardTabIdUpdated", "NoSuchElementException: Unable to locate element #cards-tab-v1", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testRazorpayModalButtonIdMismatch", "NoSuchElementException: Unable to locate element #rzp-pay-button-old", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testCustomerAvatarSelectorUpdated", "NoSuchElementException: Unable to locate element .user-profile-img-old", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testLoanApplyButtonXPathRenamed", "NoSuchElementException: Unable to locate element //button[@name='apply-loan-old']", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testLogoutButtonCssClassUpdated", "NoSuchElementException: Unable to locate element .btn-logout-v1", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));

        return list;
    }
}
