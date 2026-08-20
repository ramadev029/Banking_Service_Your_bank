package com.rc.Bank_Service.controller;

import com.rc.Bank_Service.triage.eval.TriageEvaluationHarness;
import com.rc.Bank_Service.triage.model.FailureClassification;
import com.rc.Bank_Service.triage.model.FlakinessMetrics;
import com.rc.Bank_Service.triage.model.TestExecutionHistory;
import com.rc.Bank_Service.triage.model.TestRun;
import com.rc.Bank_Service.triage.parser.TriageReportParser;
import com.rc.Bank_Service.triage.repository.FailureClassificationRepository;
import com.rc.Bank_Service.triage.repository.FlakinessMetricsRepository;
import com.rc.Bank_Service.triage.repository.TestExecutionHistoryRepository;
import com.rc.Bank_Service.triage.repository.TestRunRepository;
import com.rc.Bank_Service.triage.service.FlakinessTrackerService;
import com.rc.Bank_Service.triage.service.JiraDefectDraftService;
import com.rc.Bank_Service.triage.service.TriageClassifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/triage")
@CrossOrigin(origins = "*")
public class TriageController {

    private final TriageReportParser reportParser;
    private final TriageClassifierService classifierService;
    private final FlakinessTrackerService flakinessTrackerService;
    private final JiraDefectDraftService jiraDefectDraftService;
    private final TriageEvaluationHarness evaluationHarness;

    private final TestRunRepository testRunRepository;
    private final TestExecutionHistoryRepository testExecutionHistoryRepository;
    private final FailureClassificationRepository failureClassificationRepository;
    private final FlakinessMetricsRepository flakinessMetricsRepository;

    @Autowired
    public TriageController(TriageReportParser reportParser,
                            TriageClassifierService classifierService,
                            FlakinessTrackerService flakinessTrackerService,
                            JiraDefectDraftService jiraDefectDraftService,
                            TriageEvaluationHarness evaluationHarness,
                            TestRunRepository testRunRepository,
                            TestExecutionHistoryRepository testExecutionHistoryRepository,
                            FailureClassificationRepository failureClassificationRepository,
                            FlakinessMetricsRepository flakinessMetricsRepository) {
        this.reportParser = reportParser;
        this.classifierService = classifierService;
        this.flakinessTrackerService = flakinessTrackerService;
        this.jiraDefectDraftService = jiraDefectDraftService;
        this.evaluationHarness = evaluationHarness;
        this.testRunRepository = testRunRepository;
        this.testExecutionHistoryRepository = testExecutionHistoryRepository;
        this.failureClassificationRepository = failureClassificationRepository;
        this.flakinessMetricsRepository = flakinessMetricsRepository;
    }

    private String extractStringContent(Object raw) {
        if (raw == null) return null;
        if (raw instanceof List<?> list) {
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                sb.append(item != null ? item.toString() : "").append("\n");
            }
            return sb.toString();
        }
        return raw.toString();
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeReport(@RequestBody Map<String, Object> payload) {
        String xmlContent = extractStringContent(payload.get("xmlContent"));
        String jsonContent = extractStringContent(payload.get("jsonContent"));
        String suiteName = extractStringContent(payload.get("suiteName"));
        if (suiteName == null || suiteName.isBlank()) suiteName = "Automated Banking Test Suite";

        List<TriageReportParser.ParsedFailureRecord> failures;
        if (xmlContent != null && !xmlContent.isBlank()) {
            failures = reportParser.parseJunitXml(xmlContent);
        } else if (jsonContent != null && !jsonContent.isBlank()) {
            failures = reportParser.parseNewmanJson(jsonContent);
        } else {
            failures = List.of();
        }

        int totalTests = Math.max(failures.size(), 5);
        int failedCount = failures.size();
        int passedCount = totalTests - failedCount;

        TestRun testRun = new TestRun(suiteName, totalTests, passedCount, failedCount, 4500);
        testRunRepository.save(testRun);

        List<FailureClassification> classifiedResults = failures.stream().map(f -> {
            // Save execution history record for database Module 2
            TestExecutionHistory history = new TestExecutionHistory(
                f.getTestName(),
                f.getClassName(),
                "FAIL",
                f.getErrorMessage(),
                f.getStackTrace(),
                f.getDurationMs()
            );
            testExecutionHistoryRepository.save(history);

            // Update flakiness metrics
            flakinessTrackerService.updateFlakinessScore(f.getTestName());

            // Run AI Classification & save
            return classifierService.classifyAndSave(f.getTestName(), f.getErrorMessage(), f.getStackTrace());
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("runId", testRun.getId());
        response.put("totalFailures", failures.size());
        response.put("classifications", classifiedResults);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        List<FailureClassification> recentClassifications = failureClassificationRepository.findTop20ByOrderByCreatedAtDesc();
        List<FailureClassification> pendingApprovalDrafts = failureClassificationRepository.findByIsHumanApprovedFalseOrderByCreatedAtDesc();
        List<FlakinessMetrics> quarantinedTests = flakinessMetricsRepository.findByIsQuarantinedTrue();
        List<FlakinessMetrics> topFlakyTests = flakinessMetricsRepository.findTop10ByOrderByFlakinessScoreDesc();

        Map<String, Integer> categoryCounts = new HashMap<>();
        categoryCounts.put("GENUINE_FUNCTIONAL_DEFECT", 0);
        categoryCounts.put("FLAKY_UNSTABLE_TEST", 0);
        categoryCounts.put("ENVIRONMENT_DATA_ISSUE", 0);
        categoryCounts.put("TEST_SCRIPT_ISSUE", 0);

        for (FailureClassification fc : recentClassifications) {
            String cat = fc.getCategory();
            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
        }

        double suiteHealthScore = 100.0 - (quarantinedTests.size() * 5.0) - (pendingApprovalDrafts.size() * 10.0);
        if (suiteHealthScore < 0) suiteHealthScore = 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("suiteHealthScore", suiteHealthScore);
        summary.put("categoryCounts", categoryCounts);
        summary.put("recentClassifications", recentClassifications);
        summary.put("pendingApprovalDrafts", pendingApprovalDrafts);
        summary.put("quarantinedTests", quarantinedTests);
        summary.put("topFlakyTests", topFlakyTests);

        return ResponseEntity.ok(summary);
    }

    @PostMapping("/approve-jira/{id}")
    public ResponseEntity<FailureClassification> approveJiraDraft(@PathVariable Long id) {
        FailureClassification approved = jiraDefectDraftService.approveAndSubmitToJira(id);
        return ResponseEntity.ok(approved);
    }

    @GetMapping("/evaluation-matrix")
    public ResponseEntity<TriageEvaluationHarness.EvaluationResult> getEvaluationMatrix() {
        TriageEvaluationHarness.EvaluationResult result = evaluationHarness.runEvaluation();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/clear-test-data")
    public ResponseEntity<Map<String, String>> clearTestData() {
        failureClassificationRepository.deleteAll();
        flakinessMetricsRepository.deleteAll();
        testExecutionHistoryRepository.deleteAll();
        testRunRepository.deleteAll();
        Map<String, String> res = new HashMap<>();
        res.put("message", "All test run history and classifications cleared successfully for fresh testing.");
        return ResponseEntity.ok(res);
    }
}
