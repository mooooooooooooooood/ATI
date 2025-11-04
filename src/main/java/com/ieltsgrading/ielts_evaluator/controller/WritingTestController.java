package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.service.WritingApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/writing")
public class WritingTestController {
    
    @Autowired
    private WritingApiService writingApiService;
    
    private static final int PAGE_SIZE = 16;
    private static final int MIN_TASK1_WORDS = 150;
    private static final int MIN_TASK2_WORDS = 250;
    
    /**
     * Display list of writing tests
     */
    @GetMapping("/tests")
    public String writingTests(
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        
        try {
            if (page < 1) page = 1;
            
            List<Map<String, Object>> tests = generateMockTests();
            
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                tests = tests.stream()
                    .filter(test -> test.get("title").toString().toLowerCase().contains(searchLower))
                    .toList();
            }
            
            tests = sortTests(tests, sort);
            
            int totalTests = tests.size();
            int totalPages = (int) Math.ceil((double) totalTests / PAGE_SIZE);
            int startIndex = (page - 1) * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, totalTests);
            
            List<Map<String, Object>> pagedTests = startIndex < totalTests ?
                tests.subList(startIndex, endIndex) : Collections.emptyList();
            
            model.addAttribute("pageTitle", "Writing Test Collection");
            model.addAttribute("testType", "writing");
            model.addAttribute("tests", pagedTests);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalTests", totalTests);
            model.addAttribute("currentSort", sort);
            model.addAttribute("searchQuery", search);
            model.addAttribute("hasPrevious", page > 1);
            model.addAttribute("hasNext", page < totalPages);
            model.addAttribute("previousPage", page - 1);
            model.addAttribute("nextPage", page + 1);
            
