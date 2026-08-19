package com.rc.Bank_Service.controller;

import com.rc.Bank_Service.triage.eval.TriageEvaluationHarness;
import com.rc.Bank_Service.triage.model.FailureClassification;
import com.rc.Bank_Service.triage.model.FlakinessMetrics;
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

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeReport(@RequestBody Map<String, String> payload) {
        String xmlContent = payload.get("xmlContent");
        String jsonContent = payload.get("jsonContent");
        String suiteName = payload.getOrDefault("suiteName", "Automated Banking Test Suite");

        List<TriageReportParser.ParsedFailureRecord> failures;
        if (xmlContent != null && !xmlContent.isBlank()) {
            failures = reportParser.parseJunitXml(xmlContent);
        } else if (jsonContent != null && !jsonContent.isBlank()) {
            failures = reportParser.parseNewmanJson(jsonContent);
        } else {
            failures = List.of();
        }

        TestRun testRun = new TestRun(suiteName, 5, 5 - failures.size(), failures.size(), 4500);
        testRunRepository.save(testRun);

        List<FailureClassification> classifiedResults = failures.stream().map(f -> {
            flakinessTrackerService.updateFlakinessScore(f.getTestName());
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

        double suiteHealthScore = 100.0 - (quarantinedTests.size() * 5.0);
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
}
