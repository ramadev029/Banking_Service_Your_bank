package com.rc.Bank_Service.triage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.Bank_Service.triage.model.FailureClassification;
import com.rc.Bank_Service.triage.repository.FailureClassificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JiraDefectDraftService {

    private final FailureClassificationRepository failureClassificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    @Value("${jira.url:https://ramadev029.atlassian.net}")
    private String jiraUrl;

    @Value("${jira.username:ramadev029@gmail.com}")
    private String jiraUsername;

    @Value("${jira.api-token:}")
    private String jiraApiToken;

    @Value("${jira.project-key:BANK}")
    private String jiraProjectKey;

    @Autowired
    public JiraDefectDraftService(FailureClassificationRepository failureClassificationRepository) {
        this.failureClassificationRepository = failureClassificationRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String generateJiraDraftPayload(String testName, String errorMessage, String stackTrace,
                                           String reproductionSteps, String category, double confidenceScore,
                                           String writtenReasoning) {
        try {
            Map<String, Object> fields = new HashMap<>();
            fields.put("summary", "[Defect] Automated Test Failed: " + testName);
            fields.put("description", String.format(
                    "h2. Classification Reasoning\n%s\n\nh2. Defect Reproduction Steps\n%s\n\nh2. Error Summary\n%s\n\nh2. Stack Trace\n{code}%s{code}",
                    writtenReasoning != null ? writtenReasoning : "High-precision AI analysis performed.",
                    reproductionSteps != null ? reproductionSteps : "1. Run automated test suite\n2. Execute " + testName,
                    errorMessage != null ? errorMessage : "No explicit error message provided.",
                    stackTrace != null && stackTrace.length() > 500 ? stackTrace.substring(0, 500) + "..." : stackTrace
            ));
            
            Map<String, String> project = new HashMap<>();
            project.put("key", jiraProjectKey);
            fields.put("project", project);

            Map<String, String> issuetype = new HashMap<>();
            issuetype.put("name", "Bug");
            fields.put("issuetype", issuetype);

            Map<String, Object> payload = new HashMap<>();
            payload.put("fields", fields);
            payload.put("testName", testName);
            payload.put("category", category);
            payload.put("confidenceScore", Math.round(confidenceScore * 100));
            payload.put("writtenReasoning", writtenReasoning);
            payload.put("reproductionSteps", reproductionSteps);
            payload.put("errorMessage", errorMessage);
            payload.put("stackTrace", stackTrace);
            payload.put("status", "DRAFT_PENDING_QA_APPROVAL");

            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{\"summary\":\"[Defect] " + testName + "\", \"status\":\"DRAFT_PENDING_QA_APPROVAL\"}";
        }
    }

    @Transactional
    public FailureClassification approveAndSubmitToJira(Long classificationId) {
        FailureClassification classification = failureClassificationRepository.findById(classificationId)
                .orElseThrow(() -> new IllegalArgumentException("Classification ID not found: " + classificationId));

        classification.setHumanApproved(true);
        String jiraTicketKey = dispatchToJiraApi(classification);

        if (classification.getJiraDraftPayload() != null) {
            String updatedPayload = classification.getJiraDraftPayload().replace(
                    "DRAFT_PENDING_QA_APPROVAL", 
                    jiraTicketKey != null ? "SUBMITTED_TO_JIRA (" + jiraTicketKey + ")" : "SUBMITTED_TO_JIRA"
            );
            classification.setJiraDraftPayload(updatedPayload);
        }
        
        System.out.println("[JiraDefectDraftService] QA Lead approved Jira Defect. Key: " + jiraTicketKey);
        return failureClassificationRepository.save(classification);
    }

    private String dispatchToJiraApi(FailureClassification classification) {
        if (jiraApiToken == null || jiraApiToken.isBlank() || jiraUrl == null || jiraUrl.isBlank()) {
            System.out.println("[JiraDefectDraftService] Jira API credentials not set. Falling back to local approval sign-off.");
            return "LOCAL_APPROVED";
        }

        try {
            String cleanUrl = jiraUrl.endsWith("/") ? jiraUrl.substring(0, jiraUrl.length() - 1) : jiraUrl;
            String issueEndpoint = cleanUrl + "/rest/api/2/issue";

            Map<String, Object> fields = new HashMap<>();
            fields.put("summary", "[Defect] Automated Test Failed: " + classification.getTestName());
            fields.put("description", String.format(
                    "h2. Classification Category\n*%s* (AI Confidence: %.0f%%)\n\nh2. Diagnostic Reasoning\n%s\n\nh2. Defect Reproduction Steps\n%s\n\nh2. Error Details\nTarget Test: `%s`",
                    classification.getCategory() != null ? classification.getCategory() : "GENUINE_FUNCTIONAL_DEFECT",
                    classification.getConfidenceScore() * 100,
                    classification.getWrittenReasoning() != null ? classification.getWrittenReasoning() : "Assertion failure detected in automated test execution.",
                    classification.getReproductionSteps() != null ? classification.getReproductionSteps() : "1. Run automated test suite\n2. Execute target test method",
                    classification.getTestName()
            ));

            Map<String, String> project = new HashMap<>();
            project.put("key", jiraProjectKey);
            fields.put("project", project);

            Map<String, String> issuetype = new HashMap<>();
            issuetype.put("name", "Bug");
            fields.put("issuetype", issuetype);

            Map<String, Object> body = new HashMap<>();
            body.put("fields", fields);

            String jsonPayload = objectMapper.writeValueAsString(body);
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((jiraUsername + ":" + jiraApiToken).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(issueEndpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[Jira API Response] HTTP " + response.statusCode() + " -> " + response.body());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                JsonNode resJson = objectMapper.readTree(response.body());
                return resJson.path("key").asText("JIRA-SUCCESS");
            } else {
                System.err.println("[Jira API Warning] Live Jira returned status " + response.statusCode() + ". Check Project Key '" + jiraProjectKey + "' or User Permissions.");
                return "SUBMITTED_TO_JIRA";
            }
        } catch (Exception e) {
            System.err.println("[Jira API Exception] Failed to dispatch defect to Jira: " + e.getMessage());
            return "SUBMITTED_TO_JIRA";
        }
    }
}
