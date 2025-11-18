package com.ieltsgrading.ielts_evaluator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.speaking.*;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestQuestion;
import com.ieltsgrading.ielts_evaluator.service.SpeakingTestService;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingTestQuestionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Controller
@RequestMapping("/speaking")
public class SpeakingTestController {

    private final SpeakingTestService testService;
    private final SpeakingTestQuestionRepository questionRepository;

    // Session Constants
    private static final String SESSION_QUESTION_QUEUE = "currentQuestionQueue";
    private static final String SESSION_CURRENT_INDEX = "currentQuestionIndex";
    private static final String SESSION_CURRENT_TEST_ID = "currentTestId";
    private static final String SESSION_COLLECTED_ANSWERS = "collectedAnswers";

    @Autowired
    public SpeakingTestController(SpeakingTestService testService,
                                  SpeakingTestQuestionRepository questionRepository) {
        this.testService = testService;
        this.questionRepository = questionRepository;
    }

    // --- 1. LIST ALL TESTS ---
    @GetMapping("/tests")
    public String speakingTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) { return "redirect:/require-login?redirect=/speaking/tests"; }
        List<TestListItemDTO> testList = testService.findAllTestListItems();
        model.addAttribute("pageTitle", "Speaking Tests List");
        model.addAttribute("user", user);
        model.addAttribute("testCount", testList.size());
        model.addAttribute("testList", testList);
        return "speaking/speaking-tests";
    }

    // --- 2. START TEST ---
    @GetMapping("/start/{id}")
    public String startTest(@PathVariable Integer id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) { return "redirect:/require-login?redirect=/speaking/start/" + id; }

        List<QuestionQueueItemDTO> queue = testService.buildQuestionQueue(id);

        if (queue == null || queue.isEmpty()) {
            model.addAttribute("message", "Test contains no practice questions.");
            return "error";
        }

        session.setAttribute(SESSION_QUESTION_QUEUE, queue);
        session.setAttribute(SESSION_CURRENT_INDEX, 0);
        session.setAttribute(SESSION_CURRENT_TEST_ID, id);
        session.setAttribute(SESSION_COLLECTED_ANSWERS, new ArrayList<UserAnswerDTO>());

        System.out.println("--- DEBUG: Test " + id + " STARTED.");

        Integer firstQuestionId = queue.get(0).getQuestionId();
        return "redirect:/speaking/practice/" + firstQuestionId;
    }

    // --- 3. PRACTICE PAGE ---
    @GetMapping("/practice/{questionId}")
    public String getQuestionPage(@PathVariable Integer questionId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) { return "redirect:/require-login?redirect=/speaking/practice/" + questionId; }

        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        if (queue == null || queue.isEmpty()) { return "redirect:/speaking/tests"; }

        SpeakingTestQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found in database."));

        int currentQueueIndex = -1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getQuestionId().equals(questionId)) {
                currentQueueIndex = i;
                break;
            }
        }

        if (currentQueueIndex == -1) { return "redirect:/speaking/tests"; }
        boolean hasNext = currentQueueIndex < queue.size() - 1;

        model.addAttribute("pageTitle", question.getPartNumber() + " Practice");
        model.addAttribute("questionText", question.getQuestionText());
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("nextQuestionId", hasNext ? queue.get(currentQueueIndex + 1).getQuestionId() : null);
        session.setAttribute(SESSION_CURRENT_INDEX, currentQueueIndex);

        return "speaking/speaking-question-practice";
    }

    // --- 4. SAVE ANSWER ---
    @PostMapping("/next")
    public String nextQuestion(HttpSession session, @RequestParam String answerUrl) {
        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        Integer currentIndex = (Integer) session.getAttribute(SESSION_CURRENT_INDEX);
        List<UserAnswerDTO> collectedAnswers = (List<UserAnswerDTO>) session.getAttribute(SESSION_COLLECTED_ANSWERS);

        if (queue == null || currentIndex == null || collectedAnswers == null || currentIndex >= queue.size()) {
            return "redirect:/speaking/tests";
        }

        QuestionQueueItemDTO currentQuestion = queue.get(currentIndex);

        UserAnswerDTO userAnswer = new UserAnswerDTO();
        userAnswer.setQuestionId(currentQuestion.getQuestionId());
        userAnswer.setPartNumber(currentQuestion.getPartNumber());
        userAnswer.setQuestionText(currentQuestion.getQuestionText());
        userAnswer.setRecordedAudioUrl(answerUrl);

        collectedAnswers.add(userAnswer);
        System.out.println("--- DEBUG: Q" + currentQuestion.getQuestionId() + " SAVED. URL: " + answerUrl);

        int nextIndex = currentIndex + 1;
        session.setAttribute(SESSION_CURRENT_INDEX, nextIndex);

        if (nextIndex < queue.size()) {
            Integer nextQuestionId = queue.get(nextIndex).getQuestionId();
            return "redirect:/speaking/practice/" + nextQuestionId;
        } else {
            return "redirect:/speaking/submit";
        }
    }

    // --- 5. SUBMIT FOR GRADING ---
    @GetMapping("/submit")

    public String submitTest(Model model, HttpSession session) {



        Integer testId = (Integer) session.getAttribute(SESSION_CURRENT_TEST_ID);

        List<UserAnswerDTO> answers = (List<UserAnswerDTO>) session.getAttribute(SESSION_COLLECTED_ANSWERS);



        if (testId == null || answers == null || answers.isEmpty()) {

            System.err.println("--- ERROR: Submission failed. Answers list is EMPTY or NULL.");

            model.addAttribute("errorTitle", "Submission Failed");

            model.addAttribute("message", "No answers were recorded.");

            return "error";

        }



        System.out.println("--- DEBUG: SUBMITTING TEST " + testId + ". Total answers: " + answers.size());



        GradingRequestDTO request = new GradingRequestDTO();

        request.setTestId(testId);

        request.setAnswers(answers);



// 2. Call the external API service

        ResponseEntity<String> response = testService.submitForGrading(request, answers.size() == 1 && answers.get(0).getPartNumber().equals("Part 2"));

        String rawJsonBody = response.getBody();



// Debugging logs from the Service:

        System.out.println("--- DEBUG: API Response Status: " + response.getStatusCodeValue());

        System.out.println("--- DEBUG: Raw JSON Body Received: " + rawJsonBody);



        try {

            ObjectMapper mapper = new ObjectMapper();



// CRITICAL FIX: Step 1 - Parse the outer JSON layer

            Map<String, Object> outerMap = mapper.readValue(rawJsonBody, new TypeReference<Map<String, Object>>() {});



// CRITICAL FIX: Step 2 - Extract the value of the 'message' key (which is a raw JSON String)

            String innerJsonString = (String) outerMap.get("message");



// CRITICAL FIX: Step 3 - Re-parse the inner JSON String into the final structured Map

// This fixes the double-encoding problem.

            Map<String, Object> finalGradingMap = mapper.readValue(innerJsonString, new TypeReference<Map<String, Object>>() {});





// 4. Pass the CLEANED Map to the model

            model.addAttribute("gradingResult", finalGradingMap);



// 5. Clean up the session (critical step after test completion)

            session.removeAttribute(SESSION_QUESTION_QUEUE);

            session.removeAttribute(SESSION_CURRENT_INDEX);

            session.removeAttribute(SESSION_CURRENT_TEST_ID);

            session.removeAttribute(SESSION_COLLECTED_ANSWERS);



// 6. Return the success view

            return "speaking/speaking-review-results";



        } catch (Exception e) {

            System.err.println("Error parsing grading API response: " + e.getMessage());

            e.printStackTrace();



            model.addAttribute("errorTitle", "Failed to Process Grading Results");

            model.addAttribute("message", "A parsing error occurred while processing the server response.");



// NOTE: We pass the whole response body to the error template for inspection

            model.addAttribute("rawResponse", rawJsonBody != null ? rawJsonBody : "API returned null or error.");



            return "error";

        }
    }
}