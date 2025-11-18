package com.ieltsgrading.ielts_evaluator.service;

import com.ieltsgrading.ielts_evaluator.model.*;
import com.ieltsgrading.ielts_evaluator.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Unified Test Submission Service - FIXED VERSION
 */
@Service
public class TestSubmissionService {

    @Autowired
    private WritingSubmissionRepository writingSubmissionRepo;

    @Autowired
    private SpeakingSubmissionRepository speakingSubmissionRepo;

    @Autowired
    private IeltsWritingTestRepository writingTestRepo;

    @Autowired
    private WritingApiService writingApiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create Writing Submission
     */
    @Transactional
    public WritingSubmission createWritingSubmission(
            User user, 
            IeltsWritingTest test,
            String task1Answer, 
            String task2Answer,
            int task1Words, 
            int task2Words, 
            int timeSpent) {

        WritingSubmission submission = new WritingSubmission();
        submission.setSubmissionUuid(UUID.randomUUID().toString());
        submission.setUser(user);
        submission.setTestId(test.getTestId());
        submission.setTest(test);
        submission.setTask1Answer(task1Answer);
        submission.setTask2Answer(task2Answer);
        submission.setTask1WordCount(task1Words);
        submission.setTask2WordCount(task2Words);
        submission.setTimeSpent(timeSpent);
        submission.setStatus("pending");

        WritingSubmission saved = writingSubmissionRepo.save(submission);

        System.out.println("✅ Created WritingSubmission: " + saved.getSubmissionUuid());
        return saved;
    }

