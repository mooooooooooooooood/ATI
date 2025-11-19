package com.ieltsgrading.ielts_evaluator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.listening.ListeningSubmissionDTO;
import com.ieltsgrading.ielts_evaluator.model.User; // Assuming User model is generic
import com.ieltsgrading.ielts_evaluator.model.listening.ListeningQuestion;
import com.ieltsgrading.ielts_evaluator.model.listening.ListeningQuestionGroup;
import com.ieltsgrading.ielts_evaluator.model.listening.ListeningSection;
import com.ieltsgrading.ielts_evaluator.model.listening.ListeningTest;
import com.ieltsgrading.ielts_evaluator.repository.listening.ListeningTestRepository;
import com.ieltsgrading.ielts_evaluator.service.ListeningTestService; // Core business logic
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/listening/tests")
public class ListeningTestController {

    @Autowired
    private ListeningTestRepository testRepository;

    // Note: QuestionRepository is no longer explicitly Autowired here as it's primarily used in the Service layer
    // private ListeningQuestionRepository questionRepository;

    @Autowired
    private ListeningTestService listeningTestService; // Core business logic

    // ObjectMapper is thread-safe and can be final
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ------------------------------------------------------------------
    // 1. ENDPOINT TO LIST ALL AVAILABLE LISTENING TESTS
    // ------------------------------------------------------------------

    @GetMapping
    public String getAllTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        String newRedirectPath = "/listening/tests";
        List<ListeningTest> tests = testRepository.findAll();
        // --- ADD THESE DEBUG LINES ---
        System.out.println("============================================");
        System.out.println("DEBUG: Querying Database...");
        System.out.println("DEBUG: Found " + tests.size() + " items.");
        if (tests.size() > 0) {
            System.out.println("DEBUG: First Item Name: " + tests.get(0).getTestName());
        } else {
            System.out.println("DEBUG: The list is empty!");
        }
        System.out.println("============================================");
        // -----------------------------
        if (user == null) {
            // Redirect if not logged in
            return "redirect:/require-login?redirect=" + newRedirectPath;
        }

        model.addAttribute("pageTitle", "Listening Tests");
        model.addAttribute("user", user);
        model.addAttribute("tests", testRepository.findAll());
        model.addAttribute("testCount", testRepository.count());

        return "listening-tests"; // Thymeleaf template name
    }

    // ------------------------------------------------------------------
    // 2. ENDPOINT TO VIEW A SPECIFIC TEST AND ITS QUESTIONS
    // ------------------------------------------------------------------

    @GetMapping("/{testId}")
    public String getTestDetail(@PathVariable("testId") int testId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/require-login?redirect=/listening/tests/" + testId;
        }

        // Fetch the test with all its nested details (Sections, Groups, Questions)
        // NOTE: The repository must ensure EAGER fetching or use a custom JOIN query.
        ListeningTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Listening Test not found with ID: " + testId));

        // Flatten questions for display and processing
        List<ListeningQuestion> flattenedQuestions = test.getSections().stream()
                .flatMap(s -> s.getQuestionGroups().stream())
                .flatMap(g -> g.getQuestions().stream())
                .peek(question -> {
                    // Parse JSON options into a usable map for the front-end
                    if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                        try {
                            Map<String, String> parsed = objectMapper.readValue(
                                    question.getOptions(),
                                    new TypeReference<Map<String, String>>() {}
                            );

                            // --- FIX: SET THE DATA INTO THE ENTITY ---
                            question.setParsedOptions(parsed);
                            // -----------------------------------------

                        } catch (Exception e) {
                            System.err.println("Failed to parse options for Q" + question.getQuestionId() + ": " + e.getMessage());
                        }
                    }
                })
                // Sorting logic ensures questions appear in the correct order: Section -> Group -> Question
                .sorted(Comparator.comparing((ListeningQuestion q) -> q.getGroup().getSection().getSectionOrder())
                        .thenComparing(q -> q.getGroup().getGroupOrder())
                        .thenComparing(ListeningQuestion::getQuestionOrder))
                .collect(Collectors.toList());

        model.addAttribute("test", test);
        model.addAttribute("allQuestionsList", flattenedQuestions);

        // Map data into a structure suitable for Thymeleaf iteration
        List<Map<String, Object>> indexedQuestions = new ArrayList<>();
        int globalIndex = 0;

        for (ListeningSection section : test.getSections()) {

            // 1. Reset this flag at the start of EVERY section
            boolean isFirstQuestionInSection = true;

            for (ListeningQuestionGroup group : section.getQuestionGroups()) {
                for (ListeningQuestion question : group.getQuestions()) {
                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put("globalIndex", globalIndex + 1);
                    questionData.put("question", question);
                    questionData.put("group", group);
                    questionData.put("section", section);

                    // 2. Set the flag based on our local tracker
                    questionData.put("isStartOfSection", isFirstQuestionInSection);

                    // 3. Immediately set it to false for the rest of the questions in this section
                    isFirstQuestionInSection = false;

                    indexedQuestions.add(questionData);
                    globalIndex++;
                }
            }
        }

        model.addAttribute("indexedQuestions", indexedQuestions);

        return "listening-test-details"; // Thymeleaf template name
    }

    // ------------------------------------------------------------------
    // 3. ENDPOINT TO SUBMIT ALL TEST ANSWERS (FIXED)
    // ------------------------------------------------------------------

    @PostMapping("/submit") // Maps to /listening/tests/submit
    public ModelAndView submitTestAnswers(@ModelAttribute ListeningSubmissionDTO submissionDTO, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            // Security check: Redirect if session expires
            return new ModelAndView("redirect:/require-login?redirect=/listening/tests/" + submissionDTO.getTestId());
        }

        // FIX: Safely convert user.getId() (Long/Integer) to int for the service method signature.
        int userId = user.getId().intValue();

        // Delegate all core logic (saving answers, scoring) to the Service layer
        ModelAndView mav = listeningTestService.processAndGradeSubmission(submissionDTO, userId);

        // Ensure the testId is added to the model for the Thymeleaf template to access.
        mav.addObject("testId", submissionDTO.getTestId());

        // Assuming the service redirects to a review page or returns a model for a results page
        return mav;
    }
}