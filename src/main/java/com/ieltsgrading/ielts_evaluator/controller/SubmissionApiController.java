package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.*;
import com.ieltsgrading.ielts_evaluator.service.TestSubmissionService;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for Submission Polling
 * Used by dashboard.js to check submission status
 */
@RestController
@RequestMapping("/api")
public class SubmissionApiController {

    @Autowired
    private TestSubmissionService submissionService;

    @Autowired
    private UserService userService;

    /**
     * Get pending submissions for current user
     * Used by dashboard to show "grading in progress" banner
     * 
     * GET /api/submissions/pending
     */
    @GetMapping("/submissions/pending")
    public ResponseEntity<Map<String, Object>> getPendingSubmissions() {
        try {
            // Check authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            // Get current user
            User user = userService.getUserByEmail(auth.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            // Get pending submissions
            List<ITestSubmission> pendingSubmissions = submissionService.getPendingSubmissions(user);

            // Convert to simple DTOs for JSON response
            List<Map<String, Object>> submissions = pendingSubmissions.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("submissions", submissions);
            response.put("count", submissions.size());

            System.out.println("📋 API: Pending submissions for " + user.getName() + ": " + submissions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ API Error (pending submissions): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get submission status by UUID
     * Used for polling individual submission status
     * 
     * GET /api/submission/{uuid}/status
     */
    @GetMapping("/submission/{submissionUuid}/status")
    public ResponseEntity<Map<String, Object>> getSubmissionStatus(
            @PathVariable String submissionUuid) {
        try {
            // Check authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            User user = userService.getUserByEmail(auth.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            // Get submission
            Optional<ITestSubmission> submissionOpt = submissionService.getSubmission(submissionUuid);

            if (submissionOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Submission not found"));
            }

            ITestSubmission submission = submissionOpt.get();

            // Verify ownership
            if (!submission.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Return status
            Map<String, Object> statusMap = submissionService.getSubmissionStatus(submissionUuid);
            
            System.out.println("📊 API: Status check for " + submissionUuid + " → " + submission.getStatus());

            return ResponseEntity.ok(statusMap);

        } catch (Exception e) {
            System.err.println("❌ API Error (submission status): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all submissions for current user
     * Optional endpoint for fetching complete submission history
     * 
     * GET /api/submissions
     */
    @GetMapping("/submissions")
    public ResponseEntity<Map<String, Object>> getAllSubmissions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String testType) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            User user = userService.getUserByEmail(auth.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            // Get all submissions
            List<ITestSubmission> allSubmissions = submissionService.getUserSubmissions(user);

            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                allSubmissions = allSubmissions.stream()
                        .filter(s -> status.equalsIgnoreCase(s.getStatus()))
                        .collect(Collectors.toList());
            }

            // Filter by test type if provided
            if (testType != null && !testType.isEmpty()) {
                allSubmissions = allSubmissions.stream()
                        .filter(s -> testType.equalsIgnoreCase(s.getTestType()))
                        .collect(Collectors.toList());
            }

            List<Map<String, Object>> submissions = allSubmissions.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("submissions", submissions);
            response.put("count", submissions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ API Error (all submissions): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get user statistics
     * 
     * GET /api/user/stats
     */
    @GetMapping("/user/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            User user = userService.getUserByEmail(auth.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            Map<String, Object> stats = submissionService.getUserStats(user);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            System.err.println("❌ API Error (user stats): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retry failed submission
     * 
     * POST /api/submission/{uuid}/retry
     */
    @PostMapping("/submission/{submissionUuid}/retry")
    public ResponseEntity<Map<String, Object>> retrySubmission(
            @PathVariable String submissionUuid) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
            }

            User user = userService.getUserByEmail(auth.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            // Get submission and verify ownership
            Optional<ITestSubmission> submissionOpt = submissionService.getSubmission(submissionUuid);

            if (submissionOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Submission not found"));
            }

            ITestSubmission submission = submissionOpt.get();

            if (!submission.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            if (!submission.isFailed()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Only failed submissions can be retried"));
            }

            // Retry submission
            if ("writing".equals(submission.getTestType())) {
                submissionService.processWritingSubmissionAsync(submissionUuid);
            }
            // TODO: Add speaking retry logic

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Submission is being retried");
            response.put("submissionUuid", submissionUuid);

            System.out.println("🔄 Retrying submission: " + submissionUuid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ API Error (retry submission): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Convert ITestSubmission to DTO for JSON response
     */
    private Map<String, Object> convertToDto(ITestSubmission submission) {
        Map<String, Object> dto = new HashMap<>();
        
        dto.put("submissionUuid", submission.getSubmissionUuid());
        dto.put("testType", submission.getTestType());
        dto.put("testDisplayName", submission.getTestDisplayName());
        dto.put("status", submission.getStatus());
        dto.put("statusDisplay", submission.getStatusDisplay());
        dto.put("overallScore", submission.getOverallScore());
        dto.put("submittedAt", submission.getSubmittedAt());
        dto.put("completedAt", submission.getCompletedAt());
        dto.put("timeSpent", submission.getTimeSpent());
        dto.put("errorMessage", submission.getErrorMessage());
        
        // Add test-type specific fields
        if ("writing".equals(submission.getTestType())) {
            dto.put("task1Score", submission.getTask1Score());
            dto.put("task2Score", submission.getTask2Score());
            dto.put("task1WordCount", submission.getTask1WordCount());
            dto.put("task2WordCount", submission.getTask2WordCount());
        }
        
        return dto;
    }
}