    /**
     * Process Writing Submission Asynchronously
     */
    @Transactional
    public void processWritingSubmissionAsync(String submissionUuid) {
        new Thread(() -> {
            try {
                processWritingSubmission(submissionUuid);
            } catch (Exception e) {
                System.err.println("❌ Async processing error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * ✅ FIXED: Process Writing Submission - Parse JSON correctly
     */
    @Transactional
    public void processWritingSubmission(String submissionUuid) {
        try {
            Optional<WritingSubmission> optSubmission = writingSubmissionRepo
                    .findBySubmissionUuid(submissionUuid);

            if (optSubmission.isEmpty()) {
                System.err.println("❌ Submission not found: " + submissionUuid);
                return;
            }

            WritingSubmission submission = optSubmission.get();
            submission.setStatus("processing");
            writingSubmissionRepo.save(submission);

            System.out.println("========================================");
            System.out.println("📝 PROCESSING WRITING SUBMISSION");
            System.out.println("   UUID: " + submissionUuid);
            System.out.println("========================================");

            // Get test details
            Optional<IeltsWritingTest> testOpt = writingTestRepo.findById(submission.getTestId());
            if (testOpt.isEmpty()) {
                throw new RuntimeException("Test not found: " + submission.getTestId());
            }

            IeltsWritingTest test = testOpt.get();
            submission.setTest(test);

            // Call AI API
            System.out.println("📡 Calling AI API...");
            Map<String, Object> apiResults = writingApiService.submitCompleteTest(
                    test.getTask1Question(),
                    submission.getTask1Answer(),
                    test.getTask2Question(),
                    submission.getTask2Answer(),
                    test.getDirectImageUrl()
            );

            if ("error".equals(apiResults.get("status"))) {
                throw new RuntimeException("API Error: " + apiResults.get("message"));
            }

            // ✅ FIX: Parse results correctly
            @SuppressWarnings("unchecked")
            Map<String, Object> task1Result = (Map<String, Object>) apiResults.get("task1");
            @SuppressWarnings("unchecked")
            Map<String, Object> task2Result = (Map<String, Object>) apiResults.get("task2");

            // ✅ FIX: Extract scores from correct structure
            Double task1Score = extractOverallBand(task1Result);
            Double task2Score = extractOverallBand(task2Result);
            Double overallScore = (Double) apiResults.get("overallScore");

            System.out.println("\n--- SCORES TO SAVE ---");
            System.out.println("Task 1 Score: " + task1Score);
            System.out.println("Task 2 Score: " + task2Score);
            System.out.println("Overall Score: " + overallScore);

            // ✅ FIX: Save complete JSON results (not just message)
            submission.setTask1Result(convertToJson(task1Result));
            submission.setTask2Result(convertToJson(task2Result));
            submission.setTask1Score(task1Score);
            submission.setTask2Score(task2Score);
            submission.setOverallScore(overallScore);
            submission.setStatus("completed");
            submission.setCompletedAt(java.time.LocalDateTime.now());

            writingSubmissionRepo.save(submission);

            System.out.println("✅ SUBMISSION COMPLETED & SAVED");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("❌ ERROR PROCESSING SUBMISSION: " + submissionUuid);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();

            try {
                Optional<WritingSubmission> optSubmission = writingSubmissionRepo
                        .findBySubmissionUuid(submissionUuid);
                if (optSubmission.isPresent()) {
                    WritingSubmission submission = optSubmission.get();
                    submission.setStatus("failed");
                    submission.setErrorMessage(e.getMessage());
                    submission.setCompletedAt(java.time.LocalDateTime.now());
                    writingSubmissionRepo.save(submission);
                }
            } catch (Exception ex) {
                System.err.println("❌ Failed to update status: " + ex.getMessage());
            }
        }
    }

    /**
     * ✅ NEW: Extract overall_band from API response
     */
    private Double extractOverallBand(Map<String, Object> apiResponse) {
        if (apiResponse == null) {
            System.err.println("⚠️ API response is null");
            return null;
        }
        
        try {
            // Direct overall_band field
            Object overallBand = apiResponse.get("overall_band");
            if (overallBand != null) {
                if (overallBand instanceof Number) {
                    return ((Number) overallBand).doubleValue();
                }
                if (overallBand instanceof String) {
                    return Double.parseDouble((String) overallBand);
                }
            }
            
            System.err.println("⚠️ No overall_band in response");
            System.err.println("   Response keys: " + apiResponse.keySet());
            System.err.println("   Response: " + apiResponse);
            
        } catch (Exception e) {
            System.err.println("❌ Error extracting score: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get all user submissions
     */
    @Transactional(readOnly = true)
    public List<ITestSubmission> getUserSubmissions(User user) {
        List<ITestSubmission> allSubmissions = new ArrayList<>();

        List<WritingSubmission> writingSubmissions = writingSubmissionRepo
                .findByUserOrderBySubmittedAtDesc(user);
        
        for (WritingSubmission ws : writingSubmissions) {
            writingTestRepo.findById(ws.getTestId()).ifPresent(ws::setTest);
        }
        
        allSubmissions.addAll(writingSubmissions);

        List<SpeakingSubmission> speakingSubmissions = speakingSubmissionRepo
                .findByUserOrderBySubmittedAtDesc(user);
        allSubmissions.addAll(speakingSubmissions);

        allSubmissions.sort(Comparator.comparing(
                ITestSubmission::getSubmittedAt, 
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return allSubmissions;
    }

    /**
     * Get pending submissions
     */
    @Transactional(readOnly = true)
    public List<ITestSubmission> getPendingSubmissions(User user) {
        List<ITestSubmission> pending = new ArrayList<>();

        List<WritingSubmission> writingPending = writingSubmissionRepo
                .findByUserIdAndStatus(user.getId(), "pending");
        for (WritingSubmission ws : writingPending) {
            writingTestRepo.findById(ws.getTestId()).ifPresent(ws::setTest);
        }
        pending.addAll(writingPending);

        List<WritingSubmission> writingProcessing = writingSubmissionRepo
                .findByUserIdAndStatus(user.getId(), "processing");
        for (WritingSubmission ws : writingProcessing) {
            writingTestRepo.findById(ws.getTestId()).ifPresent(ws::setTest);
        }
        pending.addAll(writingProcessing);

        pending.addAll(speakingSubmissionRepo
                .findByUserIdAndStatus(user.getId(), "pending"));
        pending.addAll(speakingSubmissionRepo
                .findByUserIdAndStatus(user.getId(), "processing"));

        return pending;
    }

    /**
     * Get submission status
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSubmissionStatus(String submissionUuid) {
        Map<String, Object> statusMap = new HashMap<>();

        Optional<WritingSubmission> writingOpt = writingSubmissionRepo
                .findBySubmissionUuid(submissionUuid);
        
        if (writingOpt.isPresent()) {
            WritingSubmission submission = writingOpt.get();
            writingTestRepo.findById(submission.getTestId()).ifPresent(submission::setTest);
            
            statusMap.put("submissionUuid", submissionUuid);
            statusMap.put("testType", "writing");
            statusMap.put("status", submission.getStatus());
            statusMap.put("overallScore", submission.getOverallScore());
            statusMap.put("task1Score", submission.getTask1Score());
            statusMap.put("task2Score", submission.getTask2Score());
            statusMap.put("completedAt", submission.getCompletedAt());
            statusMap.put("errorMessage", submission.getErrorMessage());
            return statusMap;
        }

        Optional<SpeakingSubmission> speakingOpt = speakingSubmissionRepo
                .findBySubmissionUuid(submissionUuid);
        
        if (speakingOpt.isPresent()) {
            SpeakingSubmission submission = speakingOpt.get();
            statusMap.put("submissionUuid", submissionUuid);
            statusMap.put("testType", "speaking");
            statusMap.put("status", submission.getStatus());
            statusMap.put("overallScore", submission.getOverallScore());
            statusMap.put("completedAt", submission.getCompletedAt());
            statusMap.put("errorMessage", submission.getErrorMessage());
            return statusMap;
        }

        statusMap.put("error", "Submission not found");
        return statusMap;
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        long writingTotal = writingSubmissionRepo.countByUser(user);
        long writingCompleted = writingSubmissionRepo.countByUserAndStatus(user, "completed");
        long writingProcessing = writingSubmissionRepo.countByUserAndStatus(user, "pending") +
                                 writingSubmissionRepo.countByUserAndStatus(user, "processing");
        Double writingAvg = writingSubmissionRepo.getAverageScoreByUserId(user.getId());

        long speakingTotal = speakingSubmissionRepo.countByUser(user);
        long speakingCompleted = speakingSubmissionRepo.countByUserAndStatus(user, "completed");
        long speakingProcessing = speakingSubmissionRepo.countByUserAndStatus(user, "pending") +
                                  speakingSubmissionRepo.countByUserAndStatus(user, "processing");
        Double speakingAvg = speakingSubmissionRepo.getAverageScoreByUserId(user.getId());

        stats.put("totalTests", writingTotal + speakingTotal);
        stats.put("completedTests", writingCompleted + speakingCompleted);
        stats.put("processingTests", writingProcessing + speakingProcessing);
        
        Double overallAvg = 0.0;
        int count = 0;
        if (writingAvg != null) {
            overallAvg += writingAvg;
            count++;
        }
        if (speakingAvg != null) {
            overallAvg += speakingAvg;
            count++;
        }
        if (count > 0) {
            overallAvg = Math.round((overallAvg / count) * 10.0) / 10.0;
        }
        
        stats.put("averageScore", count > 0 ? overallAvg : 0.0);

        return stats;
    }

    /**
     * Get submission by UUID
     */
    @Transactional(readOnly = true)
    public Optional<ITestSubmission> getSubmission(String submissionUuid) {
        Optional<WritingSubmission> writingOpt = writingSubmissionRepo
                .findBySubmissionUuid(submissionUuid);
        if (writingOpt.isPresent()) {
            WritingSubmission ws = writingOpt.get();
            writingTestRepo.findById(ws.getTestId()).ifPresent(ws::setTest);
            return Optional.of(ws);
        }

        Optional<SpeakingSubmission> speakingOpt = speakingSubmissionRepo
                .findBySubmissionUuid(submissionUuid);
        return speakingOpt.map(s -> (ITestSubmission) s);
    }

    /**
     * Parse detailed result from JSON string
     */
    public Map<String, Object> parseDetailedResult(String jsonResult) {
        if (jsonResult == null || jsonResult.isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(jsonResult, Map.class);
        } catch (Exception e) {
            System.err.println("Error parsing result JSON: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            System.err.println("Error converting to JSON: " + e.getMessage());
            return "{}";
        }
    }
}