            return "writing-tests";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load tests: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Display individual writing test page
     */
    @GetMapping("/test/{testId}")
    public String doWritingTest(
            @PathVariable String testId,
            Model model,
            HttpSession session) {
        
        try {
            String[] parts = testId.split("-");
            String camNumber = parts.length > 0 ? parts[0].replace("cam", "") : "";
            String testNumber = parts.length > 1 ? parts[1].replace("test", "") : "";
            
            trackTestView(testId, session);
            Map<String, String> testQuestions = getTestQuestions(testId);
            
            String task1Draft = loadDraft(session, testId, "task1");
            String task2Draft = loadDraft(session, testId, "task2");
            
            model.addAttribute("pageTitle", "Writing Test - CAM " + camNumber + " Test " + testNumber);
            model.addAttribute("testId", testId);
            model.addAttribute("camNumber", camNumber);
            model.addAttribute("testNumber", testNumber);
            model.addAttribute("task1Type", testQuestions.get("task1Type"));
            model.addAttribute("task1Question", testQuestions.get("task1Question"));
            model.addAttribute("task1ImageUrl", testQuestions.get("task1ImageUrl"));
            model.addAttribute("task2Question", testQuestions.get("task2Question"));
            model.addAttribute("task1Draft", task1Draft);
            model.addAttribute("task2Draft", task2Draft);
            model.addAttribute("task1MinWords", MIN_TASK1_WORDS);
            model.addAttribute("task2MinWords", MIN_TASK2_WORDS);
            model.addAttribute("totalDuration", 60);
            
            return "writing-test-page";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading test: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Submit writing test - Calls external API
     */
    @PostMapping("/test/{testId}/submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitWritingTest(
            @PathVariable String testId,
            @RequestParam String task1Question,
            @RequestParam String task1Answer,
            @RequestParam String task2Question,
            @RequestParam String task2Answer,
            @RequestParam(required = false) String task1ImageUrl,
            @RequestParam(required = false, defaultValue = "0") int timeSpent,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate answers
            List<String> errors = new ArrayList<>();
            int task1Words = countWords(task1Answer);
            int task2Words = countWords(task2Answer);
            
            if (task1Words < MIN_TASK1_WORDS) {
                errors.add("Task 1 requires at least " + MIN_TASK1_WORDS + " words (you have " + task1Words + ")");
            }
            
            if (task2Words < MIN_TASK2_WORDS) {
                errors.add("Task 2 requires at least " + MIN_TASK2_WORDS + " words (you have " + task2Words + ")");
            }
            
            if (!errors.isEmpty()) {
                response.put("status", "error");
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Call external API through service
            Map<String, Object> apiResults = writingApiService.submitCompleteTest(
                task1Question,
                task1Answer,
                task2Question,
                task2Answer,
                task1ImageUrl
            );
            
            // Generate submission ID
            String submissionId = UUID.randomUUID().toString();
            
            // Store submission with API results
            Map<String, Object> submission = new HashMap<>();
            submission.put("testId", testId);
            submission.put("submissionId", submissionId);
            submission.put("task1Answer", task1Answer);
            submission.put("task2Answer", task2Answer);
            submission.put("task1Words", task1Words);
            submission.put("task2Words", task2Words);
            submission.put("timeSpent", timeSpent);
            submission.put("submittedAt", LocalDateTime.now());
            submission.put("apiResults", apiResults);
            
            session.setAttribute("writing_submission_" + submissionId, submission);
            
            // Clear drafts
            clearDraft(session, testId, "task1");
            clearDraft(session, testId, "task2");
            
            System.out.println("Writing test submitted: " + testId);
            System.out.println("Submission ID: " + submissionId);
            System.out.println("API Results: " + apiResults);
            
            response.put("status", "success");
            response.put("submissionId", submissionId);
            response.put("results", apiResults);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Submission error: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "error");
            response.put("message", "Submission failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Submit with file upload (for Task 1 with image file)
     */
    @PostMapping("/test/{testId}/submit-with-file")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitWithFile(
            @PathVariable String testId,
            @RequestParam String task1Question,
            @RequestParam String task1Answer,
            @RequestParam MultipartFile task1Image,
            @RequestParam String task2Question,
            @RequestParam String task2Answer,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate
            int task1Words = countWords(task1Answer);
            int task2Words = countWords(task2Answer);
            
            if (task1Words < MIN_TASK1_WORDS || task2Words < MIN_TASK2_WORDS) {
                response.put("status", "error");
                response.put("message", "Insufficient word count");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Submit Task 1 with file
            Map<String, Object> task1Result = writingApiService.submitTask1WithFile(
                task1Question,
                task1Answer,
                task1Image
            );
            
            // Submit Task 2
            Map<String, Object> task2Result = writingApiService.submitTask2(
                task2Question,
                task2Answer
            );
            
            // Combine results
            Map<String, Object> apiResults = new HashMap<>();
            apiResults.put("task1", task1Result);
            apiResults.put("task2", task2Result);
            
            String submissionId = UUID.randomUUID().toString();
            
            Map<String, Object> submission = new HashMap<>();
            submission.put("testId", testId);
            submission.put("submissionId", submissionId);
            submission.put("apiResults", apiResults);
            session.setAttribute("writing_submission_" + submissionId, submission);
            
            response.put("status", "success");
            response.put("submissionId", submissionId);
            response.put("results", apiResults);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Display test result
     */
    @GetMapping("/result/{submissionId}")
    public String writingResult(
            @PathVariable String submissionId,
            Model model,
            HttpSession session) {
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> submission = (Map<String, Object>)
                session.getAttribute("writing_submission_" + submissionId);
            
            if (submission == null) {
                model.addAttribute("error", "Submission not found");
                return "error-404";
            }
            
            String testId = (String) submission.get("testId");
            
            model.addAttribute("pageTitle", "Writing Test Result");
            model.addAttribute("testId", testId);
            model.addAttribute("submissionId", submissionId);
            model.addAttribute("task1Words", submission.get("task1Words"));
            model.addAttribute("task2Words", submission.get("task2Words"));
            model.addAttribute("timeSpent", submission.get("timeSpent"));
            model.addAttribute("submittedAt", submission.get("submittedAt"));
            
            // Get API results
            @SuppressWarnings("unchecked")
            Map<String, Object> apiResults = (Map<String, Object>) submission.get("apiResults");
            
            if (apiResults != null) {
                model.addAttribute("apiResults", apiResults);
                model.addAttribute("task1Result", apiResults.get("task1"));
                model.addAttribute("task2Result", apiResults.get("task2"));
                model.addAttribute("overallScore", apiResults.get("overallScore"));
            }
            
            model.addAttribute("task1Answer", submission.get("task1Answer"));
            model.addAttribute("task2Answer", submission.get("task2Answer"));
            
            return "writing-result";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading result: " + e.getMessage());
            return "error";
        }
    }
    
    // ==================== Helper Methods ====================
    
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
    
    private void trackTestView(String testId, HttpSession session) {
        String sessionKey = "viewed_writing_" + testId;
        if (session.getAttribute(sessionKey) == null) {
            session.setAttribute(sessionKey, LocalDateTime.now());
            System.out.println("Tracking view for writing test: " + testId);
        }
    }
    
    private String loadDraft(HttpSession session, String testId, String task) {
        String draftKey = "draft_" + testId + "_" + task;
        Object draft = session.getAttribute(draftKey);
        return draft != null ? draft.toString() : "";
    }
    
    private void clearDraft(HttpSession session, String testId, String task) {
        String draftKey = "draft_" + testId + "_" + task;
        session.removeAttribute(draftKey);
        session.removeAttribute(draftKey + "_timestamp");
    }
    
    private Map<String, String> getTestQuestions(String testId) {
        Map<String, String> questions = new HashMap<>();
        questions.put("task1Type", "Graph/Chart");
        questions.put("task1Question",
            "The chart below shows the percentage of households in owned and rented accommodation in England and Wales between 1918 and 2011. " +
            "Summarise the information by selecting and reporting the main features, and make comparisons where relevant.");
        questions.put("task1ImageUrl", "/images/charts/" + testId + "_task1.png");
        questions.put("task2Question",
            "Some people believe that it is best to accept a bad situation, such as an unsatisfactory job or shortage of money. " +
            "Others argue that it is better to try and improve such situations. " +
            "Discuss both these views and give your own opinion.");
        return questions;
    }
    
    private List<Map<String, Object>> generateMockTests() {
        List<Map<String, Object>> tests = new ArrayList<>();
        int[] cams = {20, 19, 18, 17, 16};
        String[] backgrounds = {"purple", "beige", "dark", "green", "blue"};
        
        for (int i = 0; i < cams.length; i++) {
            for (int j = 4; j >= 1; j--) {
                Map<String, Object> test = new HashMap<>();
                test.put("id", "cam" + cams[i] + "-test" + j);
                test.put("title", "CAM " + cams[i] + " - Writing Test " + j);
                test.put("views", (int)(Math.random() * 20000 + 10000));
                test.put("background", backgrounds[i]);
                test.put("cam", cams[i]);
                test.put("testNumber", j);
                test.put("createdAt", LocalDateTime.now().minusDays((cams.length - i) * 30L + (4 - j)));
                tests.add(test);
            }
        }
        return tests;
    }
    
    private List<Map<String, Object>> sortTests(List<Map<String, Object>> tests, String sort) {
        List<Map<String, Object>> sortedTests = new ArrayList<>(tests);
        switch (sort) {
            case "oldest":
                sortedTests.sort(Comparator.comparing(t -> (LocalDateTime) t.get("createdAt")));
                break;
            case "most-attempted":
                sortedTests.sort(Comparator.comparing((Map<String, Object> t) -> (Integer) t.get("views")).reversed());
                break;
            default:
                sortedTests.sort(Comparator.comparing((Map<String, Object> t) -> (LocalDateTime) t.get("createdAt")).reversed());
                break;
        }
        return sortedTests;
    }
}