package com.ieltsgrading.ielts_evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.speaking.QuestionQueueItemDTO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

/**
 * ✅ FIXED: Service với retry logic, timeouts tối ưu và chunked processing
 */
@Service
public class SpeakingApiService {

    private static final String BASE_URL = "https://zoogleal-parsonish-almeda.ngrok-free.dev";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // ✅ Retry settings
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;
    private static final long MAX_FILE_SIZE_MB = 10; // Max size per file

    public SpeakingApiService() {
        // ✅ Configure RestTemplate với timeouts dài hơn
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);  // 30 giây connect
        factory.setReadTimeout(300000);    // 5 phút read (cho file lớn)
        
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * ✅ Submit Speaking Test với retry logic
     */
    public Map<String, Object> submitSpeakingTest(
            List<QuestionQueueItemDTO> questions,
            Map<Integer, String> audioFilePaths) {

        try {
            System.out.println("========================================");
            System.out.println("🎤 CALLING SPEAKING API");
            System.out.println("   Total questions: " + questions.size());
            System.out.println("========================================");

            // ✅ Validate files trước khi gửi
            validateAudioFiles(audioFilePaths);

            // Separate questions by part
            List<QuestionQueueItemDTO> part1Questions = new ArrayList<>();
            List<QuestionQueueItemDTO> part2Questions = new ArrayList<>();
            List<QuestionQueueItemDTO> part3Questions = new ArrayList<>();

            for (QuestionQueueItemDTO q : questions) {
                if (q.getPartNumber().equals("Part 1")) {
                    part1Questions.add(q);
                } else if (q.getPartNumber().equals("Part 2")) {
                    part2Questions.add(q);
                } else if (q.getPartNumber().equals("Part 3")) {
                    part3Questions.add(q);
                }
            }

            Map<String, Object> finalResult = new HashMap<>();
            List<Map<String, Object>> allResults = new ArrayList<>();

            // Process Part 1
            if (!part1Questions.isEmpty()) {
                System.out.println("📞 Calling API for Part 1 (" + part1Questions.size() + " questions)");
                Map<String, Object> part1Result = callMultiPartApiWithRetry(
                    part1Questions, 
                    audioFilePaths, 
                    "Part 1"
                );
                if (!"error".equals(part1Result.get("status"))) {
                    allResults.add(part1Result);
                    finalResult.put("part1_result", part1Result);
                }
            }

            // Process Part 2
            if (!part2Questions.isEmpty()) {
                System.out.println("📞 Calling API for Part 2");
                Map<String, Object> part2Result = callPart2ApiWithRetry(
                    part2Questions.get(0), 
                    audioFilePaths
                );
                if (!"error".equals(part2Result.get("status"))) {
                    allResults.add(part2Result);
                    finalResult.put("part2_result", part2Result);
                }
            }

            // Process Part 3
            if (!part3Questions.isEmpty()) {
                System.out.println("📞 Calling API for Part 3 (" + part3Questions.size() + " questions)");
                Map<String, Object> part3Result = callMultiPartApiWithRetry(
                    part3Questions, 
                    audioFilePaths, 
                    "Part 3"
                );
                if (!"error".equals(part3Result.get("status"))) {
                    allResults.add(part3Result);
                    finalResult.put("part3_result", part3Result);
                }
            }

            // ✅ Check if có ít nhất 1 part thành công
            if (allResults.isEmpty()) {
                throw new RuntimeException("All API calls failed. Check network and API availability.");
            }

            // Calculate overall score (average of all parts)
            Double overallScore = calculateOverallScore(allResults);

            // Extract individual scores from first available result
            Map<String, Object> firstResult = allResults.get(0);
            
            finalResult.put("overall_band", overallScore);
            finalResult.put("fluency", extractScore(firstResult, "fluency_and_coherence"));
            finalResult.put("lexical_resource", extractScore(firstResult, "lexical_resource"));
            finalResult.put("grammatical_range", extractScore(firstResult, "grammatical_range_accuracy"));
            finalResult.put("pronunciation", extractScore(firstResult, "pronunciation"));
            
            // Extract feedback texts
            finalResult.put("feedback", extractFeedback(firstResult, "fluency_and_coherence"));
            finalResult.put("strengths", extractStrengths(allResults));
            finalResult.put("improvements", extractImprovements(allResults));
            
            finalResult.put("status", "success");

            System.out.println("✅ API Call completed successfully");
            System.out.println("   Overall Band: " + overallScore);

            return finalResult;

        } catch (Exception e) {
            System.err.println("❌ Error submitting speaking test: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("message", e.getMessage());
            return errorResult;
        }
    }

    /**
     * ✅ Validate audio files trước khi upload
     */
    private void validateAudioFiles(Map<Integer, String> audioFilePaths) throws Exception {
        long totalSize = 0;
        
        for (Map.Entry<Integer, String> entry : audioFilePaths.entrySet()) {
            String cleanPath = cleanFilePath(entry.getValue());
            File file = Paths.get(cleanPath).toFile();
            
            if (!file.exists()) {
                throw new RuntimeException("Audio file not found: " + file.getAbsolutePath());
            }
            
            long fileSize = file.length();
            long fileSizeMB = fileSize / (1024 * 1024);
            
            if (fileSizeMB > MAX_FILE_SIZE_MB) {
                System.out.println("⚠️ Warning: Large file detected - Question " + entry.getKey() + 
                                 ": " + fileSizeMB + " MB");
            }
            
            totalSize += fileSize;
        }
        
        long totalSizeMB = totalSize / (1024 * 1024);
        System.out.println("📦 Total upload size: " + totalSizeMB + " MB");
        
        if (totalSizeMB > 50) {
            System.out.println("⚠️ WARNING: Very large upload (" + totalSizeMB + " MB). May take 3-5 minutes...");
        }
    }

    /**
     * ✅ Call Part 2 API với retry logic
     */
    private Map<String, Object> callPart2ApiWithRetry(
            QuestionQueueItemDTO question,
            Map<Integer, String> audioFilePaths) {
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("   🔄 Part 2 - Attempt " + attempt + " of " + MAX_RETRIES);
                return callPart2Api(question, audioFilePaths);
                
            } catch (ResourceAccessException e) {
                System.err.println("   ⚠️ Network error on attempt " + attempt + ": " + e.getMessage());
                if (attempt == MAX_RETRIES) {
                    return createErrorResponse("Part 2 failed after " + MAX_RETRIES + " attempts: Network timeout");
                }
                sleep(RETRY_DELAY_MS * attempt);
                
            } catch (Exception e) {
                System.err.println("   ⚠️ Error on attempt " + attempt + ": " + e.getMessage());
                if (attempt == MAX_RETRIES) {
                    return createErrorResponse("Part 2 failed: " + e.getMessage());
                }
                sleep(RETRY_DELAY_MS);
            }
        }
        
