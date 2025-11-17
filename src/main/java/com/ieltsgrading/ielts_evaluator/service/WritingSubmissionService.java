package com.ieltsgrading.ielts_evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.model.*;
import com.ieltsgrading.ielts_evaluator.repository.WritingSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WritingSubmissionService {
    
    @Autowired
    private WritingSubmissionRepository submissionRepository;
    
    @Autowired
    private WritingApiService writingApiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create new WritingSubmission
     */
    @Transactional
    public WritingSubmission createSubmission(User user, IeltsWritingTest test, 
                                              String task1Answer, String task2Answer,
                                              int task1Words, int task2Words, int timeSpent) {
        
        WritingSubmission submission = new WritingSubmission();
        submission.setSubmissionUuid(UUID.randomUUID().toString());
        submission.setUser(user);
        submission.setTestId(test.getTestId());
        submission.setTest(test); // Set transient field
        submission.setTask1Answer(task1Answer);
        submission.setTask2Answer(task2Answer);
        submission.setTask1WordCount(task1Words);
        submission.setTask2WordCount(task2Words);
        submission.setTimeSpent(timeSpent);
        submission.setStatus("pending");
        submission.setSubmittedAt(LocalDateTime.now());
        
        System.out.println("✅ Creating WritingSubmission:");
        System.out.println("   UUID: " + submission.getSubmissionUuid());
        System.out.println("   Test Type: " + submission.getTestType());
        System.out.println("   Test ID: " + test.getTestId());
        
        return submissionRepository.save(submission);
    }
    
    /**
     * Process submission asynchronously
     */
    @Transactional
    public void processSubmissionAsync(String submissionUuid) {
        new Thread(() -> processSubmission(submissionUuid)).start();
    }
    
    /**
     * Process submission - Call external API
     */
    @Transactional
    public void processSubmission(String submissionUuid) {
        try {
            Optional<WritingSubmission> optSubmission = submissionRepository.findBySubmissionUuid(submissionUuid);
            if (optSubmission.isEmpty()) {
                System.err.println("❌ Submission not found: " + submissionUuid);
                return;
            }
            
            WritingSubmission submission = optSubmission.get();
            
            // Update status
            submission.setStatus("processing");
            submissionRepository.save(submission);
            
            System.out.println("========================================");
            System.out.println("📄 PROCESSING SUBMISSION: " + submissionUuid);
            System.out.println("   User ID: " + submission.getUserId());
            System.out.println("   Test ID: " + submission.getTestId());
            System.out.println("========================================");
            
            // TODO: Get test details and call API
            // For now, just simulate completion
            Thread.sleep(5000); // Simulate processing
            
            // Mock scores for testing
            submission.setTask1Score(6.5);
            submission.setTask2Score(7.0);
            submission.setOverallScore(6.5);
            submission.setStatus("completed");
            submission.setCompletedAt(LocalDateTime.now());
            
            submissionRepository.save(submission);
            
            System.out.println("✅ SUBMISSION COMPLETED");
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR PROCESSING SUBMISSION: " + submissionUuid);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
            
            try {
                Optional<WritingSubmission> optSubmission = submissionRepository.findBySubmissionUuid(submissionUuid);
                if (optSubmission.isPresent()) {
                    WritingSubmission submission = optSubmission.get();
                    submission.setStatus("failed");
                    submission.setErrorMessage(e.getMessage());
                    submission.setCompletedAt(LocalDateTime.now());
                    submissionRepository.save(submission);
                }
            } catch (Exception ex) {
                System.err.println("❌ Failed to update status: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Get submission by UUID
     */
    public Optional<WritingSubmission> getSubmission(String submissionUuid) {
        return submissionRepository.findBySubmissionUuid(submissionUuid);
    }
    
    /**
     * Retry failed submission
     */
    @Transactional
    public void retrySubmission(String submissionUuid) {
        Optional<WritingSubmission> optSubmission = submissionRepository.findBySubmissionUuid(submissionUuid);
        if (optSubmission.isPresent()) {
            WritingSubmission submission = optSubmission.get();
            if (submission.isFailed()) {
                submission.setStatus("pending");
                submission.setErrorMessage(null);
                submissionRepository.save(submission);
                
                processSubmissionAsync(submissionUuid);
                System.out.println("🔄 Retrying submission: " + submissionUuid);
            }
        }
    }
}