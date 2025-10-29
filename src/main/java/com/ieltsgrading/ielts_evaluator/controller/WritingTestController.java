package com.ieltsgrading.ielts_evaluator.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writing Test Controller - Complete Implementation
 * Handles all writing test related operations
 * 
 * @author ATI Team
 * @version 1.0
 */
@Controller
@RequestMapping("/writing")
public class WritingTestController {

    // Constants
    private static final int PAGE_SIZE = 16;
    private static final int MIN_TASK1_WORDS = 150;
    private static final int MIN_TASK2_WORDS = 250;
    private static final int MAX_DRAFT_SIZE = 10000; // characters
    private static final long AUTO_SAVE_INTERVAL = 30000; // 30 seconds

    /**
     * Display list of writing tests with filtering and pagination
     * 
     * @param sort Sort criteria (newest, oldest, most-attempted)
     * @param search Search query
     * @param page Current page number
     * @param model Spring Model
     * @return writing-tests.html template
     */
    @GetMapping("/tests")
    public String writingTests(
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        
        try {
            // Validate page number
            if (page < 1) page = 1;
            
            // Create mock test list for demonstration
            List<Map<String, Object>> tests = generateMockTests();
            
            // Apply search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                tests = tests.stream()
                    .filter(test -> test.get("title").toString().toLowerCase().contains(searchLower))
                    .toList();
            }
            
            // Apply sorting
            tests = sortTests(tests, sort);
            
            // Calculate pagination
            int totalTests = tests.size();
            int totalPages = (int) Math.ceil((double) totalTests / PAGE_SIZE);
            int startIndex = (page - 1) * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, totalTests);
            
            List<Map<String, Object>> pagedTests = startIndex < totalTests ? 
                tests.subList(startIndex, endIndex) : Collections.emptyList();
            
            // Add attributes to model
            model.addAttribute("pageTitle", "Writing Test Collection");
            model.addAttribute("testType", "writing");
            model.addAttribute("tests", pagedTests);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalTests", totalTests);
            model.addAttribute("currentSort", sort);
            model.addAttribute("searchQuery", search);
            
            // Add pagination info
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
     * 
     * @param testId Test identifier (e.g., cam20-test4)
     * @param model Spring Model
     * @param session HTTP Session
     * @return writing-test-page.html template
     */
    @GetMapping("/test/{testId}")
    public String doWritingTest(
            @PathVariable String testId,
            Model model,
            HttpSession session) {
        
        try {
            // Parse test ID
            String[] parts = testId.split("-");
            String camNumber = parts.length > 0 ? parts[0].replace("cam", "") : "";
            String testNumber = parts.length > 1 ? parts[1].replace("test", "") : "";
            
            // Track test view
            trackTestView(testId, session);
            
            // Fetch test questions (mock data for now)
            Map<String, String> testQuestions = getTestQuestions(testId);
            
            // Load saved draft if exists
            String task1Draft = loadDraft(session, testId, "task1");
            String task2Draft = loadDraft(session, testId, "task2");
            
            // Add attributes to model
            model.addAttribute("pageTitle", "Writing Test - " + testId);
            model.addAttribute("testId", testId);
            model.addAttribute("camNumber", camNumber);
            model.addAttribute("testNumber", testNumber);
            
            // Add test questions
            model.addAttribute("task1Type", testQuestions.get("task1Type"));
            model.addAttribute("task1Question", testQuestions.get("task1Question"));
            model.addAttribute("task1ImageUrl", testQuestions.get("task1ImageUrl"));
            model.addAttribute("task2Question", testQuestions.get("task2Question"));
            
            // Add drafts
            model.addAttribute("task1Draft", task1Draft);
            model.addAttribute("task2Draft", task2Draft);
            
            // Add word count requirements
            model.addAttribute("task1MinWords", MIN_TASK1_WORDS);
            model.addAttribute("task2MinWords", MIN_TASK2_WORDS);
            model.addAttribute("totalDuration", 60); // 60 minutes
            
            // Add tips
            model.addAttribute("task1Tips", getTask1Tips());
            model.addAttribute("task2Tips", getTask2Tips());
            
            return "writing-test-page";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading test: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Submit writing test for AI evaluation
     * 
     * @param testId Test identifier
     * @param task1Answer Task 1 answer
     * @param task2Answer Task 2 answer
     * @param timeSpent Time spent in seconds
     * @param session HTTP Session
     * @param model Spring Model
     * @return Redirect to result page
     */
    @PostMapping("/test/{testId}/submit")
    public String submitWritingTest(
            @PathVariable String testId,
            @RequestParam String task1Answer,
            @RequestParam String task2Answer,
            @RequestParam(required = false, defaultValue = "0") int timeSpent,
            HttpSession session,
            Model model) {
        
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
                model.addAttribute("errors", errors);
                return "redirect:/writing/test/" + testId + "?error=insufficient_words";
            }
            
            // Generate unique submission ID
            String submissionId = UUID.randomUUID().toString();
            
            // Store submission
            Map<String, Object> submission = new HashMap<>();
            submission.put("testId", testId);
            submission.put("submissionId", submissionId);
            submission.put("task1Answer", task1Answer);
            submission.put("task2Answer", task2Answer);
            submission.put("task1Words", task1Words);
            submission.put("task2Words", task2Words);
            submission.put("timeSpent", timeSpent);
            submission.put("submittedAt", LocalDateTime.now());
            submission.put("status", "processing");
            
            session.setAttribute("writing_submission_" + submissionId, submission);
            
            // Clear drafts after submission
            clearDraft(session, testId, "task1");
            clearDraft(session, testId, "task2");
            
            // TODO: Queue for AI evaluation
            // aiEvaluationService.evaluateWriting(submissionId, task1Answer, task2Answer);
            
            System.out.println("Writing test submitted: " + testId);
            System.out.println("Submission ID: " + submissionId);
            System.out.println("Task 1 words: " + task1Words);
            System.out.println("Task 2 words: " + task2Words);
            
            return "redirect:/writing/result/" + submissionId;
            
        } catch (Exception e) {
            model.addAttribute("error", "Submission failed: " + e.getMessage());
            return "redirect:/writing/test/" + testId + "?error=submission_failed";
        }
    }

