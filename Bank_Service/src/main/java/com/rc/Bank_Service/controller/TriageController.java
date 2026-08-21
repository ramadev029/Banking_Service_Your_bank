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

    private static Map<String, Object> latestJenkinsIngestion = null;

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

        TriageReportParser.ParsedReport report = reportParser.parseFullJunitXml(xmlContent, suiteName);

        int totalTests = report.getTotalTests() > 0 ? report.getTotalTests() : 5;
        int failedCount = report.getFailedCount();
        int passedCount = report.getPassedCount();
        long durationMs = report.getDurationMs() > 0 ? report.getDurationMs() : 4500;

        TestRun testRun = new TestRun(report.getSuiteName(), totalTests, passedCount, failedCount, durationMs);
        testRunRepository.save(testRun);

        List<FailureClassification> classifiedResults = new java.util.ArrayList<>();

        for (TriageReportParser.ParsedRecord record : report.getRecords()) {
            // Save execution history record for database Module 2
            TestExecutionHistory history = new TestExecutionHistory(
                record.getTestName(),
                record.getClassName(),
                record.getStatus(),
                record.getErrorMessage(),
                record.getStackTrace(),
                record.getDurationMs()
            );
            testExecutionHistoryRepository.save(history);

            // Update flakiness metrics (Module 4)
            flakinessTrackerService.updateFlakinessScore(record.getTestName());

            // Run AI Classification & save for failed tests (Module 3)
            if ("FAIL".equalsIgnoreCase(record.getStatus())) {
                FailureClassification fc = classifierService.classifyAndSave(record.getTestName(), record.getErrorMessage(), record.getStackTrace());
                classifiedResults.add(fc);
            }
        }

        // Fallback if legacy Newman or failure-only payload was received
        if (report.getRecords().isEmpty()) {
            List<TriageReportParser.ParsedFailureRecord> failures;
            if (xmlContent != null && !xmlContent.isBlank()) {
                failures = reportParser.parseJunitXml(xmlContent);
            } else if (jsonContent != null && !jsonContent.isBlank()) {
                failures = reportParser.parseNewmanJson(jsonContent);
            } else {
                failures = List.of();
            }

            for (TriageReportParser.ParsedFailureRecord f : failures) {
                TestExecutionHistory history = new TestExecutionHistory(
                    f.getTestName(), f.getClassName(), "FAIL", f.getErrorMessage(), f.getStackTrace(), f.getDurationMs()
                );
                testExecutionHistoryRepository.save(history);
                flakinessTrackerService.updateFlakinessScore(f.getTestName());
                classifiedResults.add(classifierService.classifyAndSave(f.getTestName(), f.getErrorMessage(), f.getStackTrace()));
            }
        }

        // Set Live Jenkins Ingestion Notification Payload
        Map<String, Object> notification = new HashMap<>();
        notification.put("timestamp", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a")));
        notification.put("suiteName", testRun.getSuiteName());
        notification.put("totalTests", totalTests);
        notification.put("failedCount", classifiedResults.size());
        notification.put("passedCount", passedCount);
        notification.put("acknowledged", false);
        latestJenkinsIngestion = notification;

        Map<String, Object> response = new HashMap<>();
        response.put("runId", testRun.getId());
        response.put("totalFailures", classifiedResults.size());
        response.put("classifications", classifiedResults);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        List<FailureClassification> liveClassifications = failureClassificationRepository.findByIsBenchmarkFalse();
        List<FailureClassification> recentClassifications = failureClassificationRepository.findTop20ByIsBenchmarkFalseOrderByCreatedAtDesc();
        List<FailureClassification> pendingApprovalDrafts = failureClassificationRepository.findByCategoryAndIsHumanApprovedFalseAndIsBenchmarkFalseOrderByCreatedAtDesc("GENUINE_FUNCTIONAL_DEFECT");
        List<FlakinessMetrics> quarantinedTests = flakinessMetricsRepository.findByIsQuarantinedTrue();
        List<FlakinessMetrics> topFlakyTests = flakinessMetricsRepository.findTop10ByOrderByFlakinessScoreDesc();

        Map<String, Integer> categoryCounts = new HashMap<>();
        categoryCounts.put("GENUINE_FUNCTIONAL_DEFECT", 0);
        categoryCounts.put("FLAKY_UNSTABLE_TEST", 0);
        categoryCounts.put("ENVIRONMENT_DATA_ISSUE", 0);
        categoryCounts.put("TEST_SCRIPT_ISSUE", 0);

        for (FailureClassification fc : liveClassifications) {
            String cat = fc.getCategory();
            if (cat != null) {
                categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
            }
        }

        double suiteHealthScore = Math.max(0.0, 100.0 - (quarantinedTests.size() * 2.5) - (pendingApprovalDrafts.size() * 2.0));

        Map<String, Object> summary = new HashMap<>();
        summary.put("suiteHealthScore", suiteHealthScore);
        summary.put("categoryCounts", categoryCounts);
        summary.put("recentClassifications", recentClassifications);
        summary.put("pendingApprovalDrafts", pendingApprovalDrafts);
        summary.put("quarantinedTests", quarantinedTests);
        summary.put("topFlakyTests", topFlakyTests);
        summary.put("latestJenkinsIngestion", latestJenkinsIngestion);

        return ResponseEntity.ok(summary);
    }

    @PostMapping("/acknowledge-jenkins-ingestion")
    public ResponseEntity<Map<String, String>> acknowledgeJenkinsIngestion() {
        if (latestJenkinsIngestion != null) {
            latestJenkinsIngestion.put("acknowledged", true);
        }
        return ResponseEntity.ok(Map.of("message", "Jenkins ingestion acknowledged successfully"));
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
