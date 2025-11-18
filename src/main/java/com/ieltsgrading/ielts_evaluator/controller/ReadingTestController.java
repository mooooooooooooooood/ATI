package com.ieltsgrading.ielts_evaluator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.reading.ReadingSubmissionDTO;
import com.ieltsgrading.ielts_evaluator.dto.reading.ReviewResponseDTO;
import com.ieltsgrading.ielts_evaluator.model.*;
import com.ieltsgrading.ielts_evaluator.model.reading.*;
import com.ieltsgrading.ielts_evaluator.repository.ReadingQuestionRepository;
import com.ieltsgrading.ielts_evaluator.repository.ReadingTestRepository;
import com.ieltsgrading.ielts_evaluator.repository.ReadingUserAnswerRepository;
import com.ieltsgrading.ielts_evaluator.service.ReadingTestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reading/tests") //
public class ReadingTestController {

    @Autowired
    private ReadingTestRepository testRepository;
    private ReadingQuestionRepository readingQuestionRepository;
    private ReadingUserAnswerRepository userAnswerRepository;
    @Autowired
    private ReadingTestService readingTestService;
    private ObjectMapper objectMapper;

    @GetMapping
    public String getAllTests(Model model, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        String newRedirectPath = "/reading/tests"; // Store the new path

        if (user == null) {
            // CHANGE 2: Updated the redirect path to the new base path
            return "redirect:/require-login?redirect=" + newRedirectPath;
        }

        model.addAttribute("pageTitle", "Reading Tests");
        model.addAttribute("user", user);
        model.addAttribute("tests", testRepository.findAll());
        model.addAttribute("testCount", testRepository.count());


        return "reading-tests";
    }


    @GetMapping("/{id}")
    public String getTestDetail(@PathVariable("id") int id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/require-login?redirect=/reading/tests/" + id;
        }

        ReadingTest test = testRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + id));

        // 🔹 Parse JSON options for each question into parsedOptions Map
        ObjectMapper objectMapper = new ObjectMapper();
        for (ReadingPassage passage : test.getPassages()) {
            for (ReadingQuestionGroup group : passage.getQuestionGroups()) {
                for (ReadingQuestion question : group.getQuestions()) {
                    if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                        try {
                            Map<String, String> parsed = objectMapper.readValue(
                                    question.getOptions(),
                                    new TypeReference<Map<String, String>>() {}
                            );
                            question.setParsedOptions(parsed);
                        } catch (Exception e) {
                            System.err.println("Failed to parse options for Q" + question.getId() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }

        // Flatten questions if needed
        List<ReadingQuestion> flattenedQuestions = test.getPassages().stream()
                .flatMap(p -> p.getQuestionGroups().stream())
                .flatMap(g -> g.getQuestions().stream())
                .collect(Collectors.toList());

        model.addAttribute("test", test);
        model.addAttribute("allQuestionsList", flattenedQuestions);

        // Add indexed questions for Thymeleaf iteration
        List<Map<String, Object>> indexedQuestions = new ArrayList<>();
        int globalIndex = 0;
        for (ReadingPassage passage : test.getPassages()) {
            for (ReadingQuestionGroup group : passage.getQuestionGroups()) {
                for (ReadingQuestion question : group.getQuestions()) {
                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put("index", globalIndex);
                    questionData.put("question", question);
                    questionData.put("group", group);
                    questionData.put("passage", passage);
                    indexedQuestions.add(questionData);
                    globalIndex++;
                }
            }
        }
        model.addAttribute("indexedQuestions", indexedQuestions);

        return "test-details"; // Thymeleaf template name
    }

    @Transactional
    public int evaluateAndSaveAnswers(int userId, int testId, Map<Integer, String> userAnswers) {

        // Find all questions belonging to this test
        // NOTE: This relies on the custom query you added to ReadingQuestionRepository
        List<ReadingQuestion> allQuestions = readingQuestionRepository.findAllByTestId(testId);

        int correctCount = 0;

        for (ReadingQuestion question : allQuestions) {
            int questionId = question.getId();

            // Get user's response, normalize it (trimming whitespace)
            String userResponse = userAnswers.getOrDefault(questionId, "").trim();
            String correctAnswer = question.getCorrectAnswer().trim();

            // Score the answer: case-insensitive and whitespace-trimmed comparison
            boolean isCorrect = userResponse.equalsIgnoreCase(correctAnswer);

            if (isCorrect) {
                correctCount++;
            }

            // Save the user's attempt history (stores user's answer and score result)
            ReadingUserAnswer userAnswer = new ReadingUserAnswer();
            userAnswer.setUserId(userId);
            userAnswer.setQuestion(question);
            userAnswer.setUserResponse(userResponse);
            userAnswer.setIsCorrect(isCorrect);

            userAnswerRepository.save(userAnswer);
        }


        return correctCount;
    }
    // 2. IMPLEMENT THE SUBMISSION ENDPOINT (@PostMapping)
    @PostMapping("/submit") // Maps to /reading/tests/submit
    public ModelAndView submitTestAnswers(@ModelAttribute ReadingSubmissionDTO submissionDTO, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            // Security check: Redirect if session expires
            return new ModelAndView("redirect:/require-login?redirect=/reading/tests/" + submissionDTO.getTestId());
        }

        // Delegate all core logic (saving answers, scoring, calling API) to the Service layer
        ModelAndView mav = readingTestService.processAndGradeSubmission(submissionDTO);

        // ⭐ CRITICAL FIX: Ensure the testId is added to the model for the Thymeleaf template to access.
        mav.addObject("testId", submissionDTO.getTestId());

        return mav;
    }

    // 3. ASYNCHRONOUS BULK REVIEW ENDPOINT
    @PostMapping("/get-test-review/{testId}")
    @ResponseBody
    public ReviewResponseDTO getTestReview(@PathVariable("testId") int testId) {
        // Calls the service method which performs the single, comprehensive API request
        ReviewResponseDTO review = readingTestService.getTestReview(testId);

        // If the service failed (returns null), return an empty DTO to prevent JSON parsing errors on the client
        if (review == null) {
            return new ReviewResponseDTO();
        }

        return review;
    }
}