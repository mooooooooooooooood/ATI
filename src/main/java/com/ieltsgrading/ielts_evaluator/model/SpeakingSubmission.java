package com.ieltsgrading.ielts_evaluator.model;

import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTest;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Speaking Submission Entity - Implements ITestSubmission
 * FIXED: Now uses SpeakingTest (not IeltsSpeakingTest)
 */
@Entity
@Table(name = "speaking_submission")
public class SpeakingSubmission implements ITestSubmission {

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

    // ✅ FIXED: Changed from IeltsSpeakingTest to SpeakingTest
    @Transient
    private SpeakingTest test;

    // Speaking specific fields
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "speaking_result", columnDefinition = "JSON")
    private String speakingResult;

    @Column(name = "speaking_score")
    private Double speakingScore;

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
        return "speaking";
    }

    @Override
    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    @Override
    public String getTestDisplayName() {
        if (test != null) {
            // ✅ FIXED: Use SpeakingTest methods
            return "Speaking Test - " + test.getTestDate() + " (" + test.getMainTopic() + ")";
        }
        return "Speaking Test #" + testId;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getStatusDisplay() {
        if (status == null)
            return "Unknown";
        switch (status.toLowerCase()) {
            case "completed":
                return "Completed";
            case "processing":
                return "Processing";
            case "pending":
                return "Pending";
            case "failed":
                return "Failed";
            default:
                return status.substring(0, 1).toUpperCase() + status.substring(1);
        }
    }

    @Override
    public String getStatusColor() {
        if (status == null)
            return "#999";
        switch (status.toLowerCase()) {
            case "completed":
                return "#4caf50";
            case "processing":
                return "#2196f3";
            case "pending":
                return "#ff9800";
            case "failed":
                return "#f44336";
            default:
                return "#999";
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
        if (json == null || json.isEmpty())
            return new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            System.err.println("JSON parse error: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public Map<String, Object> getSpeakingResultMap() {
        return parseJson(speakingResult);
    }

    /**
     * Get speaking scores breakdown
     */
    public Map<String, Double> getSpeakingScores() {
        Map<String, Object> result = getSpeakingResultMap();
        Map<String, Double> scores = new HashMap<>();

        // Extract individual scores
        if (result.containsKey("fluency")) {
            scores.put("fluency", getDoubleValue(result.get("fluency")));
        }
        if (result.containsKey("lexicalResource")) {
            scores.put("lexicalResource", getDoubleValue(result.get("lexicalResource")));
        }
        if (result.containsKey("grammaticalRange")) {
            scores.put("grammaticalRange", getDoubleValue(result.get("grammaticalRange")));
        }
        if (result.containsKey("pronunciation")) {
            scores.put("pronunciation", getDoubleValue(result.get("pronunciation")));
        }

        return scores;
    }

    private Double getDoubleValue(Object value) {
        if (value == null)
            return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
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

    // ✅ FIXED: Changed from IeltsSpeakingTest to SpeakingTest
    public SpeakingTest getTest() {
        return test;
    }

    public void setTest(SpeakingTest test) {
        this.test = test;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getSpeakingResult() {
        return speakingResult;
    }

    public void setSpeakingResult(String speakingResult) {
        this.speakingResult = speakingResult;
    }

    public Double getSpeakingScore() {
        return speakingScore;
    }

    public void setSpeakingScore(Double speakingScore) {
        this.speakingScore = speakingScore;
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
        return "SpeakingSubmission{" +
                "id=" + submissionId +
                ", uuid='" + submissionUuid + '\'' +
                ", testId=" + testId +
                ", status='" + status + '\'' +
                ", score=" + overallScore +
                '}';
    }

    public void setUserId(Long id) {
        throw new UnsupportedOperationException("Use setUser() instead");
    }
}