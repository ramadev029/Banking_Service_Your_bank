package com.rc.Bank_Service.triage.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "failure_classifications")
public class FailureClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String testName;

    @Column(nullable = false)
    private String category; // GENUINE_FUNCTIONAL_DEFECT, FLAKY_UNSTABLE_TEST, ENVIRONMENT_DATA_ISSUE, TEST_SCRIPT_ISSUE

    private double confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String writtenReasoning;

    @Column(columnDefinition = "TEXT")
    private String reproductionSteps;

    @Column(columnDefinition = "TEXT")
    private String jiraDraftPayload;

    private boolean isHumanApproved = false;

    private boolean isBenchmark = false;

    private String suiteName;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public FailureClassification() {}

    public FailureClassification(String testName, String category, double confidenceScore, String writtenReasoning, String reproductionSteps, String jiraDraftPayload) {
        this.testName = testName;
        this.category = category;
        this.confidenceScore = confidenceScore;
        this.writtenReasoning = writtenReasoning;
        this.reproductionSteps = reproductionSteps;
        this.jiraDraftPayload = jiraDraftPayload;
    }

    public FailureClassification(String testName, String category, double confidenceScore, String writtenReasoning, String reproductionSteps, String jiraDraftPayload, boolean isBenchmark, String suiteName) {
        this.testName = testName;
        this.category = category;
        this.confidenceScore = confidenceScore;
        this.writtenReasoning = writtenReasoning;
        this.reproductionSteps = reproductionSteps;
        this.jiraDraftPayload = jiraDraftPayload;
        this.isBenchmark = isBenchmark;
        this.suiteName = suiteName;
    }

    public Long getId() { return id; }
    public String getTestName() { return testName; }
    public String getCategory() { return category; }
    public double getConfidenceScore() { return confidenceScore; }
    public String getWrittenReasoning() { return writtenReasoning; }
    public String getReproductionSteps() { return reproductionSteps; }
    public String getJiraDraftPayload() { return jiraDraftPayload; }
    public boolean isHumanApproved() { return isHumanApproved; }
    public boolean isBenchmark() { return isBenchmark; }
    public String getSuiteName() { return suiteName; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTestName(String testName) { this.testName = testName; }
    public void setCategory(String category) { this.category = category; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public void setWrittenReasoning(String writtenReasoning) { this.writtenReasoning = writtenReasoning; }
    public void setReproductionSteps(String reproductionSteps) { this.reproductionSteps = reproductionSteps; }
    public void setJiraDraftPayload(String jiraDraftPayload) { this.jiraDraftPayload = jiraDraftPayload; }
    public void setHumanApproved(boolean humanApproved) { isHumanApproved = humanApproved; }
    public void setBenchmark(boolean benchmark) { isBenchmark = benchmark; }
    public void setSuiteName(String suiteName) { this.suiteName = suiteName; }
}
