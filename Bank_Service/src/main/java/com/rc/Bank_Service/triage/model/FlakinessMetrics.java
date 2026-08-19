package com.rc.Bank_Service.triage.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flakiness_metrics")
public class FlakinessMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String testName;

    private int totalRuns;
    private int flipCount;
    private double flakinessScore; // (flipCount / (totalRuns - 1)) * 100
    private boolean isQuarantined = false;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public FlakinessMetrics() {}

    public FlakinessMetrics(String testName, int totalRuns, int flipCount, double flakinessScore, boolean isQuarantined) {
        this.testName = testName;
        this.totalRuns = totalRuns;
        this.flipCount = flipCount;
        this.flakinessScore = flakinessScore;
        this.isQuarantined = isQuarantined;
    }

    public Long getId() { return id; }
    public String getTestName() { return testName; }
    public int getTotalRuns() { return totalRuns; }
    public int getFlipCount() { return flipCount; }
    public double getFlakinessScore() { return flakinessScore; }
    public boolean isQuarantined() { return isQuarantined; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTestName(String testName) { this.testName = testName; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }
    public void setFlipCount(int flipCount) { this.flipCount = flipCount; }
    public void setFlakinessScore(double flakinessScore) { this.flakinessScore = flakinessScore; }
    public void setQuarantined(boolean quarantined) { isQuarantined = quarantined; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
