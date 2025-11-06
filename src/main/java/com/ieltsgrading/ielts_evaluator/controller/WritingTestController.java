package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.model.IeltsWritingTest;
import com.ieltsgrading.ielts_evaluator.model.TestSubmission;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.repository.IeltsWritingTestRepository;
import com.ieltsgrading.ielts_evaluator.service.TestSubmissionService;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import com.ieltsgrading.ielts_evaluator.service.WritingApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/writing")
public class WritingTestController {
    @Autowired
    private TestSubmissionService submissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private WritingApiService writingApiService;

    @Autowired
    private IeltsWritingTestRepository writingTestRepository;

    private static final int PAGE_SIZE = 16;
    private static final int MIN_TASK1_WORDS = 150;
    private static final int MIN_TASK2_WORDS = 250;

    /**
     * Display list of writing tests with filtering and pagination
     */
    @GetMapping("/tests")
    public String writingTests(
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        try {
            if (page < 1)
                page = 1;

            // Get tests from database
            List<IeltsWritingTest> tests;

            if (search != null && !search.trim().isEmpty()) {
                tests = writingTestRepository.searchByKeyword(search.trim());
            } else {
                tests = writingTestRepository.findAll();
            }

            // Convert to Map for easier template rendering
            List<Map<String, Object>> testMaps = tests.stream()
                    .map(this::convertTestToMap)
                    .collect(Collectors.toList());

            // Apply sorting
            testMaps = sortTests(testMaps, sort);

            // Calculate pagination
            int totalTests = testMaps.size();
            int totalPages = (int) Math.ceil((double) totalTests / PAGE_SIZE);
            int startIndex = (page - 1) * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, totalTests);

            List<Map<String, Object>> pagedTests = startIndex < totalTests ? testMaps.subList(startIndex, endIndex)
                    : Collections.emptyList();

            // Add attributes to model
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
            System.err.println("Error loading tests: " + e.getMessage());
            e.printStackTrace();
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
            // Find test from database by display ID (e.g., "cam20-test4")
            Optional<IeltsWritingTest> testOpt = writingTestRepository.findByDisplayId(testId);

            if (testOpt.isEmpty()) {
                model.addAttribute("error", "Test not found: " + testId);
                return "error-404";
            }

            IeltsWritingTest test = testOpt.get();

            // Track test view
            trackTestView(testId, session);

            // Load saved draft if exists
            String task1Draft = loadDraft(session, testId, "task1");
            String task2Draft = loadDraft(session, testId, "task2");

            // Add attributes to model
            model.addAttribute("pageTitle",
                    "Writing Test - CAM " + test.getCamNumber() + " Test " + test.getTestNumber());
            model.addAttribute("testId", testId);
            model.addAttribute("camNumber", test.getCamNumber());
            model.addAttribute("testNumber", test.getTestNumber());

            // Add test questions
            model.addAttribute("task1Type", test.getTask1Type());
            model.addAttribute("task1Question", test.getTask1Question());
            model.addAttribute("task1ImageUrl", test.getDirectImageUrl());
            model.addAttribute("task2Question", test.getTask2Question());

            // Add drafts
            model.addAttribute("task1Draft", task1Draft);
            model.addAttribute("task2Draft", task2Draft);

            // Add word count requirements
            model.addAttribute("task1MinWords", MIN_TASK1_WORDS);
            model.addAttribute("task2MinWords", MIN_TASK2_WORDS);
            model.addAttribute("totalDuration", 60); // 60 minutes

            return "writing-test-page";

        } catch (Exception e) {
            System.err.println("Error loading test: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading test: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Submit writing test - Save to database and process async
     */
    @PostMapping("/test/{testId}/submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitWritingTest(
            @PathVariable String testId,
            @RequestParam String task1Answer,
            @RequestParam String task2Answer,
            @RequestParam(required = false, defaultValue = "0") int timeSpent,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Check authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để submit test");
                response.put("redirect", "/user/login");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get current user
            String email = auth.getName();
            User user = userService.getUserByEmail(email);

            if (user == null) {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }

            // Find test from database
            Optional<IeltsWritingTest> testOpt = writingTestRepository.findByDisplayId(testId);

            if (testOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Test not found");
                return ResponseEntity.badRequest().body(response);
            }

            IeltsWritingTest test = testOpt.get();

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

            // Create submission in database
            TestSubmission submission = submissionService.createSubmission(
                    user, test, task1Answer, task2Answer, task1Words, task2Words, timeSpent);

            // Process submission asynchronously
            submissionService.processSubmissionAsync(submission.getSubmissionUuid());

            // Clear drafts after submission
            clearDraft(session, testId, "task1");
            clearDraft(session, testId, "task2");

            System.out.println("✅ Writing test submitted: " + testId);
            System.out.println("   Submission UUID: " + submission.getSubmissionUuid());
            System.out.println("   User: " + user.getName());

            response.put("status", "success");
            response.put("submissionId", submission.getSubmissionUuid());
            response.put("message", "Test submitted successfully. Redirecting to dashboard...");
            response.put("redirect", "/dashboard");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Submission error: " + e.getMessage());
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
            @RequestParam String task1Answer,
            @RequestParam MultipartFile task1Image,
            @RequestParam String task2Answer,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Find test from database
            Optional<IeltsWritingTest> testOpt = writingTestRepository.findByDisplayId(testId);

            if (testOpt.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Test not found");
                return ResponseEntity.badRequest().body(response);
            }

            IeltsWritingTest test = testOpt.get();

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
                    test.getTask1Question(),
                    task1Answer,
                    task1Image);

            // Submit Task 2
            Map<String, Object> task2Result = writingApiService.submitTask2(
                    test.getTask2Question(),
                    task2Answer);

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
            Map<String, Object> submission = (Map<String, Object>) session
                    .getAttribute("writing_submission_" + submissionId);

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

    /**
     * Convert IeltsWritingTest entity to Map for template
     */
    private Map<String, Object> convertTestToMap(IeltsWritingTest test) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", test.getDisplayId());
        map.put("title", "CAM " + test.getCamNumber() + " - Writing Test " + test.getTestNumber());
        map.put("views", (int) (Math.random() * 20000 + 10000)); // Mock views
        map.put("background", test.getBackgroundColor());
        map.put("cam", test.getCamNumber());
        map.put("testNumber", test.getTestNumber());
        map.put("createdAt", LocalDateTime.now().minusDays(test.getTestId())); // Mock date
        map.put("testId", test.getTestId());
        return map;
    }

    /**
     * Sort tests based on criteria
     */
    private List<Map<String, Object>> sortTests(List<Map<String, Object>> tests, String sort) {
        List<Map<String, Object>> sortedTests = new ArrayList<>(tests);

        switch (sort) {
            case "oldest":
                sortedTests.sort(Comparator.comparing(t -> (LocalDateTime) t.get("createdAt")));
                break;
            case "most-attempted":
                sortedTests.sort(Comparator.comparing((Map<String, Object> t) -> (Integer) t.get("views")).reversed());
                break;
            default: // newest
                sortedTests.sort(
                        Comparator.comparing((Map<String, Object> t) -> (LocalDateTime) t.get("createdAt")).reversed());
                break;
        }

        return sortedTests;
    }

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
}