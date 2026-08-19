package com.rc.Bank_Service.triage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.Bank_Service.triage.model.FailureClassification;
import com.rc.Bank_Service.triage.repository.FailureClassificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class JiraDefectDraftService {

    private final FailureClassificationRepository failureClassificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public JiraDefectDraftService(FailureClassificationRepository failureClassificationRepository) {
        this.failureClassificationRepository = failureClassificationRepository;
    }

    public String generateJiraDraftPayload(String testName, String errorMessage, String stackTrace, String reproductionSteps) {
        try {
            Map<String, Object> fields = new HashMap<>();
            fields.put("summary", "[Defect] Automated Test Failed: " + testName);
            fields.put("description", String.format(
                    "h2. Defect Reproduction Steps\n%s\n\nh2. Error Summary\n%s\n\nh2. Stack Trace\n{code}%s{code}",
                    reproductionSteps != null ? reproductionSteps : "1. Run automated test suite\n2. Execute " + testName,
                    errorMessage,
                    stackTrace != null && stackTrace.length() > 500 ? stackTrace.substring(0, 500) + "..." : stackTrace
            ));
            
            Map<String, String> project = new HashMap<>();
            project.put("key", "BANK");
            fields.put("project", project);

            Map<String, String> issuetype = new HashMap<>();
            issuetype.put("name", "Bug");
            fields.put("issuetype", issuetype);

            Map<String, Object> payload = new HashMap<>();
            payload.put("fields", fields);
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
        System.out.println("[JiraDefectDraftService] QA Lead approved Jira Defect for test: " + classification.getTestName());
        
        return failureClassificationRepository.save(classification);
    }
}
