package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.IeltsSpeakingTest;
import com.ieltsgrading.ielts_evaluator.repository.IeltsSpeakingTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Speaking Test Controller - Complete Implementation
 * Handles all speaking test related operations
 * 
 * @author ATI Team
 * @version 1.0
 */
@Controller
@RequestMapping("/speaking")
public class SpeakingTestController {

    @Autowired
    private IeltsSpeakingTestRepository speakingTestRepository;

    // Constants
    private static final int PAGE_SIZE = 16;
    private static final int MAX_AUDIO_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_AUDIO_FORMATS = {".mp3", ".wav", ".m4a", ".ogg"};

    /**
     * Display list of speaking tests with filtering and pagination
     * 
     * @param sort Sort criteria (newest, oldest, most-attempted)
     * @param search Search query
     * @param page Current page number
     * @param model Spring Model
     * @return speaking-tests.html template
     */
    @GetMapping("/tests")
    public String speakingTests(
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        
        try {
            // Validate page number
            if (page < 1) page = 1;
            
            // Create pageable with sorting
            Sort sortOrder = getSortOrder(sort);
            Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, sortOrder);
            
            // Fetch tests from database
            Page<IeltsSpeakingTest> testPage;
            if (search != null && !search.trim().isEmpty()) {
                testPage = searchTests(search, pageable);
            } else {
                testPage = speakingTestRepository.findAll(pageable);
            }
            
            // Add attributes to model
            model.addAttribute("pageTitle", "Speaking Test Collection");
            model.addAttribute("testType", "speaking");
            model.addAttribute("tests", testPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", testPage.getTotalPages());
            model.addAttribute("totalTests", testPage.getTotalElements());
            model.addAttribute("currentSort", sort);
            model.addAttribute("searchQuery", search);
            
            // Add pagination info
            model.addAttribute("hasPrevious", testPage.hasPrevious());
            model.addAttribute("hasNext", testPage.hasNext());
            model.addAttribute("previousPage", page - 1);
            model.addAttribute("nextPage", page + 1);
            
            return "speaking-tests";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load tests: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Display individual speaking test page
     * 
     * @param testId Test identifier (e.g., cam20-test4)
     * @param model Spring Model
     * @param session HTTP Session
     * @return speaking-test-page.html template
     */
    @GetMapping("/test/{testId}")
    public String doSpeakingTest(
            @PathVariable String testId,
            Model model,
            HttpSession session) {
        
        try {
            // Find test in database
            Optional<IeltsSpeakingTest> testOptional = findTestById(testId);
            
            if (testOptional.isEmpty()) {
                model.addAttribute("error", "Test not found: " + testId);
                return "error-404";
            }
            
            IeltsSpeakingTest test = testOptional.get();
            
            // Parse test ID
            String[] parts = testId.split("-");
            String camNumber = parts.length > 0 ? parts[0].replace("cam", "") : "";
            String testNumber = parts.length > 1 ? parts[1].replace("test", "") : "";
            
            // Track test view
            trackTestView(testId, session);
            
            // Add attributes to model
            model.addAttribute("pageTitle", "Speaking Test - " + testId);
            model.addAttribute("testId", testId);
            model.addAttribute("test", test);
            model.addAttribute("camNumber", camNumber);
            model.addAttribute("testNumber", testNumber);
            
            // Add test structure
            model.addAttribute("part1Questions", getTestPart1Questions(test));
            model.addAttribute("part2Topic", getTestPart2Topic(test));
            model.addAttribute("part3Questions", getTestPart3Questions(test));
            
            // Add timing information
            model.addAttribute("part1Duration", 4); // 4-5 minutes
            model.addAttribute("part2Duration", 3); // 3-4 minutes (including 1 min prep)
            model.addAttribute("part3Duration", 5); // 4-5 minutes
            model.addAttribute("totalDuration", 12); // 11-14 minutes
            
            return "speaking-test-page";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading test: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Submit speaking test for AI evaluation
     * 
     * @param testId Test identifier
     * @param part1Audio Part 1 audio file
     * @param part2Audio Part 2 audio file
     * @param part3Audio Part 3 audio file
     * @param session HTTP Session
     * @return Redirect to result page
     */
    @PostMapping("/test/{testId}/submit")
    public String submitSpeakingTest(
            @PathVariable String testId,
            @RequestParam(required = false) MultipartFile part1Audio,
            @RequestParam(required = false) MultipartFile part2Audio,
            @RequestParam(required = false) MultipartFile part3Audio,
            HttpSession session,
            Model model) {
        
        try {
            // Validate audio files
            List<String> errors = new ArrayList<>();
            
            if (!isValidAudioFile(part1Audio)) {
                errors.add("Invalid Part 1 audio file");
            }
            if (!isValidAudioFile(part2Audio)) {
                errors.add("Invalid Part 2 audio file");
            }
            if (!isValidAudioFile(part3Audio)) {
                errors.add("Invalid Part 3 audio file");
            }
            
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                return "redirect:/speaking/test/" + testId + "?error=invalid_audio";
            }
            
            // Generate unique submission ID
            String submissionId = UUID.randomUUID().toString();
            
            // Store audio files (in production, upload to cloud storage)
            String part1Path = saveAudioFile(part1Audio, testId, "part1", submissionId);
            String part2Path = saveAudioFile(part2Audio, testId, "part2", submissionId);
            String part3Path = saveAudioFile(part3Audio, testId, "part3", submissionId);
            
            // Store submission info in session (temporary)
            Map<String, Object> submission = new HashMap<>();
            submission.put("testId", testId);
            submission.put("submissionId", submissionId);
            submission.put("part1Audio", part1Path);
            submission.put("part2Audio", part2Path);
            submission.put("part3Audio", part3Path);
            submission.put("submittedAt", LocalDateTime.now());
            submission.put("status", "processing");
            
            session.setAttribute("speaking_submission_" + submissionId, submission);
            
            // TODO: Queue for AI evaluation
            // aiEvaluationService.evaluateSpeaking(submissionId, part1Path, part2Path, part3Path);
            
            System.out.println("Speaking test submitted: " + testId);
            System.out.println("Submission ID: " + submissionId);
            
            return "redirect:/speaking/result/" + submissionId;
            
        } catch (Exception e) {
            model.addAttribute("error", "Submission failed: " + e.getMessage());
            return "redirect:/speaking/test/" + testId + "?error=submission_failed";
        }
    }

    /**
     * Display test result
     * 
     * @param submissionId Submission identifier
     * @param model Spring Model
     * @param session HTTP Session
     * @return speaking-result.html template
     */
    @GetMapping("/result/{submissionId}")
    public String speakingResult(
            @PathVariable String submissionId,
            Model model,
            HttpSession session) {
        
        try {
            // Retrieve submission from session
            @SuppressWarnings("unchecked")
            Map<String, Object> submission = (Map<String, Object>) 
                session.getAttribute("speaking_submission_" + submissionId);
            
            if (submission == null) {
                model.addAttribute("error", "Submission not found");
                return "error-404";
            }
            
            String testId = (String) submission.get("testId");
            String status = (String) submission.get("status");
            
            model.addAttribute("pageTitle", "Speaking Test Result");
            model.addAttribute("testId", testId);
            model.addAttribute("submissionId", submissionId);
            model.addAttribute("status", status);
            
            // TODO: Fetch actual result from database after AI evaluation
            // For now, generate mock result
            if ("processing".equals(status)) {
                // Still processing
                model.addAttribute("processing", true);
                model.addAttribute("message", "Your test is being evaluated by AI. Please wait...");
            } else {
                // Generate mock result
                Map<String, Object> result = generateMockResult(testId);
                model.addAttribute("result", result);
                model.addAttribute("overallScore", result.get("overallScore"));
                model.addAttribute("fluency", result.get("fluency"));
                model.addAttribute("lexicalResource", result.get("lexicalResource"));
                model.addAttribute("grammaticalRange", result.get("grammaticalRange"));
                model.addAttribute("pronunciation", result.get("pronunciation"));
                model.addAttribute("feedback", result.get("feedback"));
                model.addAttribute("strengths", result.get("strengths"));
                model.addAttribute("improvements", result.get("improvements"));
            }
            
            return "speaking-result";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading result: " + e.getMessage());
            return "error";
        }
    }

    /**
     * API endpoint to get speaking tests as JSON
     * 
     * @param sort Sort criteria
     * @param search Search query
     * @param page Page number
     * @param size Page size
     * @return JSON list of tests
     */
    @GetMapping("/api/tests")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSpeakingTestsApi(
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Sort sortOrder = getSortOrder(sort);
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            
            Page<IeltsSpeakingTest> testPage;
            if (search != null && !search.trim().isEmpty()) {
                testPage = searchTests(search, pageable);
            } else {
                testPage = speakingTestRepository.findAll(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("tests", testPage.getContent());
            response.put("currentPage", page);
            response.put("totalPages", testPage.getTotalPages());
            response.put("totalTests", testPage.getTotalElements());
            response.put("hasNext", testPage.hasNext());
            response.put("hasPrevious", testPage.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * API endpoint to get single test details
     * 
     * @param testId Test identifier
     * @return JSON test details
     */
    @GetMapping("/api/test/{testId}")
    @ResponseBody
    public ResponseEntity<?> getSpeakingTestApi(@PathVariable String testId) {
        Optional<IeltsSpeakingTest> test = findTestById(testId);
        
        if (test.isPresent()) {
            return ResponseEntity.ok(test.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Test not found: " + testId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Check evaluation status (AJAX endpoint)
     * 
     * @param submissionId Submission identifier
     * @return JSON with status
     */
    @GetMapping("/api/result/{submissionId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkEvaluationStatus(
            @PathVariable String submissionId,
            HttpSession session) {
        
        @SuppressWarnings("unchecked")
        Map<String, Object> submission = (Map<String, Object>) 
            session.getAttribute("speaking_submission_" + submissionId);
        
        Map<String, Object> response = new HashMap<>();
        
        if (submission == null) {
            response.put("error", "Submission not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        response.put("status", submission.get("status"));
        response.put("submissionId", submissionId);
        response.put("submittedAt", submission.get("submittedAt"));
        
        return ResponseEntity.ok(response);
    }

    // ==================== Helper Methods ====================

    /**
     * Get sort order based on sort criteria
     */
    private Sort getSortOrder(String sort) {
        switch (sort) {
            case "oldest":
                return Sort.by(Sort.Direction.ASC, "createdAt");
            case "most-attempted":
                return Sort.by(Sort.Direction.DESC, "attemptCount");
            default: // newest
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    /**
     * Search tests by query
     */
    private Page<IeltsSpeakingTest> searchTests(String query, Pageable pageable) {
        // TODO: Implement custom search query
        // For now, return all tests
        return speakingTestRepository.findAll(pageable);
    }

    /**
     * Find test by ID
     */
    private Optional<IeltsSpeakingTest> findTestById(String testId) {
        // TODO: Implement proper search by testId field
        List<IeltsSpeakingTest> allTests = speakingTestRepository.findAll();
        return allTests.stream()
                .filter(test -> testId.equals(test.getId()))
                .findFirst();
    }

    /**
     * Track test view
     */
    private void trackTestView(String testId, HttpSession session) {
        String sessionKey = "viewed_" + testId;
        if (session.getAttribute(sessionKey) == null) {
            session.setAttribute(sessionKey, LocalDateTime.now());
            // TODO: Increment view count in database
            System.out.println("Tracking view for test: " + testId);
        }
    }

    /**
     * Validate audio file
     */
    private boolean isValidAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        boolean validFormat = Arrays.stream(ALLOWED_AUDIO_FORMATS)
                .anyMatch(format -> filename.toLowerCase().endsWith(format));
        
        boolean validSize = file.getSize() <= MAX_AUDIO_SIZE;
        
        return validFormat && validSize;
    }

    /**
     * Save audio file
     */
    private String saveAudioFile(MultipartFile file, String testId, String part, String submissionId) 
            throws IOException {
        // TODO: Implement actual file storage (cloud storage in production)
        String filename = String.format("%s_%s_%s_%s.mp3", submissionId, testId, part, System.currentTimeMillis());
        System.out.println("Saving audio file: " + filename);
        return "/uploads/speaking/" + filename;
    }

    /**
     * Get Part 1 questions
     */
    private List<String> getTestPart1Questions(IeltsSpeakingTest test) {
        // TODO: Fetch from database
        return Arrays.asList(
            "Let's talk about your hometown. Where are you from?",
            "What do you like most about your hometown?",
            "Has your hometown changed much in recent years?",
            "Would you like to live there in the future?"
        );
    }

    /**
     * Get Part 2 topic
     */
    private String getTestPart2Topic(IeltsSpeakingTest test) {
        // TODO: Fetch from database
        return "Describe a person who has had an important influence on your life.";
    }

    /**
     * Get Part 3 questions
     */
    private List<String> getTestPart3Questions(IeltsSpeakingTest test) {
        // TODO: Fetch from database
        return Arrays.asList(
            "How important is family in your culture?",
            "Do you think the role of family is changing in modern society?",
            "What qualities make a good role model?"
        );
    }

    /**
     * Generate mock result for demonstration
     */
    private Map<String, Object> generateMockResult(String testId) {
        Map<String, Object> result = new HashMap<>();
        result.put("overallScore", 7.5);
        result.put("fluency", 7.5);
        result.put("lexicalResource", 7.0);
        result.put("grammaticalRange", 8.0);
        result.put("pronunciation", 7.5);
        result.put("feedback", "Strong overall performance with good fluency and range of vocabulary.");
        result.put("strengths", Arrays.asList(
            "Clear pronunciation",
            "Good range of vocabulary",
            "Confident delivery"
        ));
        result.put("improvements", Arrays.asList(
            "Use more complex sentence structures",
            "Expand ideas with more examples",
            "Reduce hesitation in Part 3"
        ));
        return result;
    }
}