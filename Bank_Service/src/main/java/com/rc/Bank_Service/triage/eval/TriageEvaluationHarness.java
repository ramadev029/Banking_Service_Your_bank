package com.rc.Bank_Service.triage.eval;

import com.rc.Bank_Service.triage.service.TriageClassifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TriageEvaluationHarness {

    private final TriageClassifierService triageClassifierService;

    @Autowired
    public TriageEvaluationHarness(TriageClassifierService triageClassifierService) {
        this.triageClassifierService = triageClassifierService;
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

        int correct = 0;
        for (EvaluationTestCase testCase : dataset) {
            TriageClassifierService.ClassificationResult res = triageClassifierService.classifyFailure(
                    testCase.getTestName(),
                    testCase.getErrorMessage(),
                    testCase.getStackTrace()
            );

            String predicted = res.getCategory();
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

    private List<EvaluationTestCase> getLabeledBenchmarkDataset() {
        List<EvaluationTestCase> list = new ArrayList<>();
        // Environment Issues
        list.add(new EvaluationTestCase("testDbConnection", "Connection refused to database localhost:5432", "java.net.ConnectException", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testPaymentGatewayTimeout", "HTTP 503 Service Unavailable", "java.net.SocketTimeoutException", "ENVIRONMENT_DATA_ISSUE"));
        list.add(new EvaluationTestCase("testAuthTimeout", "SocketTimeoutException: Read timed out", "java.net.SocketTimeoutException", "ENVIRONMENT_DATA_ISSUE"));

        // Script Issues
        list.add(new EvaluationTestCase("testLoginButtonCss", "NoSuchElementException: Cannot locate element with xpath //button[@id='submit']", "org.openqa.selenium.NoSuchElementException", "TEST_SCRIPT_ISSUE"));
        list.add(new EvaluationTestCase("testDashboardNav", "StaleElementReferenceException: Element is no longer attached to DOM", "org.openqa.selenium.StaleElementReferenceException", "TEST_SCRIPT_ISSUE"));

        // Functional Defects
        list.add(new EvaluationTestCase("testTransferInsufficientBalance", "AssertionError: Expected balance 4000 but was 5000", "java.lang.AssertionError", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testAadhaarValidation", "Status expected:<201> but was:<409>", "org.opentest4j.AssertionFailedError", "GENUINE_FUNCTIONAL_DEFECT"));
        list.add(new EvaluationTestCase("testMpinAuthorization", "HTTP 401 Unauthorized for valid MPIN payload", "java.lang.AssertionError", "GENUINE_FUNCTIONAL_DEFECT"));

        return list;
    }
}
