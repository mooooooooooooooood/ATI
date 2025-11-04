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
            
            return response.getBody();
            
        } catch (Exception e) {
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
            
            return response.getBody();
            
        } catch (Exception e) {
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
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true");
        
        // Create multipart form data
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("writing_task", writingTask);
        body.add("writing_input", writingInput);
        
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
            
            return response.getBody();
            
        } catch (Exception e) {
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
        
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Submit Task 1
            Map<String, Object> task1Result;
            if (task1ImageUrl != null && !task1ImageUrl.isEmpty()) {
                task1Result = submitTask1WithLink(task1Question, task1Answer, task1ImageUrl);
            } else {
                // If no image, use Task 2 endpoint as fallback
                task1Result = submitTask2(task1Question, task1Answer);
            }
            results.put("task1", task1Result);
            
            // Submit Task 2
            Map<String, Object> task2Result = submitTask2(task2Question, task2Answer);
            results.put("task2", task2Result);
            
            // Extract scores and calculate overall
            Double task1Score = extractScore(task1Result.get("message"));
            Double task2Score = extractScore(task2Result.get("message"));
            
            if (task1Score != null && task2Score != null) {
                double overall = (task1Score + task2Score) / 2.0;
                results.put("overallScore", Math.round(overall * 10) / 10.0);
            }
            
            results.put("status", "success");
            
        } catch (Exception e) {
            results.put("status", "error");
            results.put("message", e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Extract band score from API response message
     * Example: "Predicted score: Band 7.0" -> 7.0
     */
    private Double extractScore(Object message) {
        if (message == null) return null;
        
        String messageStr = message.toString();
        
        // Try to extract "Band X.X" pattern
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Band\\s+(\\d+\\.?\\d*)");
        java.util.regex.Matcher matcher = pattern.matcher(messageStr);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
}