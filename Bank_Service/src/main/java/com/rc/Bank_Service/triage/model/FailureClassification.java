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

    @Column(columnDefinition = "TEXT")
    private String rootCause;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    private boolean jiraRequired = false;

    @Column(columnDefinition = "TEXT")
    private String evidenceJson;

    @Column(columnDefinition = "TEXT")
    private String contradictingEvidenceJson;

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

    public FailureClassification(String testName, String category, double confidenceScore, String writtenReasoning, String reproductionSteps, String jiraDraftPayload, boolean isBenchmark, String suiteName, String rootCause, String recommendedAction, boolean jiraRequired, String evidenceJson, String contradictingEvidenceJson) {
        this.testName = testName;
        this.category = category;
        this.confidenceScore = confidenceScore;
        this.writtenReasoning = writtenReasoning;
        this.reproductionSteps = reproductionSteps;
        this.jiraDraftPayload = jiraDraftPayload;
        this.isBenchmark = isBenchmark;
        this.suiteName = suiteName;
        this.rootCause = rootCause;
        this.recommendedAction = recommendedAction;
        this.jiraRequired = jiraRequired;
        this.evidenceJson = evidenceJson;
        this.contradictingEvidenceJson = contradictingEvidenceJson;
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
    public String getRootCause() { return rootCause; }
    public String getRecommendedAction() { return recommendedAction; }
    public boolean isJiraRequired() { return jiraRequired; }
    public String getEvidenceJson() { return evidenceJson; }
    public String getContradictingEvidenceJson() { return contradictingEvidenceJson; }
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
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    public void setJiraRequired(boolean jiraRequired) { this.jiraRequired = jiraRequired; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }
    public void setContradictingEvidenceJson(String contradictingEvidenceJson) { this.contradictingEvidenceJson = contradictingEvidenceJson; }
}
