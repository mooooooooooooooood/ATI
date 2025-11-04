package com.ieltsgrading.ielts_evaluator.service;

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
 * Service to call external Writing API
 */
@Service
public class WritingApiService {
    
    private static final String API_BASE_URL = "https://zoogleal-parsonish-almeda.ngrok-free.dev";
    private final RestTemplate restTemplate;
    
    public WritingApiService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Submit Writing Task 1 with image file
     */
    public Map<String, Object> submitTask1WithFile(
            String writingTask, 
            String writingInput, 
            MultipartFile file) throws IOException {
        
        String url = API_BASE_URL + "/writing/1/file";
        
        System.out.println("=== Submitting Task 1 with File ===");
        System.out.println("URL: " + url);
        System.out.println("Task length: " + writingTask.length());
        System.out.println("Input length: " + writingInput.length());
        System.out.println("File: " + file.getOriginalFilename());
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");
        
        // Create multipart form data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("writing_task", writingTask);
        body.add("writing_input", writingInput);
        
        // Add file
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", fileResource);
        
        // Create request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        // Make request
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            System.out.println("Task 1 File Response: " + response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            System.err.println("❌ Task 1 File Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit Task 1 with file: " + e.getMessage(), e);
        }
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
        System.out.println("Task: " + writingTask.substring(0, Math.min(50, writingTask.length())) + "...");
        System.out.println("Input length: " + writingInput.length());
        System.out.println("Image link: " + imageLink);
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");
        
        // Create multipart form data
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("writing_task", writingTask);
        body.add("writing_input", writingInput);
        body.add("link", imageLink);
        
        // Create request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        // Make request
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            System.out.println("✅ Task 1 Link Response: " + response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            System.err.println("❌ Task 1 Link Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit Task 1 with link: " + e.getMessage(), e);
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
        System.out.println("Task: " + writingTask.substring(0, Math.min(50, writingTask.length())) + "...");
        System.out.println("Input length: " + writingInput.length() + " characters");
        System.out.println("Input word count: ~" + writingInput.split("\\s+").length + " words");
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");
        
        // Create multipart form data
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("writing_task", writingTask);
        body.add("writing_input", writingInput);
        
        System.out.println("Request body created:");
        System.out.println("  - writing_task: " + (writingTask != null ? "✓" : "✗"));
        System.out.println("  - writing_input: " + (writingInput != null ? "✓" : "✗"));
        
        // Create request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        System.out.println("Headers: " + headers);
        
        // Make request
        try {
            System.out.println("Sending POST request to: " + url);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            System.out.println("✅ Task 2 Response Status: " + response.getStatusCode());
            System.out.println("✅ Task 2 Response Body: " + response.getBody());
            
            return response.getBody();
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ HTTP Client Error (4xx):");
            System.err.println("   Status: " + e.getStatusCode());
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Response Body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to submit Task 2 (HTTP " + e.getStatusCode() + "): " + e.getMessage(), e);
            
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            System.err.println("❌ HTTP Server Error (5xx):");
            System.err.println("   Status: " + e.getStatusCode());
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Response Body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to submit Task 2 (HTTP " + e.getStatusCode() + "): " + e.getMessage(), e);
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            System.err.println("❌ Connection Error:");
            System.err.println("   Cannot reach API at: " + url);
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Possible causes:");
            System.err.println("   - ngrok tunnel expired");
            System.err.println("   - API server is down");
            System.err.println("   - Network connection issue");
            throw new RuntimeException("Cannot connect to API server. Please check if ngrok tunnel is active.", e);
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected Error:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit Task 2: " + e.getMessage(), e);
        }
    }
    
    /**
     * Submit complete writing test (both tasks)
     * Returns combined results
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
                System.out.println("Using Task 1 with image link");
                task1Result = submitTask1WithLink(task1Question, task1Answer, task1ImageUrl);
            } else {
                System.out.println("⚠️ No image URL provided for Task 1, using Task 2 endpoint as fallback");
                task1Result = submitTask2(task1Question, task1Answer);
            }
            results.put("task1", task1Result);
            System.out.println("✅ Task 1 completed");
            
            // Submit Task 2
            System.out.println("\n--- TASK 2 ---");
            Map<String, Object> task2Result = submitTask2(task2Question, task2Answer);
            results.put("task2", task2Result);
            System.out.println("✅ Task 2 completed");
            
            // Extract scores and calculate overall
            Double task1Score = extractScore(task1Result.get("message"));
            Double task2Score = extractScore(task2Result.get("message"));
            
            System.out.println("\n--- SCORES ---");
            System.out.println("Task 1 Score: " + (task1Score != null ? task1Score : "N/A"));
            System.out.println("Task 2 Score: " + (task2Score != null ? task2Score : "N/A"));
            
            if (task1Score != null && task2Score != null) {
                // Task 1 = 33%, Task 2 = 67%
                double overall = (task1Score / 3.0) + (task2Score * 2.0 / 3.0);
                overall = Math.round(overall * 2.0) / 2.0; // Round to nearest 0.5
                results.put("overallScore", overall);
                System.out.println("Overall Score: " + overall);
            } else {
                System.out.println("⚠️ Cannot calculate overall score - missing band scores");
            }
            
            results.put("status", "success");
            System.out.println("\n✅ COMPLETE TEST SUBMISSION SUCCESSFUL");
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ COMPLETE TEST SUBMISSION FAILED");
            System.err.println("Error: " + e.getMessage());
            System.err.println("========================================\n");
            
            results.put("status", "error");
            results.put("message", e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Extract band score from API response message
     * Example: "Predicted score: Band 7.0" -> 7.0
     * Example: "<h2>Predicted score: Band 7.0</h2>" -> 7.0
     */
    private Double extractScore(Object message) {
        if (message == null) return null;
        
        String messageStr = message.toString();
        
        // Remove HTML tags
        messageStr = messageStr.replaceAll("<[^>]*>", "");
        
        // Try to extract "Band X.X" pattern
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Band\\s+(\\d+\\.?\\d*)");
        java.util.regex.Matcher matcher = pattern.matcher(messageStr);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                System.err.println("⚠️ Could not parse band score: " + matcher.group(1));
                return null;
            }
        }
        
        System.err.println("⚠️ No band score found in message: " + messageStr);
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
            System.err.println("Please check:");
            System.err.println("1. Is ngrok tunnel running?");
            System.err.println("2. Is the Python API server running?");
            System.err.println("3. Is the ngrok URL correct?");
            return false;
        }
    }
}