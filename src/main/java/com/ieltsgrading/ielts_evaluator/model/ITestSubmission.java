package com.ieltsgrading.ielts_evaluator.model;

import java.time.LocalDateTime;

/**
 * Interface for Test Submissions
 * Provides common methods for both Writing and Speaking submissions
 */
public interface ITestSubmission {
    
    /**
     * Get submission UUID (unique identifier)
     */
    String getSubmissionUuid();
    
    /**
     * Get submission ID (database primary key)
     */
    Long getSubmissionId();
    
    /**
     * Get user who submitted this test
     */
    User getUser();
    
    /**
     * Get user ID
     */
    Long getUserId();
    
    /**
     * Get test type: "writing" or "speaking"
     */
    String getTestType();
    
    /**
     * Get test ID (foreign key to ielts_writing_test or speaking_tests)
     */
    Integer getTestId();
    
    /**
     * Get display name for the test (e.g., "CAM 20 - Writing Test 1")
     */
    String getTestDisplayName();
    
    /**
     * Get submission status: "pending", "processing", "completed", "failed"
     */
    String getStatus();
    
    /**
     * Get human-readable status display
     */
    String getStatusDisplay();
    
    /**
     * Get color code for status badge
     */
    String getStatusColor();
    
    /**
     * Check if submission is completed
     */
    boolean isCompleted();
    
    /**
     * Check if submission is being processed
     */
    boolean isProcessing();
    
    /**
     * Check if submission is pending
     */
    boolean isPending();
    
    /**
     * Check if submission failed
     */
    boolean isFailed();
    
    /**
     * Get overall band score
     */
    Double getOverallScore();
    
    /**
     * Get submission timestamp
     */
    LocalDateTime getSubmittedAt();
    
    /**
     * Get completion timestamp
     */
    LocalDateTime getCompletedAt();
    
    /**
     * Get time spent on test (in seconds)
     */
    Integer getTimeSpent();
    
    /**
     * Get formatted time spent string (e.g., "45 min 30 sec")
     */
    String getFormattedTimeSpent();
    
    /**
     * Get error message if submission failed
     */
    String getErrorMessage();
    
    // ==================== Writing-specific methods ====================
    // These return null for speaking submissions
    
    /**
     * Get Task 1 answer (writing only)
     */
    default String getTask1Answer() { return null; }
    
    /**
     * Get Task 2 answer (writing only)
     */
    default String getTask2Answer() { return null; }
    
    /**
     * Get Task 1 word count (writing only)
     */
    default Integer getTask1WordCount() { return null; }
    
    /**
     * Get Task 2 word count (writing only)
     */
    default Integer getTask2WordCount() { return null; }
    
    /**
     * Get Task 1 score (writing only)
     */
    default Double getTask1Score() { return null; }
    
    /**
     * Get Task 2 score (writing only)
     */
    default Double getTask2Score() { return null; }
    
    /**
     * Get Task 1 feedback HTML (writing only)
     */
    default String getTask1Feedback() { return null; }
    
    /**
     * Get Task 2 feedback HTML (writing only)
     */
    default String getTask2Feedback() { return null; }
    
    // ==================== Speaking-specific methods ====================
    // These return null for writing submissions
    
    /**
     * Get audio URL (speaking only)
     */
    default String getAudioUrl() { return null; }
    
    /**
     * Get speaking result JSON (speaking only)
     */
    default String getSpeakingResult() { return null; }
}