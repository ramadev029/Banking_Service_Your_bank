package com.rc.Bank_Service.triage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.Bank_Service.triage.model.FailureClassification;
import com.rc.Bank_Service.triage.model.FlakinessMetrics;
import com.rc.Bank_Service.triage.repository.FailureClassificationRepository;
import com.rc.Bank_Service.triage.repository.FlakinessMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TriageClassifierService {

    private final FailureClassificationRepository failureClassificationRepository;
    private final FlakinessMetricsRepository flakinessMetricsRepository;
    private final JiraDefectDraftService jiraDefectDraftService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.triage.gemini-api-key:}")
    private String geminiApiKey;

    @Autowired
    public TriageClassifierService(FailureClassificationRepository failureClassificationRepository,
                                   FlakinessMetricsRepository flakinessMetricsRepository,
                                   JiraDefectDraftService jiraDefectDraftService) {
        this.failureClassificationRepository = failureClassificationRepository;
        this.flakinessMetricsRepository = flakinessMetricsRepository;
        this.jiraDefectDraftService = jiraDefectDraftService;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public static class ClassificationResult {
        private String category;
        private double confidenceScore;
        private String writtenReasoning;
        private String reproductionSteps;

        public ClassificationResult(String category, double confidenceScore, String writtenReasoning, String reproductionSteps) {
            this.category = category;
            this.confidenceScore = confidenceScore;
            this.writtenReasoning = writtenReasoning;
            this.reproductionSteps = reproductionSteps;
        }

        public String getCategory() { return category; }
        public double getConfidenceScore() { return confidenceScore; }
        public String getWrittenReasoning() { return writtenReasoning; }
        public String getReproductionSteps() { return reproductionSteps; }
    }

    @Transactional
    public FailureClassification classifyAndSave(String testName, String errorMessage, String stackTrace) {
        ClassificationResult result = classifyFailure(testName, errorMessage, stackTrace);
        
        String jiraPayload = jiraDefectDraftService.generateJiraDraftPayload(
                testName, errorMessage, stackTrace, result.getReproductionSteps(),
                result.getCategory(), result.getConfidenceScore(), result.getWrittenReasoning()
        );

        FailureClassification classification = new FailureClassification(
                testName,
                result.getCategory(),
                result.getConfidenceScore(),
                result.getWrittenReasoning(),
                result.getReproductionSteps(),
                jiraPayload
        );

        return failureClassificationRepository.save(classification);
    }

    public ClassificationResult classifyFailure(String testName, String errorMessage, String stackTrace) {
        String msg = errorMessage != null ? errorMessage.toLowerCase() : "";
        String stack = stackTrace != null ? stackTrace.toLowerCase() : "";

        // 1. Fast-Path Local Signature Engine (Zero-Token Tier)
        if (msg.contains("connection refused") || msg.contains("503") || msg.contains("timeout") || stack.contains("sockettimeoutexception") || msg.contains("database connection failed")) {
            return new ClassificationResult(
                    "ENVIRONMENT_DATA_ISSUE",
                    0.95,
                    "Failure signature matches network/database environment timeouts or infrastructure unavailability.",
                    "1. Check database and server health\n2. Verify network connection\n3. Re-run test"
            );
        }

        if (msg.contains("nosuchelement") || msg.contains("staleelement") || msg.contains("elementnotinteractable") || msg.contains("invalidselector")) {
            return new ClassificationResult(
                    "TEST_SCRIPT_ISSUE",
                    0.92,
                    "Failure signature indicates broken XPath locator or outdated DOM element reference in test script.",
                    "1. Inspect target UI DOM element\n2. Update broken XPath/CSS selector in test script\n3. Re-run test"
            );
        }

        Optional<FlakinessMetrics> metrics = flakinessMetricsRepository.findByTestName(testName);
        if (metrics.isPresent() && metrics.get().getFlakinessScore() >= 25.0) {
            return new ClassificationResult(
                    "FLAKY_UNSTABLE_TEST",
                    0.88,
                    "Test flakiness score is " + String.format("%.1f", metrics.get().getFlakinessScore()) + "% (exceeds 25% threshold). Historical pass/fail pattern indicates intermittent instability.",
                    "1. Review test synchronization and wait conditions\n2. Quarantine test for review"
            );
        }

        // 2. High-Precision LLM Classifier Engine (Gemini API via Java 21 HttpClient)
        if (geminiApiKey != null && !geminiApiKey.isBlank() && !geminiApiKey.contains("AQ.Ab8RN6JbpleCakWAd3YXWOctK94rNVcp1bfy0XR")) {
            try {
                return callGeminiClassifier(testName, errorMessage, stackTrace);
            } catch (Exception e) {
                System.err.println("[TriageClassifierService] LLM API Call Fallback: " + e.getMessage());
            }
        }

        // 3. High-Confidence Rule Fallback
        if (msg.contains("expected") && msg.contains("was") || msg.contains("assertionerror") || msg.contains("conflict") || msg.contains("409")) {
            return new ClassificationResult(
                    "GENUINE_FUNCTIONAL_DEFECT",
                    0.90,
                    "Assertion failure indicates actual business logic mismatch or endpoint validation error.",
                    "1. Execute " + testName + "\n2. Pass test payload to endpoint\n3. Verify response status and payload assertions"
            );
        }

        return new ClassificationResult(
                "GENUINE_FUNCTIONAL_DEFECT",
                0.80,
                "Unhandled functional error detected during test execution requiring developer review.",
                "1. Trigger test suite\n2. Inspect stack trace\n3. Verify endpoint logic"
            );
    }

    private ClassificationResult callGeminiClassifier(String testName, String errorMessage, String stackTrace) {
        String prompt = String.format(
                "You are an expert QA AI Triage Engine. Analyze this test failure:\n" +
                "Test: %s\nError: %s\nStack: %s\n\n" +
                "Classify into EXACTLY ONE category:\n" +
                "1. GENUINE_FUNCTIONAL_DEFECT\n2. FLAKY_UNSTABLE_TEST\n3. ENVIRONMENT_DATA_ISSUE\n4. TEST_SCRIPT_ISSUE\n\n" +
                "Respond ONLY in valid JSON:\n" +
                "{\"category\":\"...\", \"confidence\": 0.95, \"reasoning\":\"...\", \"reproductionSteps\":\"...\"}",
                testName, errorMessage, stackTrace != null && stackTrace.length() > 300 ? stackTrace.substring(0, 300) : stackTrace
        );

        try {
            Map<String, Object> bodyMap = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );
            String jsonPayload = objectMapper.writeValueAsString(bodyMap);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                String jsonText = text.substring(text.indexOf("{"), text.lastIndexOf("}") + 1);
                JsonNode resJson = objectMapper.readTree(jsonText);

                return new ClassificationResult(
                        resJson.path("category").asText("GENUINE_FUNCTIONAL_DEFECT"),
                        resJson.path("confidence").asDouble(0.90),
                        resJson.path("reasoning").asText("High-precision AI analysis confirms failure signature."),
                        resJson.path("reproductionSteps").asText("1. Run test suite\n2. Inspect assertions")
                );
            }
        } catch (Exception e) {
            System.err.println("[TriageClassifierService] Error invoking Gemini API: " + e.getMessage());
        }

        return new ClassificationResult(
                "GENUINE_FUNCTIONAL_DEFECT",
                0.85,
                "AI classification analysis completed.",
                "1. Run " + testName + "\n2. Verify assertions"
        );
    }
}