    /**
     * Display test result with detailed feedback
     * 
     * @param submissionId Submission identifier
     * @param model Spring Model
     * @param session HTTP Session
     * @return writing-result.html template
     */
    @GetMapping("/result/{submissionId}")
    public String writingResult(
            @PathVariable String submissionId,
            Model model,
            HttpSession session) {
        
        try {
            // Retrieve submission from session
            @SuppressWarnings("unchecked")
            Map<String, Object> submission = (Map<String, Object>) 
                session.getAttribute("writing_submission_" + submissionId);
            
            if (submission == null) {
                model.addAttribute("error", "Submission not found");
                return "error-404";
            }
            
            String testId = (String) submission.get("testId");
            String status = (String) submission.get("status");
            
            model.addAttribute("pageTitle", "Writing Test Result");
            model.addAttribute("testId", testId);
            model.addAttribute("submissionId", submissionId);
            model.addAttribute("status", status);
            
            // Add submission details
            model.addAttribute("task1Words", submission.get("task1Words"));
            model.addAttribute("task2Words", submission.get("task2Words"));
            model.addAttribute("timeSpent", submission.get("timeSpent"));
            model.addAttribute("submittedAt", submission.get("submittedAt"));
            
            // TODO: Fetch actual result from database after AI evaluation
            // For now, generate mock result
            if ("processing".equals(status)) {
                model.addAttribute("processing", true);
                model.addAttribute("message", "Your test is being evaluated by AI. Please wait...");
            } else {
                // Generate mock result
                Map<String, Object> result = generateMockResult(submission);
                model.addAttribute("result", result);
                model.addAttribute("task1Score", result.get("task1Score"));
                model.addAttribute("task2Score", result.get("task2Score"));
                model.addAttribute("overallScore", result.get("overallScore"));
                
                // Detailed criteria scores
                model.addAttribute("taskAchievement", result.get("taskAchievement"));
                model.addAttribute("coherenceCohesion", result.get("coherenceCohesion"));
                model.addAttribute("lexicalResource", result.get("lexicalResource"));
                model.addAttribute("grammaticalRange", result.get("grammaticalRange"));
                
                // Feedback
                model.addAttribute("task1Feedback", result.get("task1Feedback"));
                model.addAttribute("task2Feedback", result.get("task2Feedback"));
                model.addAttribute("strengths", result.get("strengths"));
                model.addAttribute("improvements", result.get("improvements"));
                
                // Answers
                model.addAttribute("task1Answer", submission.get("task1Answer"));
                model.addAttribute("task2Answer", submission.get("task2Answer"));
            }
            
            return "writing-result";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading result: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Auto-save draft (AJAX endpoint)
     * 
     * @param testId Test identifier
     * @param task Task number (task1 or task2)
     * @param content Draft content
     * @param session HTTP Session
     * @return JSON response
     */
    @PostMapping("/test/{testId}/autosave")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autoSaveDraft(
            @PathVariable String testId,
            @RequestParam String task,
            @RequestParam String content,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (content.length() > MAX_DRAFT_SIZE) {
                response.put("status", "error");
                response.put("message", "Content too large");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save draft to session
            String draftKey = "draft_" + testId + "_" + task;
            session.setAttribute(draftKey, content);
            session.setAttribute(draftKey + "_timestamp", System.currentTimeMillis());
            
            // Calculate word count
            int wordCount = countWords(content);
            
            response.put("status", "saved");
            response.put("timestamp", System.currentTimeMillis());
            response.put("wordCount", wordCount);
            response.put("message", "Draft saved successfully");
            
            System.out.println("Auto-saved " + task + " for test: " + testId + " (" + wordCount + " words)");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get word count (AJAX endpoint)
     * 
     * @param text Text to count
     * @return JSON with word count
     */
    @PostMapping("/api/wordcount")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWordCount(@RequestParam String text) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int wordCount = countWords(text);
            int charCount = text.length();
            
            response.put("words", wordCount);
            response.put("characters", charCount);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check grammar (AJAX endpoint)
     * 
     * @param text Text to check
     * @return JSON with grammar suggestions
     */
    @PostMapping("/api/grammar-check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkGrammar(@RequestParam String text) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: Integrate with grammar checking API (LanguageTool, Grammarly API, etc.)
            List<Map<String, String>> suggestions = performBasicGrammarCheck(text);
            
            response.put("status", "success");
            response.put("suggestions", suggestions);
            response.put("count", suggestions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get vocabulary suggestions (AJAX endpoint)
     * 
     * @param word Word to get suggestions for
     * @return JSON with vocabulary suggestions
     */
    @PostMapping("/api/vocabulary-suggest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVocabularySuggestions(@RequestParam String word) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: Integrate with thesaurus API
            List<String> synonyms = getBasicSynonyms(word);
            
            response.put("status", "success");
            response.put("word", word);
            response.put("synonyms", synonyms);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Analyze text complexity (AJAX endpoint)
     * 
     * @param text Text to analyze
     * @return JSON with complexity analysis
     */
    @PostMapping("/api/analyze-complexity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyzeTextComplexity(@RequestParam String text) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int wordCount = countWords(text);
            int sentenceCount = countSentences(text);
            double avgWordsPerSentence = sentenceCount > 0 ? (double) wordCount / sentenceCount : 0;
            int complexWords = countComplexWords(text);
            
            response.put("status", "success");
            response.put("wordCount", wordCount);
            response.put("sentenceCount", sentenceCount);
            response.put("avgWordsPerSentence", Math.round(avgWordsPerSentence * 10) / 10.0);
            response.put("complexWords", complexWords);
            response.put("readabilityLevel", calculateReadabilityLevel(wordCount, sentenceCount, complexWords));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Count words in text
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Count sentences in text
     */
    private int countSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] sentences = text.split("[.!?]+");
        return sentences.length;
    }

    /**
     * Count complex words (words with 3+ syllables)
     */
    private int countComplexWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.split("\\s+");
        int count = 0;
        for (String word : words) {
            if (word.length() > 8) { // Simple heuristic
                count++;
            }
        }
        return count;
    }

    /**
     * Calculate readability level
     */
    private String calculateReadabilityLevel(int words, int sentences, int complexWords) {
        if (sentences == 0) return "N/A";
        
        double avgWordsPerSentence = (double) words / sentences;
        double complexityRatio = (double) complexWords / words;
        
        if (avgWordsPerSentence > 25 || complexityRatio > 0.2) {
            return "Advanced";
        } else if (avgWordsPerSentence > 20 || complexityRatio > 0.15) {
            return "Intermediate";
        } else {
            return "Basic";
        }
    }

    /**
     * Track test view
     */
    private void trackTestView(String testId, HttpSession session) {
        String sessionKey = "viewed_writing_" + testId;
        if (session.getAttribute(sessionKey) == null) {
            session.setAttribute(sessionKey, LocalDateTime.now());
            System.out.println("Tracking view for writing test: " + testId);
        }
    }

    /**
     * Load draft from session
     */
    private String loadDraft(HttpSession session, String testId, String task) {
        String draftKey = "draft_" + testId + "_" + task;
        Object draft = session.getAttribute(draftKey);
        return draft != null ? draft.toString() : "";
    }

    /**
     * Clear draft from session
     */
    private void clearDraft(HttpSession session, String testId, String task) {
        String draftKey = "draft_" + testId + "_" + task;
        session.removeAttribute(draftKey);
        session.removeAttribute(draftKey + "_timestamp");
    }

    /**
     * Get test questions (mock data)
     */
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

    /**
     * Get Task 1 tips
     */
    private List<String> getTask1Tips() {
        return Arrays.asList(
            "Spend about 20 minutes on this task",
            "Write at least 150 words",
            "Include an overview of the main trends",
            "Compare key features and data",
            "Use appropriate vocabulary for describing data"
        );
    }

    /**
     * Get Task 2 tips
     */
    private List<String> getTask2Tips() {
        return Arrays.asList(
            "Spend about 40 minutes on this task",
            "Write at least 250 words",
            "Include a clear introduction and conclusion",
            "Support your arguments with examples",
            "Present a balanced view before giving your opinion"
        );
    }

    /**
     * Generate mock tests list
     */
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
                sortedTests.sort(Comparator.comparing((Map<String, Object> t) -> (LocalDateTime) t.get("createdAt")).reversed());
                break;
        }
        
        return sortedTests;
    }

    /**
     * Perform basic grammar check
     */
    private List<Map<String, String>> performBasicGrammarCheck(String text) {
        List<Map<String, String>> suggestions = new ArrayList<>();
        
        // Simple checks (in production, use proper grammar checking API)
        if (text.contains("  ")) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("type", "spacing");
            suggestion.put("message", "Extra space detected");
            suggestion.put("suggestion", "Remove extra spaces");
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }

    /**
     * Get basic synonyms
     */
    private List<String> getBasicSynonyms(String word) {
        // TODO: Integrate with thesaurus API
        // For now, return mock data
        Map<String, List<String>> synonymsMap = new HashMap<>();
        synonymsMap.put("good", Arrays.asList("excellent", "great", "fine", "pleasant"));
        synonymsMap.put("bad", Arrays.asList("poor", "terrible", "awful", "unpleasant"));
        synonymsMap.put("important", Arrays.asList("significant", "crucial", "vital", "essential"));
        
        return synonymsMap.getOrDefault(word.toLowerCase(), Collections.emptyList());
    }

    /**
     * Generate mock result
     */
    private Map<String, Object> generateMockResult(Map<String, Object> submission) {
        Map<String, Object> result = new HashMap<>();
        
        // Scores
        result.put("task1Score", 7.0);
        result.put("task2Score", 7.5);
        result.put("overallScore", 7.5);
        
        // Criteria scores
        result.put("taskAchievement", 7.5);
        result.put("coherenceCohesion", 7.0);
        result.put("lexicalResource", 7.5);
        result.put("grammaticalRange", 8.0);
        
        // Feedback
        result.put("task1Feedback", 
            "You have adequately covered all requirements of the task. The overview clearly identifies the main trends. " +
            "Data comparisons are appropriate and well-supported.");
        
        result.put("task2Feedback",
            "Your essay presents a balanced discussion of both views with a clear personal opinion. " +
            "Arguments are well-developed with relevant examples. Good use of cohesive devices throughout.");
        
        result.put("strengths", Arrays.asList(
            "Clear organization and structure",
            "Good range of vocabulary",
            "Effective use of complex sentences",
            "Well-developed arguments with examples"
        ));
        
        result.put("improvements", Arrays.asList(
            "Use more varied sentence structures in Task 1",
            "Add more specific examples in Task 2",
            "Improve paragraphing in some sections",
            "Check for minor spelling errors"
        ));
        
        return result;
    }
}