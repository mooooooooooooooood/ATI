package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reading Submission Entity - Implements ITestSubmission
 */
@Entity
@Table(name = "reading_submission")
public class ReadingSubmission implements ITestSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "submission_uuid", nullable = false, unique = true)
    private String submissionUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "test_id", nullable = false)
    private Integer testId;

    @Transient
    private IeltsReadingTest test;

    // Reading specific fields
    @Column(name = "objective_answer", columnDefinition = "JSON")
    private String objectiveAnswer;

    @Column(name = "objective_result", columnDefinition = "JSON")
    private String objectiveResult;

    @Column(name = "objective_score")
    private Double objectiveScore;

    // Shared fields
    @Column(name = "time_spent")
    private Integer timeSpent;

    @Column(name = "status", length = 20)
    private String status = "pending";

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /* ==================== ITestSubmission Implementation ==================== */

    @Override
    public String getSubmissionUuid() {
        return submissionUuid;
    }

    @Override
    public Long getSubmissionId() {
        return submissionId;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @Override
    public String getTestType() {
        return "reading";
    }

    @Override
    public Integer getTestId() {
        return testId;
    }

    @Override
    public String getTestDisplayName() {
        if (test != null) {
            return "CAM " + test.getCamNumber() + " - Reading Test " + test.getTestNumber();
        }
        return "Reading Test";
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getStatusDisplay() {
        if (status == null) return "Unknown";
        switch (status.toLowerCase()) {
            case "completed": return "Completed";
            case "processing": return "Processing";
            case "pending": return "Pending";
            case "failed": return "Failed";
            default: return status.substring(0, 1).toUpperCase() + status.substring(1);
        }
    }

    @Override
    public String getStatusColor() {
        if (status == null) return "#999";
        switch (status.toLowerCase()) {
            case "completed": return "#4caf50";
            case "processing": return "#2196f3";
            case "pending": return "#ff9800";
            case "failed": return "#f44336";
            default: return "#999";
        }
    }

    @Override
    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }

    @Override
    public boolean isProcessing() {
        return "processing".equalsIgnoreCase(status);
    }

    @Override
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    @Override
    public boolean isFailed() {
        return "failed".equalsIgnoreCase(status);
    }

    @Override
    public Double getOverallScore() {
        return overallScore;
    }

    @Override
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    @Override
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    @Override
    public Integer getTimeSpent() {
        return timeSpent;
    }

    @Override
    public String getFormattedTimeSpent() {
        if (timeSpent == null || timeSpent == 0) {
            return "N/A";
        }
        int minutes = timeSpent / 60;
        int seconds = timeSpent % 60;
        if (minutes > 0) {
            return minutes + " min" + (seconds > 0 ? " " + seconds + " sec" : "");
        }
        return seconds + " sec";
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /* ==================== Helper Methods ==================== */

    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            System.err.println("JSON parse error: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public Map<String, Object> getObjectiveResultMap() {
        return parseJson(objectiveResult);
    }

    public Map<String, Object> getObjectiveAnswerMap() {
        return parseJson(objectiveAnswer);
    }

    /* ==================== Getters & Setters ==================== */

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public void setSubmissionUuid(String submissionUuid) {
        this.submissionUuid = submissionUuid;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public IeltsReadingTest getTest() {
        return test;
    }

    public void setTest(IeltsReadingTest test) {
        this.test = test;
    }

    public String getObjectiveAnswer() {
        return objectiveAnswer;
    }

    public void setObjectiveAnswer(String objectiveAnswer) {
        this.objectiveAnswer = objectiveAnswer;
    }

    public String getObjectiveResult() {
        return objectiveResult;
    }

    public void setObjectiveResult(String objectiveResult) {
        this.objectiveResult = objectiveResult;
    }

    public Double getObjectiveScore() {
        return objectiveScore;
    }

    public void setObjectiveScore(Double objectiveScore) {
        this.objectiveScore = objectiveScore;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ReadingSubmission{" +
                "id=" + submissionId +
                ", uuid='" + submissionUuid + '\'' +
                ", testId=" + testId +
                ", status='" + status + '\'' +
                ", score=" + overallScore +
                '}';
    }
}