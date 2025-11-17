package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class for Test Submission
 * Stores user's test submissions and results
 */
@Entity
@Table(name = "test_submission")
public class TestSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;
    
    @Column(name = "submission_uuid", unique = true, nullable = false)
    private String submissionUuid;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private IeltsWritingTest test;
    
    @Column(name = "test_type", length = 20)
    private String testType = "writing"; // writing, speaking, reading, listening
    
    @Column(name = "task1_answer", columnDefinition = "TEXT")
    private String task1Answer;
    
    @Column(name = "task2_answer", columnDefinition = "TEXT")
    private String task2Answer;
    
    @Column(name = "task1_word_count")
    private Integer task1WordCount;
    
    @Column(name = "task2_word_count")
    private Integer task2WordCount;
    
    @Column(name = "time_spent")
    private Integer timeSpent; // in seconds
    
    @Column(name = "status", length = 20)
    private String status = "pending"; // pending, processing, completed, failed
    
    @Column(name = "task1_result", columnDefinition = "TEXT")
    private String task1Result; // JSON string from API
    
    @Column(name = "task2_result", columnDefinition = "TEXT")
    private String task2Result; // JSON string from API
    
    @Column(name = "task1_score")
    private Double task1Score;
    
    @Column(name = "task2_score")
    private Double task2Score;
    
    @Column(name = "overall_score")
    private Double overallScore;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    // Constructors
    public TestSubmission() {
        this.submittedAt = LocalDateTime.now();
        this.status = "pending";
    }
    
    public TestSubmission(User user, IeltsWritingTest test, String task1Answer, String task2Answer) {
        this();
        this.user = user;
        this.test = test;
        this.task1Answer = task1Answer;
        this.task2Answer = task2Answer;
    }
    
    // Getters and Setters
    public Long getSubmissionId() {
        return submissionId;
    }
    
    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }
    
    public String getSubmissionUuid() {
        return submissionUuid;
    }
    
    public void setSubmissionUuid(String submissionUuid) {
        this.submissionUuid = submissionUuid;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public IeltsWritingTest getTest() {
        return test;
    }
    
    public void setTest(IeltsWritingTest test) {
        this.test = test;
    }
    
    public String getTestType() {
        return testType;
    }
    
    public void setTestType(String testType) {
        this.testType = testType;
    }
    
    public String getTask1Answer() {
        return task1Answer;
    }
    
    public void setTask1Answer(String task1Answer) {
        this.task1Answer = task1Answer;
    }
    
    public String getTask2Answer() {
        return task2Answer;
    }
    
    public void setTask2Answer(String task2Answer) {
        this.task2Answer = task2Answer;
    }
    
    public Integer getTask1WordCount() {
        return task1WordCount;
    }
    
    public void setTask1WordCount(Integer task1WordCount) {
        this.task1WordCount = task1WordCount;
    }
    
    public Integer getTask2WordCount() {
        return task2WordCount;
    }
    
    public void setTask2WordCount(Integer task2WordCount) {
        this.task2WordCount = task2WordCount;
    }
    
    public Integer getTimeSpent() {
        return timeSpent;
    }
    
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public Double getTask1Score() {
        return task1Score;
    }
    
    public void setTask1Score(Double task1Score) {
        this.task1Score = task1Score;
    }
    
    public Double getTask2Score() {
        return task2Score;
    }
    
    public void setTask2Score(Double task2Score) {
        this.task2Score = task2Score;
    }
    
    public Double getOverallScore() {
        return overallScore;
    }
    
    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    // Helper methods
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    public boolean isPending() {
        return "pending".equals(status);
    }
    
    public boolean isProcessing() {
        return "processing".equals(status);
    }
    
    public boolean isFailed() {
        return "failed".equals(status);
    }
    
    public String getTestDisplayName() {
        if (test != null) {
            return "CAM " + test.getCamNumber() + " - Test " + test.getTestNumber();
        }
        return "Unknown Test";
    }
    
    public String getStatusDisplay() {
        switch (status) {
            case "pending": return "Đang chờ";
            case "processing": return "Đang chấm";
            case "completed": return "Hoàn thành";
            case "failed": return "Thất bại";
            default: return status;
        }
    }
    
    public String getStatusColor() {
        switch (status) {
            case "pending": return "#ff9800";
            case "processing": return "#2196F3";
            case "completed": return "#4caf50";
            case "failed": return "#f44336";
            default: return "#999";
        }
    }
    
    @Override
    public String toString() {
        return "TestSubmission{" +
                "submissionId=" + submissionId +
                ", submissionUuid='" + submissionUuid + '\'' +
                ", status='" + status + '\'' +
                ", overallScore=" + overallScore +
                ", submittedAt=" + submittedAt +
                '}';
    }
}