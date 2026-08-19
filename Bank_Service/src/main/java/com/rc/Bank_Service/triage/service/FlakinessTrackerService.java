package com.rc.Bank_Service.triage.service;

import com.rc.Bank_Service.triage.model.FlakinessMetrics;
import com.rc.Bank_Service.triage.model.TestExecutionHistory;
import com.rc.Bank_Service.triage.repository.FlakinessMetricsRepository;
import com.rc.Bank_Service.triage.repository.TestExecutionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlakinessTrackerService {

    private final FlakinessMetricsRepository flakinessMetricsRepository;
    private final TestExecutionHistoryRepository testExecutionHistoryRepository;

    @Value("${app.triage.flakiness-threshold-percent:25.0}")
    private double flakinessThresholdPercent;

    @Autowired
    public FlakinessTrackerService(FlakinessMetricsRepository flakinessMetricsRepository,
                                   TestExecutionHistoryRepository testExecutionHistoryRepository) {
        this.flakinessMetricsRepository = flakinessMetricsRepository;
        this.testExecutionHistoryRepository = testExecutionHistoryRepository;
    }

    @Transactional
    public FlakinessMetrics updateFlakinessScore(String testName) {
        List<TestExecutionHistory> history = testExecutionHistoryRepository
                .findByTestNameOrderByCreatedAtDesc(testName, PageRequest.of(0, 20));

        if (history.size() <= 1) {
            return flakinessMetricsRepository.findByTestName(testName)
                    .orElseGet(() -> flakinessMetricsRepository.save(new FlakinessMetrics(testName, history.size(), 0, 0.0, false)));
        }

        int totalRuns = history.size();
        int flipCount = 0;
        for (int i = 0; i < totalRuns - 1; i++) {
            String currentStatus = history.get(i).getStatus();
            String previousStatus = history.get(i + 1).getStatus();
            if (!currentStatus.equalsIgnoreCase(previousStatus)) {
                flipCount++;
            }
        }

        double flakinessScore = (double) flipCount / (totalRuns - 1) * 100.0;
        boolean isQuarantined = flakinessScore >= flakinessThresholdPercent;

        Optional<FlakinessMetrics> metricsOpt = flakinessMetricsRepository.findByTestName(testName);
        FlakinessMetrics metrics;
        if (metricsOpt.isPresent()) {
            metrics = metricsOpt.get();
            metrics.setTotalRuns(totalRuns);
            metrics.setFlipCount(flipCount);
            metrics.setFlakinessScore(flakinessScore);
            metrics.setQuarantined(isQuarantined);
            metrics.setUpdatedAt(LocalDateTime.now());
        } else {
            metrics = new FlakinessMetrics(testName, totalRuns, flipCount, flakinessScore, isQuarantined);
        }

        return flakinessMetricsRepository.save(metrics);
    }
}
