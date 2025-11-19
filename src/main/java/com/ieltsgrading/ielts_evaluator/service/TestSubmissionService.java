package com.ieltsgrading.ielts_evaluator.service;

import com.ieltsgrading.ielts_evaluator.util.IeltsScoreRounder;
import com.ieltsgrading.ielts_evaluator.model.*;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingSubmissionDetail;
import com.ieltsgrading.ielts_evaluator.repository.*;
import com.ieltsgrading.ielts_evaluator.repository.speaking.*;
import com.ieltsgrading.ielts_evaluator.dto.speaking.QuestionQueueItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TestSubmissionService {

    @Autowired
    private WritingSubmissionRepository writingSubmissionRepo;

    @Autowired
    private SpeakingSubmissionRepository speakingSubmissionRepo;

    @Autowired
    private SpeakingSubmissionDetailRepository speakingDetailRepo;

    @Autowired
    private IeltsWritingTestRepository writingTestRepo;

    @Autowired
    private SpeakingTestRepository speakingTestRepo;

    @Autowired
    private WritingApiService writingApiService;

    @Autowired
    private SpeakingApiService speakingApiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /* ==================== WRITING SUBMISSION ==================== */

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

            Optional<IeltsWritingTest> testOpt = writingTestRepo.findById(submission.getTestId());
            if (testOpt.isEmpty()) {
                throw new RuntimeException("Test not found: " + submission.getTestId());
            }

            IeltsWritingTest test = testOpt.get();
            submission.setTest(test);

            System.out.println("📡 Calling AI API...");
            Map<String, Object> apiResults = writingApiService.submitCompleteTest(
                    test.getTask1Question(),
                    submission.getTask1Answer(),
                    test.getTask2Question(),
                    submission.getTask2Answer(),
                    test.getDirectImageUrl());

            if ("error".equals(apiResults.get("status"))) {
                throw new RuntimeException("API Error: " + apiResults.get("message"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> task1Result = (Map<String, Object>) apiResults.get("task1");
            @SuppressWarnings("unchecked")
            Map<String, Object> task2Result = (Map<String, Object>) apiResults.get("task2");

            Double task1Score = extractOverallBand(task1Result);
            Double task2Score = extractOverallBand(task2Result);
            Double overallScore = (Double) apiResults.get("overallScore");

            // ✅ Apply IELTS rounding
            task1Score = IeltsScoreRounder.roundToIeltsBand(task1Score);
            task2Score = IeltsScoreRounder.roundToIeltsBand(task2Score);
            overallScore = IeltsScoreRounder.roundToIeltsBand(overallScore);

            System.out.println("\n--- SCORES TO SAVE (IELTS ROUNDED) ---");
            System.out.println("Task 1 Score: " + task1Score);
            System.out.println("Task 2 Score: " + task2Score);
            System.out.println("Overall Score: " + overallScore);

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

    /*
     * ==================== ✅ SPEAKING SUBMISSION (OPTIMIZED) ====================
     */

    /**
     * ✅ OPTIMIZED: Create Speaking Submission
     * Now stores data in separate TEXT column to avoid truncation
     */
    @Transactional
    public SpeakingSubmission createSpeakingSubmission(
            User user,
            Integer testId,
            List<QuestionQueueItemDTO> questions,
            Map<Integer, String> audioUrls) {

        SpeakingSubmission submission = new SpeakingSubmission();
        submission.setSubmissionUuid(UUID.randomUUID().toString());
        submission.setUser(user);
        submission.setTestId(testId);
        submission.setStatus("pending");

        // ✅ CRITICAL FIX: Store metadata separately to keep audio_url short
        // Just store a simple identifier in audio_url
        submission.setAudioUrl("pending-" + UUID.randomUUID().toString().substring(0, 8));

        // ✅ Store full data in speaking_result (which should be TEXT type)
        Map<String, Object> submissionData = new HashMap<>();
        submissionData.put("questions", questions);
        submissionData.put("audioUrls", audioUrls);
        submission.setSpeakingResult(convertToJson(submissionData));

        SpeakingSubmission saved = speakingSubmissionRepo.save(submission);

        System.out.println("✅ Created SpeakingSubmission: " + saved.getSubmissionUuid());
        System.out.println("   Test ID: " + testId);
        System.out.println("   Questions: " + questions.size());
        System.out.println("   Audio URLs: " + audioUrls.size());

        return saved;
    }

    /**
     * ✅ Process Speaking Submission Asynchronously
     */
    @Transactional
    public void processSpeakingSubmissionAsync(String submissionUuid) {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Small delay to ensure transaction is committed
                processSpeakingSubmission(submissionUuid);
            } catch (Exception e) {
                System.err.println("❌ Async processing error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * ✅ OPTIMIZED: Process Speaking Submission
     * Now reads from speaking_result instead of audio_url
     */
    // Replace the processSpeakingSubmission method with this fixed version:

    @Transactional
    public void processSpeakingSubmission(String submissionUuid) {
        try {
            Optional<SpeakingSubmission> optSubmission = speakingSubmissionRepo
                    .findBySubmissionUuid(submissionUuid);

            if (optSubmission.isEmpty()) {
                System.err.println("❌ Speaking submission not found: " + submissionUuid);
                return;
            }

            SpeakingSubmission submission = optSubmission.get();
            submission.setStatus("processing");
            speakingSubmissionRepo.save(submission);

            System.out.println("========================================");
            System.out.println("🎤 PROCESSING SPEAKING SUBMISSION");
            System.out.println("   UUID: " + submissionUuid);
            System.out.println("========================================");

            // ✅ Extract questions and audio paths from speaking_result
            String dataJson = submission.getSpeakingResult();

            if (dataJson == null || dataJson.isEmpty()) {
                throw new RuntimeException("No submission data found");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> submissionData = objectMapper.readValue(dataJson, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questionsData = (List<Map<String, Object>>) submissionData.get("questions");
            @SuppressWarnings("unchecked")
            Map<String, String> audioFilePathsData = (Map<String, String>) submissionData.get("audioUrls");

            // Convert to proper types
            List<QuestionQueueItemDTO> questions = new ArrayList<>();
            Map<Integer, String> audioFilePaths = new HashMap<>();

            for (Map<String, Object> qData : questionsData) {
                QuestionQueueItemDTO dto = new QuestionQueueItemDTO();
                dto.setQuestionId((Integer) qData.get("questionId"));
                dto.setPartNumber((String) qData.get("partNumber"));
                dto.setQuestionText((String) qData.get("questionText"));
                questions.add(dto);
            }

            for (Map.Entry<String, String> entry : audioFilePathsData.entrySet()) {
                audioFilePaths.put(Integer.parseInt(entry.getKey()), entry.getValue());
            }

            System.out.println(
                    "📋 Reconstructed: " + questions.size() + " questions, " + audioFilePaths.size() + " audio files");

            // ✅ Call Speaking AI API
            System.out.println("📡 Calling Speaking AI API...");
            Map<String, Object> apiResults = speakingApiService.submitSpeakingTest(
                    questions,
                    audioFilePaths);

            if ("error".equals(apiResults.get("status"))) {
                throw new RuntimeException("API Error: " + apiResults.get("message"));
            }

            // ✅ FIX: Handle both String and Map responses from API
            Map<String, Object> resultData;
            if (apiResults.get("result") instanceof String) {
                // API returned a JSON string, parse it
                String resultJson = (String) apiResults.get("result");
                resultData = objectMapper.readValue(resultJson, Map.class);
            } else if (apiResults.get("result") instanceof Map) {
                // API returned a Map directly
                @SuppressWarnings("unchecked")
                Map<String, Object> temp = (Map<String, Object>) apiResults.get("result");
                resultData = temp;
            } else {
                // Use apiResults directly if no "result" key
                resultData = apiResults;
            }

            // ✅ Extract scores from the correct structure
            Double overallScore = extractOverallBand(resultData);

            // Extract individual scores if available
            Double fluency = extractScore(resultData, "fluency");
            Double lexical = extractScore(resultData, "lexical_resource");
            Double grammar = extractScore(resultData, "grammatical_range");
            Double pronunciation = extractScore(resultData, "pronunciation");

            // ✅ Apply IELTS rounding
            overallScore = IeltsScoreRounder.roundToIeltsBand(overallScore);
            if (fluency != null)
                fluency = IeltsScoreRounder.roundToIeltsBand(fluency);
            if (lexical != null)
                lexical = IeltsScoreRounder.roundToIeltsBand(lexical);
            if (grammar != null)
                grammar = IeltsScoreRounder.roundToIeltsBand(grammar);
            if (pronunciation != null)
                pronunciation = IeltsScoreRounder.roundToIeltsBand(pronunciation);

            System.out.println("\n--- SPEAKING SCORES (IELTS ROUNDED) ---");
            System.out.println("Overall Score: " + overallScore);
            System.out.println("Fluency: " + fluency);
            System.out.println("Lexical: " + lexical);
            System.out.println("Grammar: " + grammar);
            System.out.println("Pronunciation: " + pronunciation);

            // ✅ Update audio_url with summary
            submission.setAudioUrl("completed-" + questions.size() + "-questions");

            // ✅ Save full results
            submission.setSpeakingResult(convertToJson(resultData));
            submission.setSpeakingScore(overallScore);
            submission.setOverallScore(overallScore);
            submission.setStatus("completed");
            submission.setCompletedAt(java.time.LocalDateTime.now());
            speakingSubmissionRepo.save(submission);

            // ✅ Save detailed feedback in SpeakingSubmissionDetail
            SpeakingSubmissionDetail detail = new SpeakingSubmissionDetail();
            detail.setSubmission(submission);
            detail.setFluency(fluency);
            detail.setLexicalResource(lexical);
            detail.setGrammaticalRange(grammar);
            detail.setPronunciation(pronunciation);

            // Extract feedback texts
            detail.setFeedback(extractString(resultData, "feedback"));
            detail.setStrengths(extractString(resultData, "strengths"));
            detail.setImprovements(extractString(resultData, "improvements"));

            // Store part-specific results
            detail.setPart1Result(convertToJson(resultData.get("part1_result")));
            detail.setPart2Result(convertToJson(resultData.get("part2_result")));
            detail.setPart3Result(convertToJson(resultData.get("part3_result")));

            speakingDetailRepo.save(detail);

            System.out.println("✅ SPEAKING SUBMISSION COMPLETED & SAVED");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("❌ ERROR PROCESSING SPEAKING SUBMISSION: " + submissionUuid);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();

            try {
                Optional<SpeakingSubmission> optSubmission = speakingSubmissionRepo
                        .findBySubmissionUuid(submissionUuid);
                if (optSubmission.isPresent()) {
                    SpeakingSubmission submission = optSubmission.get();
                    submission.setStatus("failed");
                    submission.setErrorMessage(e.getMessage());
                    submission.setCompletedAt(java.time.LocalDateTime.now());
                    speakingSubmissionRepo.save(submission);
                }
            } catch (Exception ex) {
                System.err.println("❌ Failed to update status: " + ex.getMessage());
            }
        }
    }

    /* ==================== SHARED METHODS ==================== */

    private Double extractOverallBand(Map<String, Object> apiResponse) {
        if (apiResponse == null)
            return null;

        try {
            Object overallBand = apiResponse.get("overall_band");
            if (overallBand != null) {
                if (overallBand instanceof Number) {
                    return ((Number) overallBand).doubleValue();
                }
                if (overallBand instanceof String) {
                    return Double.parseDouble((String) overallBand);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting score: " + e.getMessage());
        }
        return null;
    }

    private Double extractScore(Map<String, Object> response, String key) {
        try {
            Object value = response.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private String extractString(Map<String, Object> response, String key) {
        try {
            Object value = response.get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

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
        for (SpeakingSubmission ss : speakingSubmissions) {
            speakingTestRepo.findById(ss.getTestId()).ifPresent(ss::setTest);
        }
        allSubmissions.addAll(speakingSubmissions);

        allSubmissions.sort(Comparator.comparing(
                ITestSubmission::getSubmittedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return allSubmissions;
    }

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

        List<SpeakingSubmission> speakingPending = speakingSubmissionRepo
                .findByUserIdAndStatus(user.getId(), "pending");
        for (SpeakingSubmission ss : speakingPending) {
            speakingTestRepo.findById(ss.getTestId()).ifPresent(ss::setTest);
        }
        pending.addAll(speakingPending);

        List<SpeakingSubmission> speakingProcessing = speakingSubmissionRepo
                .findByUserIdAndStatus(user.getId(), "processing");
        for (SpeakingSubmission ss : speakingProcessing) {
            speakingTestRepo.findById(ss.getTestId()).ifPresent(ss::setTest);
        }
        pending.addAll(speakingProcessing);

        return pending;
    }

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
            overallAvg = overallAvg / count;
            overallAvg = IeltsScoreRounder.roundToIeltsBand(overallAvg);
        }

        stats.put("averageScore", count > 0 ? overallAvg : 0.0);

        return stats;
    }

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
        if (speakingOpt.isPresent()) {
            SpeakingSubmission ss = speakingOpt.get();
            speakingTestRepo.findById(ss.getTestId()).ifPresent(ss::setTest);
            return Optional.of(ss);
        }

        return Optional.empty();
    }

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

    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            System.err.println("Error converting to JSON: " + e.getMessage());
            return "{}";
        }
    }
}