package com.ieltsgrading.ielts_evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.speaking.QuestionQueueItemDTO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

/**
 * ✅ UPDATED: Service to call Speaking AI API with ACTUAL FILES
 */
@Service
public class SpeakingApiService {

    private static final String BASE_URL = "https://zoogleal-parsonish-almeda.ngrok-free.dev";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ✅ Submit Speaking Test for Grading
     * Now accepts file PATHS instead of URLs
     */
    public Map<String, Object> submitSpeakingTest(
            List<QuestionQueueItemDTO> questions,
            Map<Integer, String> audioFilePaths) {

        try {
            System.out.println("========================================");
            System.out.println("🎤 CALLING SPEAKING API");
            System.out.println("   Total questions: " + questions.size());
            System.out.println("========================================");

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
                Map<String, Object> part1Result = callMultiPartApi(
                    part1Questions, 
                    audioFilePaths, 
                    "Part 1"
                );
                allResults.add(part1Result);
                finalResult.put("part1_result", part1Result);
            }

            // Process Part 2
            if (!part2Questions.isEmpty()) {
                System.out.println("📞 Calling API for Part 2");
                Map<String, Object> part2Result = callPart2Api(
                    part2Questions.get(0), 
                    audioFilePaths
                );
                allResults.add(part2Result);
                finalResult.put("part2_result", part2Result);
            }

            // Process Part 3
            if (!part3Questions.isEmpty()) {
                System.out.println("📞 Calling API for Part 3 (" + part3Questions.size() + " questions)");
                Map<String, Object> part3Result = callMultiPartApi(
                    part3Questions, 
                    audioFilePaths, 
                    "Part 3"
                );
                allResults.add(part3Result);
                finalResult.put("part3_result", part3Result);
            }

            // Calculate overall score (average of all parts)
            Double overallScore = calculateOverallScore(allResults);

            // Extract individual scores from first available result
            Map<String, Object> firstResult = allResults.isEmpty() ? new HashMap<>() : allResults.get(0);
            
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
     * ✅ Call /speaking/2 API for Part 2
     * Now uses ACTUAL FILE instead of URL
     */
    private Map<String, Object> callPart2Api(
            QuestionQueueItemDTO question,
            Map<Integer, String> audioFilePaths) throws Exception {

        String url = BASE_URL + "/speaking/2";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("speaking_task", question.getQuestionText());
        
        // ✅ Get audio FILE PATH (not URL)
        String audioFilePath = audioFilePaths.get(question.getQuestionId());
        
        if (audioFilePath == null) {
            throw new RuntimeException("No audio file for question: " + question.getQuestionId());
        }
        
        // ✅ Load actual file and add to request
        File audioFile = new File(audioFilePath);
        
        if (!audioFile.exists()) {
            throw new RuntimeException("Audio file not found: " + audioFilePath);
        }
        
        FileSystemResource fileResource = new FileSystemResource(audioFile);
        body.add("file", fileResource);
        
        System.out.println("   📎 Uploading file: " + audioFile.getName() + " (" + audioFile.length() + " bytes)");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        System.out.println("   ✅ Part 2 API Response: " + response.getStatusCode());
        
        // Parse response
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) responseMap.get("message");
        
        return message != null ? message : responseMap;
    }

    /**
     * ✅ Call /speaking/multi API for Part 1 & 3
     * Now uses ACTUAL FILES instead of URLs
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
        
        // Prepare questions array
        List<String> questionTexts = new ArrayList<>();
        for (QuestionQueueItemDTO q : questions) {
            questionTexts.add(q.getQuestionText());
        }
        body.add("questions", objectMapper.writeValueAsString(questionTexts));
        
        // Add topic for Part 1, empty for Part 3
        if ("Part 1".equals(partLabel)) {
            // You might want to get actual topic from test details
            body.add("topic", "Personal Information");
        } else {
            body.add("topic", ""); // Empty for Part 3
        }

        // ✅ Add ACTUAL AUDIO FILES (not URLs)
        int fileCount = 0;
        for (QuestionQueueItemDTO q : questions) {
            String audioFilePath = audioFilePaths.get(q.getQuestionId());
            
            if (audioFilePath == null) {
                throw new RuntimeException("No audio file for question: " + q.getQuestionId());
            }
            
            File audioFile = new File(audioFilePath);
            
            if (!audioFile.exists()) {
                throw new RuntimeException("Audio file not found: " + audioFilePath);
            }
            
            FileSystemResource fileResource = new FileSystemResource(audioFile);
            body.add("files", fileResource);
            fileCount++;
            
            System.out.println("   📎 Adding file " + fileCount + ": " + audioFile.getName() + " (" + audioFile.length() + " bytes)");
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        System.out.println("   ✅ " + partLabel + " API Response: " + response.getStatusCode());
        
        // Parse response
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) responseMap.get("message");
        
        return message != null ? message : responseMap;
    }

    /**
     * Calculate overall score from multiple parts
     */
    private Double calculateOverallScore(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            return null;
        }

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

        if (count == 0) {
            return null;
        }

        // Round to 1 decimal place
        return Math.round((total / count) * 10.0) / 10.0;
    }

    /**
     * Extract individual score from result
     */
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
            System.err.println("⚠️ Could not extract score for: " + criteriaKey);
        }
        return null;
    }

    /**
     * Extract feedback text from criteria
     */
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

    /**
     * Extract strengths from all results
     */
    private String extractStrengths(List<Map<String, Object>> results) {
        StringBuilder strengths = new StringBuilder();
        
        for (Map<String, Object> result : results) {
            // Extract positive points from assessments
            String feedback = extractFeedback(result, "fluency_and_coherence");
            if (feedback != null && feedback.contains("good")) {
                strengths.append(feedback.substring(0, Math.min(200, feedback.length())));
                strengths.append("\n\n");
            }
        }
        
        return strengths.length() > 0 ? strengths.toString() : "Continue your good work!";
    }

    /**
     * Extract areas for improvement from all results
     */
    private String extractImprovements(List<Map<String, Object>> results) {
        StringBuilder improvements = new StringBuilder();
        
        for (Map<String, Object> result : results) {
            // Extract improvement suggestions from assessments
            String feedback = extractFeedback(result, "lexical_resource");
            if (feedback != null && (feedback.contains("improve") || feedback.contains("could"))) {
                improvements.append(feedback.substring(0, Math.min(200, feedback.length())));
                improvements.append("\n\n");
            }
        }
        
        return improvements.length() > 0 ? improvements.toString() : "Keep practicing to improve further.";
    }
}