package com.ieltsgrading.ielts_evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to call external Writing API - FIXED VERSION
 */
@Service
public class WritingApiService {
    
    private static final String API_BASE_URL = "https://zoogleal-parsonish-almeda.ngrok-free.dev";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public WritingApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Submit Writing Task 1 with image link
     */
    public Map<String, Object> submitTask1WithLink(
            String writingTask, 
            String writingInput, 
            String imageLink) {
        
        String url = API_BASE_URL + "/writing/1/link";
        
        System.out.println("=== Submitting Task 1 with Link ===");
        System.out.println("URL: " + url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("writing_task", writingTask);
        body.add("writing_input", writingInput);
        body.add("link", imageLink);
        
        HttpEntity<MultiValueMap<String, String>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            Map<String, Object> apiResponse = response.getBody();
            System.out.println("✅ Task 1 API Response: " + apiResponse);
            
            // ✅ FIX: Parse nested JSON structure
            return parseApiResponse(apiResponse);
            
        } catch (Exception e) {
            System.err.println("❌ Task 1 Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit Task 1: " + e.getMessage(), e);
        }
    }
    
    /**
     * Submit Writing Task 2 (essay)
     */
    public Map<String, Object> submitTask2(
            String writingTask, 
            String writingInput) {
        
        String url = API_BASE_URL + "/writing/2";
        
        System.out.println("=== Submitting Task 2 ===");
        System.out.println("URL: " + url);
        System.out.println("Input length: " + writingInput.length() + " chars");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("writing_task", writingTask);
        body.add("writing_input", writingInput);
        
        HttpEntity<MultiValueMap<String, String>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            Map<String, Object> apiResponse = response.getBody();
            System.out.println("✅ Task 2 API Response received");
            
            // ✅ FIX: Parse nested JSON structure
            return parseApiResponse(apiResponse);
            
        } catch (Exception e) {
            System.err.println("❌ Task 2 Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit Task 2: " + e.getMessage(), e);
        }
    }
    
    /**
     * ✅ NEW: Parse API response with correct structure
     * API returns: { "message": { "overall_band": 5.5, "task_achievement": {...}, ... } }
     */
    private Map<String, Object> parseApiResponse(Map<String, Object> apiResponse) {
        if (apiResponse == null) {
            System.err.println("❌ API response is null");
            return new HashMap<>();
        }
        
        try {
            // Get the "message" field
            Object messageObj = apiResponse.get("message");
            
            if (messageObj == null) {
                System.err.println("❌ No 'message' field in API response");
                return apiResponse; // Return as-is if no message field
            }
            
            // If message is already a Map, return it directly
            if (messageObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageMap = (Map<String, Object>) messageObj;
                
                System.out.println("✅ Parsed message as Map");
                System.out.println("   overall_band: " + messageMap.get("overall_band"));
                
                return messageMap;
            }
            
            // If message is a String (JSON string), parse it
            if (messageObj instanceof String) {
                String messageStr = (String) messageObj;
                
                // Check if it's HTML response (old API format)
                if (messageStr.contains("<h2>") || messageStr.startsWith("Band")) {
                    System.out.println("⚠️ Detected old HTML format response");
                    Double score = extractScoreFromHtml(messageStr);
                    Map<String, Object> result = new HashMap<>();
                    result.put("overall_band", score);
                    result.put("message", messageStr);
                    return result;
                }
                
                // Try to parse as JSON
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsed = objectMapper.readValue(messageStr, Map.class);
                    System.out.println("✅ Parsed message string as JSON");
                    System.out.println("   overall_band: " + parsed.get("overall_band"));
                    return parsed;
                } catch (Exception e) {
                    System.err.println("⚠️ Could not parse message as JSON: " + e.getMessage());
                    Map<String, Object> result = new HashMap<>();
                    result.put("message", messageStr);
                    return result;
                }
            }
            
            System.err.println("⚠️ Unknown message type: " + messageObj.getClass().getName());
            return apiResponse;
            
        } catch (Exception e) {
            System.err.println("❌ Error parsing API response: " + e.getMessage());
            e.printStackTrace();
            return apiResponse;
        }
    }
    
    /**
     * Extract score from HTML response (fallback for old API format)
     */
    private Double extractScoreFromHtml(String html) {
        if (html == null) return null;
        
        String cleaned = html.replaceAll("<[^>]*>", "");
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Band\\s+(\\d+\\.?\\d*)");
        java.util.regex.Matcher matcher = pattern.matcher(cleaned);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                System.err.println("⚠️ Could not parse band score: " + matcher.group(1));
            }
        }
        
        return null;
    }
    
    /**
     * Submit complete writing test (both tasks)
     */
    public Map<String, Object> submitCompleteTest(
            String task1Question,
            String task1Answer,
            String task2Question,
            String task2Answer,
            String task1ImageUrl) {
        
        System.out.println("\n========================================");
        System.out.println("SUBMITTING COMPLETE WRITING TEST");
        System.out.println("========================================");
        
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Submit Task 1
            System.out.println("\n--- TASK 1 ---");
            Map<String, Object> task1Result;
            if (task1ImageUrl != null && !task1ImageUrl.isEmpty()) {
                task1Result = submitTask1WithLink(task1Question, task1Answer, task1ImageUrl);
            } else {
                System.out.println("⚠️ No image URL for Task 1, using Task 2 endpoint");
                task1Result = submitTask2(task1Question, task1Answer);
            }
            results.put("task1", task1Result);
            
            // Submit Task 2
            System.out.println("\n--- TASK 2 ---");
            Map<String, Object> task2Result = submitTask2(task2Question, task2Answer);
            results.put("task2", task2Result);
            
            // ✅ FIX: Extract scores from correct nested structure
            Double task1Score = extractOverallBand(task1Result);
            Double task2Score = extractOverallBand(task2Result);
            
            System.out.println("\n--- SCORES EXTRACTED ---");
            System.out.println("Task 1 Score: " + (task1Score != null ? task1Score : "N/A"));
            System.out.println("Task 2 Score: " + (task2Score != null ? task2Score : "N/A"));
            
            // Calculate overall score
            if (task1Score != null && task2Score != null) {
                // Task 1 = 33%, Task 2 = 67%
                double overall = (task1Score / 3.0) + (task2Score * 2.0 / 3.0);
                overall = Math.round(overall * 2.0) / 2.0; // Round to nearest 0.5
                results.put("overallScore", overall);
                System.out.println("Overall Score: " + overall);
            } else {
                System.err.println("⚠️ Cannot calculate overall - missing scores");
            }
            
            results.put("status", "success");
            System.out.println("\n✅ COMPLETE TEST SUBMISSION SUCCESSFUL");
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ COMPLETE TEST SUBMISSION FAILED");
            System.err.println("Error: " + e.getMessage());
            System.err.println("========================================\n");
            e.printStackTrace();
            
            results.put("status", "error");
            results.put("message", e.getMessage());
        }
        
        return results;
    }
    
    /**
     * ✅ NEW: Extract overall_band from parsed response
     */
    private Double extractOverallBand(Map<String, Object> parsedResponse) {
        if (parsedResponse == null) return null;
        
        try {
            // Try to get overall_band directly
            Object overallBand = parsedResponse.get("overall_band");
            if (overallBand != null) {
                if (overallBand instanceof Number) {
                    return ((Number) overallBand).doubleValue();
                }
                if (overallBand instanceof String) {
                    return Double.parseDouble((String) overallBand);
                }
            }
            
            System.err.println("⚠️ No overall_band found in response");
            System.err.println("   Available keys: " + parsedResponse.keySet());
            
        } catch (Exception e) {
            System.err.println("❌ Error extracting overall_band: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Test API connection
     */
    public boolean testConnection() {
        System.out.println("Testing API connection to: " + API_BASE_URL);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                API_BASE_URL,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            System.out.println("✅ API connection successful - Status: " + response.getStatusCode());
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ API connection failed: " + e.getMessage());
            return false;
        }
    }
}