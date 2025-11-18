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

@Controller
@RequestMapping("/speaking")
public class SpeakingTestController {

    @Autowired
    private IeltsSpeakingTestRepository speakingTestRepository;

    private static final int PAGE_SIZE = 16;
    private static final int MAX_AUDIO_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_AUDIO_FORMATS = {".mp3", ".wav", ".m4a", ".ogg"};

    /**
     * Display list of speaking tests
     */
    @GetMapping("/tests")
    public String speakingTests(
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        
        try {
            if (page < 1) page = 1;
            
            Sort sortOrder = getSortOrder(sort);
            Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, sortOrder);
            
            Page<IeltsSpeakingTest> testPage;
            if (search != null && !search.trim().isEmpty()) {
                testPage = speakingTestRepository.findAll(pageable);
            } else {
                testPage = speakingTestRepository.findAll(pageable);
            }
            
            model.addAttribute("pageTitle", "Speaking Test Collection");
            model.addAttribute("testType", "speaking");
            model.addAttribute("tests", testPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", testPage.getTotalPages());
            model.addAttribute("totalTests", testPage.getTotalElements());
            model.addAttribute("currentSort", sort);
            model.addAttribute("searchQuery", search);
            model.addAttribute("hasPrevious", testPage.hasPrevious());
            model.addAttribute("hasNext", testPage.hasNext());
            model.addAttribute("previousPage", page - 1);
            model.addAttribute("nextPage", page + 1);
            
            System.out.println("✅ Loaded " + testPage.getContent().size() + " speaking tests");
            
            return "speaking-tests";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to load tests: " + e.getMessage());
            model.addAttribute("tests", Collections.emptyList());
            return "speaking-tests";
        }
    }

    /**
     * Display individual test - FIXED to extract ID from testId string
     */
    @GetMapping("/test/{testId}")
    public String doSpeakingTest(
            @PathVariable String testId,
            Model model,
            HttpSession session) {
        
        try {
            // Extract numeric ID from testId (e.g., "test-1" -> 1)
            Long id = extractIdFromTestId(testId);
            
            Optional<IeltsSpeakingTest> testOptional = speakingTestRepository.findById(id);
            
            if (testOptional.isEmpty()) {
                model.addAttribute("error", "Test not found: " + testId);
                return "error-404";
            }
            
            IeltsSpeakingTest test = testOptional.get();
            
            trackTestView(testId, session);
            
            model.addAttribute("pageTitle", "Speaking Test - " + test.getTestDate());
            model.addAttribute("testId", testId);
            model.addAttribute("test", test);
            model.addAttribute("camNumber", "");
            model.addAttribute("testNumber", test.getTestNumber());
            
            // Add test structure
            model.addAttribute("part1Questions", getTestPart1Questions(test));
            model.addAttribute("part2Topic", getTestPart2Topic(test));
            model.addAttribute("part3Questions", getTestPart3Questions(test));
            
            model.addAttribute("part1Duration", 4);
            model.addAttribute("part2Duration", 3);
            model.addAttribute("part3Duration", 5);
            model.addAttribute("totalDuration", 12);
            
            return "speaking-test-page";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading test: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Submit speaking test
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
            
            String submissionId = UUID.randomUUID().toString();
            
            String part1Path = saveAudioFile(part1Audio, testId, "part1", submissionId);
            String part2Path = saveAudioFile(part2Audio, testId, "part2", submissionId);
            String part3Path = saveAudioFile(part3Audio, testId, "part3", submissionId);
            
            Map<String, Object> submission = new HashMap<>();
            submission.put("testId", testId);
            submission.put("submissionId", submissionId);
            submission.put("part1Audio", part1Path);
            submission.put("part2Audio", part2Path);
            submission.put("part3Audio", part3Path);
            submission.put("submittedAt", LocalDateTime.now());
            submission.put("status", "processing");
            
            session.setAttribute("speaking_submission_" + submissionId, submission);
            
            System.out.println("Speaking test submitted: " + testId);
            System.out.println("Submission ID: " + submissionId);
            
            return "redirect:/speaking/result/" + submissionId;
            
        } catch (Exception e) {
            model.addAttribute("error", "Submission failed: " + e.getMessage());
            return "redirect:/speaking/test/" + testId + "?error=submission_failed";
        }
    }

    /**
     * Display result
     */
    @GetMapping("/result/{submissionId}")
    public String speakingResult(
            @PathVariable String submissionId,
            Model model,
            HttpSession session) {
        
        try {
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
            
            if ("processing".equals(status)) {
                model.addAttribute("processing", true);
                model.addAttribute("message", "Your test is being evaluated by AI. Please wait...");
            } else {
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
     * API endpoint
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
            
            Page<IeltsSpeakingTest> testPage = speakingTestRepository.findAll(pageable);
            
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

    @GetMapping("/api/test/{testId}")
    @ResponseBody
    public ResponseEntity<?> getSpeakingTestApi(@PathVariable String testId) {
        try {
            Long id = extractIdFromTestId(testId);
            Optional<IeltsSpeakingTest> test = speakingTestRepository.findById(id);
            
            if (test.isPresent()) {
                return ResponseEntity.ok(test.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Test not found: " + testId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

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
     * Extract numeric ID from testId string (e.g., "test-1" -> 1)
     */
    private Long extractIdFromTestId(String testId) {
        try {
            String[] parts = testId.split("-");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid testId format: " + testId);
        }
    }

    private Sort getSortOrder(String sort) {
        switch (sort) {
            case "oldest":
                return Sort.by(Sort.Direction.ASC, "createdAt");
            case "most-attempted":
                return Sort.by(Sort.Direction.DESC, "id");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private void trackTestView(String testId, HttpSession session) {
        String sessionKey = "viewed_" + testId;
        if (session.getAttribute(sessionKey) == null) {
            session.setAttribute(sessionKey, LocalDateTime.now());
            System.out.println("Tracking view for test: " + testId);
        }
    }

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

    private String saveAudioFile(MultipartFile file, String testId, String part, String submissionId) 
            throws IOException {
        String filename = String.format("%s_%s_%s_%s.mp3", submissionId, testId, part, System.currentTimeMillis());
        System.out.println("Saving audio file: " + filename);
        return "/uploads/speaking/" + filename;
    }

    private List<String> getTestPart1Questions(IeltsSpeakingTest test) {
        return Arrays.asList(
            "Let's talk about your hometown. Where are you from?",
            "What do you like most about your hometown?",
            "Has your hometown changed much in recent years?",
            "Would you like to live there in the future?"
        );
    }

    private String getTestPart2Topic(IeltsSpeakingTest test) {
        return test.getMainTopic() != null ? test.getMainTopic() : "Describe a memorable event in your life.";
    }

    private List<String> getTestPart3Questions(IeltsSpeakingTest test) {
        return Arrays.asList(
            "How important is family in your culture?",
            "Do you think the role of family is changing in modern society?",
            "What qualities make a good role model?"
        );
    }

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