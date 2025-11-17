package com.ieltsgrading.ielts_evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.model.IeltsWritingTest;
import com.ieltsgrading.ielts_evaluator.model.TestSubmission;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.repository.TestSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TestSubmissionService {
    
    @Autowired
    private TestSubmissionRepository submissionRepository;
    
    @Autowired
    private WritingApiService writingApiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create new submission and save to database
     * 🔥 NOW SETS testType = "writing"
     */
    @Transactional
    public TestSubmission createSubmission(User user, IeltsWritingTest test, 
                                          String task1Answer, String task2Answer,
                                          int task1Words, int task2Words, int timeSpent) {
        
        TestSubmission submission = new TestSubmission();
        submission.setSubmissionUuid(UUID.randomUUID().toString());
        submission.setUser(user);
        submission.setTest(test);
        
        // 🔥 SET TEST TYPE
        submission.setTestType("writing");
        
        submission.setTask1Answer(task1Answer);
        submission.setTask2Answer(task2Answer);
        submission.setTask1WordCount(task1Words);
        submission.setTask2WordCount(task2Words);
        submission.setTimeSpent(timeSpent);
        submission.setStatus("pending");
        submission.setSubmittedAt(LocalDateTime.now());
        
        System.out.println("✅ Creating submission:");
        System.out.println("   UUID: " + submission.getSubmissionUuid());
        System.out.println("   Test Type: " + submission.getTestType());
        System.out.println("   Status: " + submission.getStatus());
        
        return submissionRepository.save(submission);
    }
    
    /**
     * Process submission asynchronously - Call external API
     */
    @Transactional
    public void processSubmissionAsync(String submissionUuid) {
        new Thread(() -> processSubmission(submissionUuid)).start();
    }
    
    /**
     * Process submission - Call external WritingApiService to grade the test
     */
    @Transactional
    public void processSubmission(String submissionUuid) {
        try {
            Optional<TestSubmission> optSubmission = submissionRepository.findBySubmissionUuid(submissionUuid);
            if (optSubmission.isEmpty()) {
                System.err.println("❌ Submission not found: " + submissionUuid);
                return;
            }
            
            TestSubmission submission = optSubmission.get();
            IeltsWritingTest test = submission.getTest();
            
            // Update status to processing
            submission.setStatus("processing");
            submissionRepository.save(submission);
            
            System.out.println("========================================");
            System.out.println("📄 PROCESSING SUBMISSION: " + submissionUuid);
            System.out.println("   User: " + submission.getUser().getName());
            System.out.println("   Test: " + test.getDisplayId());
            System.out.println("   Test Type: " + submission.getTestType());
            System.out.println("========================================");
            
            // Call WritingApiService.submitCompleteTest()
            Map<String, Object> apiResults = writingApiService.submitCompleteTest(
                test.getTask1Question(),
                submission.getTask1Answer(),
                test.getTask2Question(),
                submission.getTask2Answer(),
                test.getDirectImageUrl()
            );
            
            System.out.println("🔥 API Response received: " + apiResults);
            
            // Check if API call was successful
            if (apiResults != null && "success".equals(apiResults.get("status"))) {
                
                // Extract Task 1 result
                @SuppressWarnings("unchecked")
                Map<String, Object> task1Result = (Map<String, Object>) apiResults.get("task1");
                if (task1Result != null) {
                    submission.setTask1Result(objectMapper.writeValueAsString(task1Result));
                    Double task1Score = extractScore(task1Result.get("message"));
                    submission.setTask1Score(task1Score);
                    System.out.println("   ✅ Task 1 Score: " + task1Score);
                } else {
                    System.err.println("   ⚠️ Task 1 result is null");
                }
                
                // Extract Task 2 result
                @SuppressWarnings("unchecked")
                Map<String, Object> task2Result = (Map<String, Object>) apiResults.get("task2");
                if (task2Result != null) {
                    submission.setTask2Result(objectMapper.writeValueAsString(task2Result));
                    Double task2Score = extractScore(task2Result.get("message"));
                    submission.setTask2Score(task2Score);
                    System.out.println("   ✅ Task 2 Score: " + task2Score);
                } else {
                    System.err.println("   ⚠️ Task 2 result is null");
                }
                
                // Calculate overall score (Task 1 = 33%, Task 2 = 67%)
                if (submission.getTask1Score() != null && submission.getTask2Score() != null) {
                    double overall = (submission.getTask1Score() / 3.0) + (submission.getTask2Score() * 2.0 / 3.0);
                    overall = Math.round(overall * 2.0) / 2.0; // Round to nearest 0.5
                    submission.setOverallScore(overall);
                    System.out.println("   ✅ Overall Score: " + overall);
                } else {
                    System.err.println("   ⚠️ Cannot calculate overall score - missing band scores");
                }
                
                // Check if we got overall score from API response
                if (apiResults.containsKey("overallScore") && submission.getOverallScore() == null) {
                    Object apiOverallScore = apiResults.get("overallScore");
                    if (apiOverallScore instanceof Number) {
                        submission.setOverallScore(((Number) apiOverallScore).doubleValue());
                        System.out.println("   ℹ️ Using overall score from API: " + submission.getOverallScore());
                    }
                }
                
                submission.setStatus("completed");
                submission.setCompletedAt(LocalDateTime.now());
                
                System.out.println("✅ SUBMISSION COMPLETED SUCCESSFULLY");
                System.out.println("========================================\n");
                
            } else {
                String errorMsg = apiResults != null ? String.valueOf(apiResults.get("message")) : "API returned error status";
                throw new Exception(errorMsg);
            }
            
            submissionRepository.save(submission);
            
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ ERROR PROCESSING SUBMISSION: " + submissionUuid);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================\n");
            
            // Update status to failed
            try {
                Optional<TestSubmission> optSubmission = submissionRepository.findBySubmissionUuid(submissionUuid);
                if (optSubmission.isPresent()) {
                    TestSubmission submission = optSubmission.get();
                    submission.setStatus("failed");
                    submission.setErrorMessage(e.getMessage());
                    submission.setCompletedAt(LocalDateTime.now());
                    submissionRepository.save(submission);
                    System.out.println("💾 Submission marked as failed in database");
                }
            } catch (Exception ex) {
                System.err.println("❌ Failed to update submission status: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Get submission by UUID
     */
    public Optional<TestSubmission> getSubmission(String submissionUuid) {
        return submissionRepository.findBySubmissionUuid(submissionUuid);
    }
    
    /**
     * Get all submissions by user
     */
    public List<TestSubmission> getUserSubmissions(User user) {
        return submissionRepository.findByUserOrderBySubmittedAtDesc(user);
    }
    
    /**
     * Get pending submissions by user
     */
    public List<TestSubmission> getPendingSubmissions(Long userId) {
        return submissionRepository.findPendingByUserId(userId);
    }
    
    /**
     * Get completed submissions by user
     */
    public List<TestSubmission> getCompletedSubmissions(Long userId) {
        return submissionRepository.findCompletedByUserId(userId);
    }
    
    /**
     * Get user statistics
     */
    public Map<String, Object> getUserStats(User user) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalTests = submissionRepository.countByUser(user);
        long completedTests = submissionRepository.countByUserAndStatus(user, "completed");
        long processingTests = submissionRepository.countByUserAndStatus(user, "processing") +
                               submissionRepository.countByUserAndStatus(user, "pending");
        
        Double avgScore = submissionRepository.getAverageScoreByUserId(user.getId());
        
        stats.put("totalTests", totalTests);
        stats.put("completedTests", completedTests);
        stats.put("processingTests", processingTests);
        stats.put("averageScore", avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);
        
        return stats;
    }
    
    /**
     * Parse API result to get detailed feedback
     */
    public Map<String, Object> parseDetailedResult(String resultJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(resultJson, Map.class);
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error parsing result JSON: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Extract band score from API message
     */
    private Double extractScore(Object message) {
        if (message == null) {
            System.err.println("⚠️ Message is null, cannot extract score");
            return null;
        }
        
        String messageStr = message.toString();
        messageStr = messageStr.replaceAll("<[^>]*>", "");
        
        // Try pattern: "Band X.X"
        Pattern pattern1 = Pattern.compile("Band\\s+(\\d+\\.?\\d*)");
        Matcher matcher1 = pattern1.matcher(messageStr);
        if (matcher1.find()) {
            try {
                Double score = Double.parseDouble(matcher1.group(1));
                System.out.println("   📊 Extracted score (Band format): " + score);
                return score;
            } catch (NumberFormatException e) {
                System.err.println("   ⚠️ Could not parse band score: " + matcher1.group(1));
            }
        }
        
        // Try pattern: numeric
        Pattern pattern2 = Pattern.compile("(\\d+\\.\\d+)");
        Matcher matcher2 = pattern2.matcher(messageStr);
        if (matcher2.find()) {
            try {
                Double score = Double.parseDouble(matcher2.group(1));
                if (score >= 0.0 && score <= 9.0) {
                    System.out.println("   📊 Extracted score (numeric format): " + score);
                    return score;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        System.err.println("   ⚠️ No valid band score found in message");
        return null;
    }
    
    /**
     * Retry failed submission
     */
    @Transactional
    public void retrySubmission(String submissionUuid) {
        Optional<TestSubmission> optSubmission = submissionRepository.findBySubmissionUuid(submissionUuid);
        if (optSubmission.isPresent()) {
            TestSubmission submission = optSubmission.get();
            if (submission.isFailed()) {
                submission.setStatus("pending");
                submission.setErrorMessage(null);
                submissionRepository.save(submission);
                
                processSubmissionAsync(submissionUuid);
                System.out.println("🔄 Retrying failed submission: " + submissionUuid);
            }
        }
    }
}