        return createErrorResponse("Part 2 failed after all retries");
    }

    /**
     * ✅ Call Multi Part API với retry logic
     */
    private Map<String, Object> callMultiPartApiWithRetry(
            List<QuestionQueueItemDTO> questions,
            Map<Integer, String> audioFilePaths,
            String partLabel) {
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("   🔄 " + partLabel + " - Attempt " + attempt + " of " + MAX_RETRIES);
                return callMultiPartApi(questions, audioFilePaths, partLabel);
                
            } catch (ResourceAccessException e) {
                System.err.println("   ⚠️ Network error on attempt " + attempt + ": " + e.getMessage());
                if (attempt == MAX_RETRIES) {
                    return createErrorResponse(partLabel + " failed after " + MAX_RETRIES + " attempts: Network timeout");
                }
                sleep(RETRY_DELAY_MS * attempt);
                
            } catch (Exception e) {
                System.err.println("   ⚠️ Error on attempt " + attempt + ": " + e.getMessage());
                if (attempt == MAX_RETRIES) {
                    return createErrorResponse(partLabel + " failed: " + e.getMessage());
                }
                sleep(RETRY_DELAY_MS);
            }
        }
        
        return createErrorResponse(partLabel + " failed after all retries");
    }

    /**
     * ✅ Call /speaking/2 API for Part 2
     */
    private Map<String, Object> callPart2Api(
            QuestionQueueItemDTO question,
            Map<Integer, String> audioFilePaths) throws Exception {

        String url = BASE_URL + "/speaking/multi";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("speaking_task", question.getQuestionText());
        
        String audioFilePath = audioFilePaths.get(question.getQuestionId());
        
        if (audioFilePath == null) {
            throw new RuntimeException("No audio file for question: " + question.getQuestionId());
        }
        
        audioFilePath = cleanFilePath(audioFilePath);
        File audioFile = Paths.get(audioFilePath).toFile();
        
        if (!audioFile.exists()) {
            throw new RuntimeException("Audio file not found: " + audioFile.getAbsolutePath());
        }
        
        FileSystemResource fileResource = new FileSystemResource(audioFile);
        body.add("file", fileResource);
        
        System.out.println("   📎 Uploading: " + audioFile.getName() + 
                         " (" + (audioFile.length() / 1024) + " KB)");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        System.out.println("   ⏳ Uploading and processing...");
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        System.out.println("   ✅ Part 2 Response: " + response.getStatusCode());
        
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) responseMap.get("message");
        
        return message != null ? message : responseMap;
    }

    /**
     * ✅ Call /speaking/multi API for Part 1 & 3
     */
    private Map<String, Object> callMultiPartApi(
            List<QuestionQueueItemDTO> questions,
            Map<Integer, String> audioFilePaths,
            String partLabel) throws Exception {

        String url = BASE_URL + "/speaking/multi";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        List<String> questionTexts = new ArrayList<>();
        for (QuestionQueueItemDTO q : questions) {
            questionTexts.add(q.getQuestionText());
        }
        body.add("questions", objectMapper.writeValueAsString(questionTexts));
        
        if ("Part 1".equals(partLabel)) {
            body.add("topic", "Personal Information");
        } else {
            body.add("topic", "");
        }

        int fileCount = 0;
        long totalSize = 0;
        
        for (QuestionQueueItemDTO q : questions) {
            String audioFilePath = audioFilePaths.get(q.getQuestionId());
            
            if (audioFilePath == null) {
                throw new RuntimeException("No audio file for question: " + q.getQuestionId());
            }
            
            audioFilePath = cleanFilePath(audioFilePath);
            File audioFile = Paths.get(audioFilePath).toFile();
            
            if (!audioFile.exists()) {
                throw new RuntimeException("Audio file not found: " + audioFile.getAbsolutePath());
            }
            
            FileSystemResource fileResource = new FileSystemResource(audioFile);
            body.add("files", fileResource);
            fileCount++;
            totalSize += audioFile.length();
            
            System.out.println("   📎 File " + fileCount + ": " + audioFile.getName() + 
                             " (" + (audioFile.length() / 1024) + " KB)");
        }

        System.out.println("   📦 Total: " + fileCount + " files, " + 
                         (totalSize / 1024) + " KB");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        System.out.println("   ⏳ Uploading and processing...");
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        System.out.println("   ✅ " + partLabel + " Response: " + response.getStatusCode());
        
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) responseMap.get("message");
        
        return message != null ? message : responseMap;
    }

    /**
     * ✅ Clean file path
     */
    private String cleanFilePath(String filePath) {
        if (filePath == null) return null;
        
        if (filePath.startsWith("file:///")) {
            filePath = filePath.substring(8);
        } else if (filePath.startsWith("file:/")) {
            filePath = filePath.substring(6);
        }
        
        if (filePath.matches("^/[A-Z]:/.*")) {
            filePath = filePath.substring(1);
        }
        
        return filePath;
    }

    /**
     * ✅ Sleep helper
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * ✅ Create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        return error;
    }

    private Double calculateOverallScore(List<Map<String, Object>> results) {
        if (results.isEmpty()) return null;

        double total = 0.0;
        int count = 0;

        for (Map<String, Object> result : results) {
            Object overallBand = result.get("overall_band");
            if (overallBand != null) {
                if (overallBand instanceof Number) {
                    total += ((Number) overallBand).doubleValue();
                    count++;
                } else if (overallBand instanceof String) {
                    try {
                        total += Double.parseDouble((String) overallBand);
                        count++;
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ Could not parse overall_band: " + overallBand);
                    }
                }
            }
        }

        return count > 0 ? Math.round((total / count) * 10.0) / 10.0 : null;
    }

    private Double extractScore(Map<String, Object> result, String criteriaKey) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> criteria = (Map<String, Object>) result.get(criteriaKey);
            
            if (criteria != null) {
                Object band = criteria.get("band");
                if (band instanceof Number) {
                    return ((Number) band).doubleValue();
                } else if (band instanceof String) {
                    return Double.parseDouble((String) band);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private String extractFeedback(Map<String, Object> result, String criteriaKey) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> criteria = (Map<String, Object>) result.get(criteriaKey);
            
            if (criteria != null) {
                Object assessment = criteria.get("assessment");
                if (assessment != null) {
                    return assessment.toString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private String extractStrengths(List<Map<String, Object>> results) {
        StringBuilder strengths = new StringBuilder();
        
        for (Map<String, Object> result : results) {
            String feedback = extractFeedback(result, "fluency_and_coherence");
            if (feedback != null && feedback.contains("good")) {
                strengths.append(feedback.substring(0, Math.min(200, feedback.length())));
                strengths.append("\n\n");
            }
        }
        
        return strengths.length() > 0 ? strengths.toString() : "Continue your good work!";
    }

    private String extractImprovements(List<Map<String, Object>> results) {
        StringBuilder improvements = new StringBuilder();
        
        for (Map<String, Object> result : results) {
            String feedback = extractFeedback(result, "lexical_resource");
            if (feedback != null && (feedback.contains("improve") || feedback.contains("could"))) {
                improvements.append(feedback.substring(0, Math.min(200, feedback.length())));
                improvements.append("\n\n");
            }
        }
        
        return improvements.length() > 0 ? improvements.toString() : "Keep practicing to improve further.";
    }
}