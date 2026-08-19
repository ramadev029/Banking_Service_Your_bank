package com.rc.Bank_Service.triage.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_execution_history")
public class TestExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String testName;

    private String className;

    @Column(nullable = false)
    private String status; // PASS or FAIL

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    private long durationMs;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TestExecutionHistory() {}

    public TestExecutionHistory(String testName, String className, String status, String errorMessage, String stackTrace, long durationMs) {
        this.testName = testName;
        this.className = className;
        this.status = status;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
        this.durationMs = durationMs;
    }

    public Long getId() { return id; }
    public String getTestName() { return testName; }
    public String getClassName() { return className; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public String getStackTrace() { return stackTrace; }
    public long getDurationMs() { return durationMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTestName(String testName) { this.testName = testName; }
    public void setClassName(String className) { this.className = className; }
    public void setStatus(String status) { this.status = status; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
