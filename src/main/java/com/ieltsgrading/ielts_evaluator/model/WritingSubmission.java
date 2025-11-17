package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Writing Submission Entity - Implements ITestSubmission
 */
@Entity
@Table(name = "writing_submission")
public class WritingSubmission implements ITestSubmission {

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
    private IeltsWritingTest test; // Transient field for test details

    // Writing specific fields
    @Column(name = "task1_answer", columnDefinition = "TEXT")
    private String task1Answer;

    @Column(name = "task2_answer", columnDefinition = "TEXT")
    private String task2Answer;

    @Column(name = "task1_word_count")
    private Integer task1WordCount;

    @Column(name = "task2_word_count")
    private Integer task2WordCount;

    @Column(name = "task1_result", columnDefinition = "TEXT")
    private String task1Result;

    @Column(name = "task2_result", columnDefinition = "TEXT")
    private String task2Result;

    @Column(name = "task1_score")
    private Double task1Score;

    @Column(name = "task2_score")
    private Double task2Score;

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
        return "writing";
    }

    @Override
    public Integer getTestId() {
        return testId;
    }

    @Override
    public String getTestDisplayName() {
        if (test != null) {
            return "CAM " + test.getCamNumber() + " - Writing Test " + test.getTestNumber();
        }
        return "Writing Test";
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

    // Writing-specific overrides
    @Override
    public String getTask1Answer() {
        return task1Answer;
    }

    @Override
    public String getTask2Answer() {
        return task2Answer;
    }

    @Override
    public Integer getTask1WordCount() {
        return task1WordCount;
    }

    @Override
    public Integer getTask2WordCount() {
        return task2WordCount;
    }

    @Override
    public Double getTask1Score() {
        return task1Score;
    }

    @Override
    public Double getTask2Score() {
        return task2Score;
    }

    @Override
    public String getTask1Feedback() {
        if (task1Result == null || task1Result.isEmpty()) {
            return "<p>No feedback available yet.</p>";
        }
        try {
            Map<String, Object> result = parseJson(task1Result);
            Object message = result.get("message");
            if (message != null) {
                return message.toString();
            }
        } catch (Exception e) {
            System.err.println("Error parsing Task 1 feedback: " + e.getMessage());
        }
        return "<p>Feedback loading...</p>";
    }

    @Override
    public String getTask2Feedback() {
        if (task2Result == null || task2Result.isEmpty()) {
            return "<p>No feedback available yet.</p>";
        }
        try {
            Map<String, Object> result = parseJson(task2Result);
            Object message = result.get("message");
            if (message != null) {
                return message.toString();
            }
        } catch (Exception e) {
            System.err.println("Error parsing Task 2 feedback: " + e.getMessage());
        }
        return "<p>Feedback loading...</p>";
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

    public Map<String, Object> getTask1ResultMap() {
        return parseJson(task1Result);
    }

    public Map<String, Object> getTask2ResultMap() {
        return parseJson(task2Result);
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

    public IeltsWritingTest getTest() {
        return test;
    }

    public void setTest(IeltsWritingTest test) {
        this.test = test;
    }

    public void setTask1Answer(String task1Answer) {
        this.task1Answer = task1Answer;
    }

    public void setTask2Answer(String task2Answer) {
        this.task2Answer = task2Answer;
    }

    public void setTask1WordCount(Integer task1WordCount) {
        this.task1WordCount = task1WordCount;
    }

    public void setTask2WordCount(Integer task2WordCount) {
        this.task2WordCount = task2WordCount;
    }

    public String getTask1Result() {
        return task1Result;
    }

    public void setTask1Result(String task1Result) {
        this.task1Result = task1Result;
    }

    public String getTask2Result() {
        return task2Result;
    }

    public void setTask2Result(String task2Result) {
        this.task2Result = task2Result;
    }

    public void setTask1Score(Double task1Score) {
        this.task1Score = task1Score;
    }

    public void setTask2Score(Double task2Score) {
        this.task2Score = task2Score;
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
        return "WritingSubmission{" +
                "id=" + submissionId +
                ", uuid='" + submissionUuid + '\'' +
                ", testId=" + testId +
                ", status='" + status + '\'' +
                ", score=" + overallScore +
                '}';
    }
}