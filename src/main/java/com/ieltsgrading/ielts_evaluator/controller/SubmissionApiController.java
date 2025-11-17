package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.TestSubmission;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.service.TestSubmissionService;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * API Controller for Test Submissions
 * Provides REST endpoints for dashboard interactions
 */
@RestController
@RequestMapping("/api/submissions")
public class SubmissionApiController {

    @Autowired
    private TestSubmissionService submissionService;

    @Autowired
    private UserService userService;

    /**
     * Get submission status
     * GET /api/submissions/{uuid}/status
     */
    @GetMapping("/{submissionUuid}/status")
    public ResponseEntity<Map<String, Object>> getSubmissionStatus(
            @PathVariable String submissionUuid) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<TestSubmission> optSubmission = submissionService.getSubmission(submissionUuid);
            
            if (optSubmission.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Submission not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            TestSubmission submission = optSubmission.get();
            
            response.put("status", "success");
            response.put("submissionUuid", submission.getSubmissionUuid());
            response.put("submissionStatus", submission.getStatus());
            response.put("testType", submission.getTestType());
            response.put("isCompleted", submission.isCompleted());
            response.put("isProcessing", submission.isProcessing());
            response.put("isFailed", submission.isFailed());
            
            if (submission.isCompleted()) {
                response.put("overallScore", submission.getOverallScore());
                response.put("task1Score", submission.getTask1Score());
                response.put("task2Score", submission.getTask2Score());
            }
            
            if (submission.isFailed()) {
                response.put("errorMessage", submission.getErrorMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retry failed submission
     * POST /api/submissions/{uuid}/retry
     */
    @PostMapping("/{submissionUuid}/retry")
    public ResponseEntity<Map<String, Object>> retrySubmission(
            @PathVariable String submissionUuid) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                response.put("status", "error");
                response.put("message", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Get user
            User user = userService.getUserByEmail(auth.getName());
            
            // Get submission
            Optional<TestSubmission> optSubmission = submissionService.getSubmission(submissionUuid);
            
            if (optSubmission.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Submission not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            TestSubmission submission = optSubmission.get();
            
            // Check ownership
            if (!submission.getUser().getId().equals(user.getId())) {
                response.put("status", "error");
                response.put("message", "Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Check if failed
            if (!submission.isFailed()) {
                response.put("status", "error");
                response.put("message", "Only failed submissions can be retried");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Retry submission
            submissionService.retrySubmission(submissionUuid);
            
            response.put("status", "success");
            response.put("message", "Submission is being reprocessed");
            response.put("submissionUuid", submissionUuid);
            
            System.out.println("🔄 Retry requested for submission: " + submissionUuid);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Retry error: " + e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete submission
     * DELETE /api/submissions/{uuid}
     */
    @DeleteMapping("/{submissionUuid}")
    public ResponseEntity<Map<String, Object>> deleteSubmission(
            @PathVariable String submissionUuid) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                response.put("status", "error");
                response.put("message", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userService.getUserByEmail(auth.getName());
            Optional<TestSubmission> optSubmission = submissionService.getSubmission(submissionUuid);
            
            if (optSubmission.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Submission not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            TestSubmission submission = optSubmission.get();
            
            // Check ownership
            if (!submission.getUser().getId().equals(user.getId())) {
                response.put("status", "error");
                response.put("message", "Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // TODO: Implement delete functionality
            response.put("status", "error");
            response.put("message", "Delete functionality not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}