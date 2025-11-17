package com.ieltsgrading.ielts_evaluator.model;

import java.time.LocalDateTime;

/**
 * Interface chung cho tất cả submission types
 * Cho phép xử lý polymorphic cho Writing, Speaking, Listening, Reading
 */
public interface ITestSubmission {
    
    // Identity methods
    String getSubmissionUuid();
    Long getSubmissionId();
    
    // User info
    User getUser();
    Long getUserId();
    
    // Test info
    String getTestType(); // "writing", "speaking", "listening", "reading"
    Integer getTestId();
    String getTestDisplayName(); // "CAM 20 - Writing Test 4"
    
    // Status
    String getStatus(); // "pending", "processing", "completed", "failed"
    String getStatusDisplay(); // "Completed", "Processing", etc.
    String getStatusColor(); // "#4caf50", "#2196f3", etc.
    boolean isCompleted();
    boolean isProcessing();
    boolean isPending();
    boolean isFailed();
    
    // Score
    Double getOverallScore();
    
    // Time
    LocalDateTime getSubmittedAt();
    LocalDateTime getCompletedAt();
    Integer getTimeSpent();
    String getFormattedTimeSpent();
    
    // Error
    String getErrorMessage();
    
    // Type-specific data (có thể null cho các loại khác)
    default String getTask1Answer() { return null; }
    default String getTask2Answer() { return null; }
    default Integer getTask1WordCount() { return null; }
    default Integer getTask2WordCount() { return null; }
    default Double getTask1Score() { return null; }
    default Double getTask2Score() { return null; }
    default String getTask1Feedback() { return null; }
    default String getTask2Feedback() { return null; }
}