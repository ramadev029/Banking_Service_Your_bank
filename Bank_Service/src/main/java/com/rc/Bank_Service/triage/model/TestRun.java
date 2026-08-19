package com.rc.Bank_Service.triage.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_runs")
public class TestRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String suiteName;

    private int totalTests;
    private int passedCount;
    private int failedCount;
    private long durationMs;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TestRun() {}

    public TestRun(String suiteName, int totalTests, int passedCount, int failedCount, long durationMs) {
        this.suiteName = suiteName;
        this.totalTests = totalTests;
        this.passedCount = passedCount;
        this.failedCount = failedCount;
        this.durationMs = durationMs;
    }

    public Long getId() { return id; }
    public String getSuiteName() { return suiteName; }
    public int getTotalTests() { return totalTests; }
    public int getPassedCount() { return passedCount; }
    public int getFailedCount() { return failedCount; }
    public long getDurationMs() { return durationMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setSuiteName(String suiteName) { this.suiteName = suiteName; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    public void setPassedCount(int passedCount) { this.passedCount = passedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
