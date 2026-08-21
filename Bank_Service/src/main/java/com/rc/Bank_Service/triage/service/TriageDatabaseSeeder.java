package com.rc.Bank_Service.triage.service;

import com.rc.Bank_Service.triage.eval.TriageEvaluationHarness;
import com.rc.Bank_Service.triage.repository.FailureClassificationRepository;
import com.rc.Bank_Service.triage.repository.FlakinessMetricsRepository;
import com.rc.Bank_Service.triage.repository.TestExecutionHistoryRepository;
import com.rc.Bank_Service.triage.repository.TestRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TriageDatabaseSeeder implements CommandLineRunner {

    private final TestRunRepository testRunRepository;
    private final TestExecutionHistoryRepository testExecutionHistoryRepository;
    private final FailureClassificationRepository failureClassificationRepository;
    private final FlakinessMetricsRepository flakinessMetricsRepository;
    private final TriageEvaluationHarness evaluationHarness;

    @Autowired
    public TriageDatabaseSeeder(TestRunRepository testRunRepository,
                                TestExecutionHistoryRepository testExecutionHistoryRepository,
                                FailureClassificationRepository failureClassificationRepository,
                                FlakinessMetricsRepository flakinessMetricsRepository,
                                TriageEvaluationHarness evaluationHarness) {
        this.testRunRepository = testRunRepository;
        this.testExecutionHistoryRepository = testExecutionHistoryRepository;
        this.failureClassificationRepository = failureClassificationRepository;
        this.flakinessMetricsRepository = flakinessMetricsRepository;
        this.evaluationHarness = evaluationHarness;
    }

    @Override
    public void run(String... args) {
        if (testRunRepository.count() == 0 || failureClassificationRepository.count() == 0) {
            System.out.println("[TriageDatabaseSeeder] Seeding initial TrustBank QA Triage benchmark dataset into database...");
            evaluationHarness.runEvaluation();
            System.out.println("[TriageDatabaseSeeder] Database seeding complete! Test runs, execution history, flakiness metrics, and classifications populated.");
        }
    }
}
