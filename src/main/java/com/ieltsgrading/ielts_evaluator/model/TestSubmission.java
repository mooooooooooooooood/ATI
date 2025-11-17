package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "test_submission")
public class TestSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "submission_uuid", nullable = false, unique = true)
    private String submissionUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private IeltsWritingTest test;

    @Column(name = "test_type", length = 20)
    private String testType; // Will be set based on test type

    /* ============================ WRITING FIELDS ============================ */
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

    /* ============================ READING / LISTENING ============================ */
    @Column(name = "objective_answer", columnDefinition = "JSON")
    private String objectiveAnswer;

    @Column(name = "objective_result", columnDefinition = "JSON")
    private String objectiveResult;

    @Column(name = "objective_score")
    private Double objectiveScore;

    /* ============================ SPEAKING FIELDS ============================ */
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "speaking_result", columnDefinition = "JSON")
    private String speakingResult;

    @Column(name = "speaking_score")
    private Double speakingScore;

    /* ============================ SHARED FIELDS ============================ */
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

    /* ============================ DISPLAY METHODS ============================ */
    
    /**
     * Get test display name for dashboard
     * Example: "CAM 20 - Writing Test 4"
     */
    public String getTestDisplayName() {
        if (test != null) {
            return "CAM " + test.getCamNumber() + " - Writing Test " + test.getTestNumber();
        }
        // Fallback
        return testType.substring(0, 1).toUpperCase() + testType.substring(1) + " Test";
    }

    /**
     * Get status display text
     * Returns: "Completed", "Processing", "Pending", "Failed"
     */
    public String getStatusDisplay() {
        if (status == null) return "Unknown";
        
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

    /**
     * Get status color for badges
     */
    public String getStatusColor() {
        if (status == null) return "#999";
        
        switch (status.toLowerCase()) {
            case "completed":
                return "#4caf50"; // Green
            case "processing":
                return "#2196f3"; // Blue
            case "pending":
                return "#ff9800"; // Orange
            case "failed":
                return "#f44336"; // Red
            default:
                return "#999";
        }
    }

    /**
     * Get formatted time spent
     * Example: "45 min"
     */
    public String getFormattedTimeSpent() {
        if (timeSpent == null || timeSpent == 0) {
            return "N/A";
        }
        
        int minutes = timeSpent / 60;
        int seconds = timeSpent % 60;
        
        if (minutes > 0) {
            return minutes + " min" + (seconds > 0 ? " " + seconds + " sec" : "");
        } else {
            return seconds + " sec";
        }
    }

    /**
     * Get Task 1 feedback from JSON result
     */
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

    /**
     * Get Task 2 feedback from JSON result
     */
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

    /* ============================ JSON PARSER ============================ */
    
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

    public Map<String, Object> getObjectiveResultMap() {
        return parseJson(objectiveResult);
    }

    public Map<String, Object> getSpeakingResultMap() {
        return parseJson(speakingResult);
    }

    /* ============================ STATUS HELPERS ============================ */
    
    public boolean isCompleted() { 
        return "completed".equalsIgnoreCase(status); 
    }
    
    public boolean isPending() { 
        return "pending".equalsIgnoreCase(status); 
    }
    
    public boolean isProcessing() { 
        return "processing".equalsIgnoreCase(status); 
    }
    
    public boolean isFailed() { 
        return "failed".equalsIgnoreCase(status); 
    }

    /* ============================ GETTERS & SETTERS ============================ */

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

    @Override
    public String toString() {
        return "TestSubmission{" +
                "id=" + submissionId +
                ", uuid='" + submissionUuid + '\'' +
                ", type='" + testType + '\'' +
                ", status='" + status + '\'' +
                ", score=" + overallScore +
                '}';
    }
}