package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.dto.speaking.QuestionQueueItemDTO;
import com.ieltsgrading.ielts_evaluator.dto.speaking.SpeakingTestFullDTO;
import com.ieltsgrading.ielts_evaluator.dto.speaking.TestListItemDTO;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestQuestion;
import com.ieltsgrading.ielts_evaluator.service.SpeakingTestService;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingTestQuestionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/speaking")
public class SpeakingTestController {

    private final SpeakingTestService testService;
    private final SpeakingTestQuestionRepository questionRepository;

    private static final String SESSION_QUESTION_QUEUE = "currentQuestionQueue";
    private static final String SESSION_CURRENT_INDEX = "currentQuestionIndex";
    private static final String SESSION_CURRENT_TEST_ID = "currentTestId";

    @Autowired
    public SpeakingTestController(SpeakingTestService testService,
                                  SpeakingTestQuestionRepository questionRepository) {
        this.testService = testService;
        this.questionRepository = questionRepository;
    }

    // --- 1. ENDPOINT: LIST ALL TESTS (GET /speaking/tests) ---
    @GetMapping("/tests")
    public String speakingTests(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/require-login?redirect=/speaking/tests";
        }

        List<TestListItemDTO> testList = testService.findAllTestListItems();

        model.addAttribute("pageTitle", "Speaking Tests List");
        model.addAttribute("user", user);
        model.addAttribute("testCount", testList.size());
        model.addAttribute("testList", testList);

        return "speaking/speaking-tests";
    }

    // --- 2. ENDPOINT: START/SETUP THE TEST (GET /speaking/start/{id}) ---
    @GetMapping("/start/{id}")
    public String startTest(@PathVariable Integer id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/require-login?redirect=/speaking/start/" + id;
        }

        List<QuestionQueueItemDTO> queue = testService.buildQuestionQueue(id);

        if (queue == null || queue.isEmpty()) { // FIX: Added null check for queue
            model.addAttribute("message", "Test contains no practice questions.");
            return "error";
        }

        session.setAttribute(SESSION_QUESTION_QUEUE, queue);
        session.setAttribute(SESSION_CURRENT_INDEX, 0);
        session.setAttribute(SESSION_CURRENT_TEST_ID, id);

        Integer firstQuestionId = queue.get(0).getQuestionId();
        return "redirect:/speaking/practice/" + firstQuestionId;
    }


    // --- 3. ENDPOINT: QUESTION PRACTICE PAGE (GET /speaking/practice/{questionId}) ---
    @GetMapping("/practice/{questionId}")
    public String getQuestionPage(
            @PathVariable Integer questionId,
            Model model,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/require-login?redirect=/speaking/practice/" + questionId;
        }

        // FIX: Check for session state integrity immediately
        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        if (queue == null || queue.isEmpty()) {
            // If queue is missing, force a restart from the test list
            return "redirect:/speaking/tests";
        }

        // 1. Fetch the question text using the Repository
        SpeakingTestQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found in database."));

        // 2. Find the index of the current question in the queue (failsafe check)
        int currentQueueIndex = -1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getQuestionId().equals(questionId)) {
                currentQueueIndex = i;
                break;
            }
        }

        if (currentQueueIndex == -1) {
            // If the question ID is valid but doesn't belong to the current session queue
            return "redirect:/speaking/tests";
        }

        // 1. Calculate the boolean value and store it in a local variable
        boolean hasNext = currentQueueIndex < queue.size() - 1;

        // 2. Prepare Model for View
        model.addAttribute("pageTitle", question.getPartNumber() + " Practice");
        model.addAttribute("questionText", question.getQuestionText());

        // Navigation data
        model.addAttribute("hasNext", hasNext);

        // 3. Use the local boolean variable 'hasNext' in the conditional statement
        model.addAttribute("nextQuestionId", hasNext ? queue.get(currentQueueIndex + 1).getQuestionId() : null);

        // Update session index to match the current page index for robustness
        session.setAttribute(SESSION_CURRENT_INDEX, currentQueueIndex);

        return "speaking/speaking-question-practice";
    }


    // --- 4. ENDPOINT: NAVIGATION/SUBMIT ANSWER (POST /speaking/next) ---
    @PostMapping("/next")
    public String nextQuestion(HttpSession session, @RequestParam String answerUrl) {

        // FIX: Re-check session state before proceeding
        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        Integer currentIndex = (Integer) session.getAttribute(SESSION_CURRENT_INDEX);

        if (queue == null || currentIndex == null) {
            return "redirect:/speaking/tests";
        }

        // TODO: 1. SAVE THE ANSWER: (Implementation needed here)

        // 2. Advance the index
        int nextIndex = currentIndex + 1;
        session.setAttribute(SESSION_CURRENT_INDEX, nextIndex);

        if (nextIndex < queue.size()) {
            // Redirect to the next question
            Integer nextQuestionId = queue.get(nextIndex).getQuestionId();
            return "redirect:/speaking/practice/" + nextQuestionId;
        } else {
            // Test finished
            session.removeAttribute(SESSION_QUESTION_QUEUE);
            session.removeAttribute(SESSION_CURRENT_INDEX);
            session.removeAttribute(SESSION_CURRENT_TEST_ID);
            return "redirect:/speaking/review";
        }
    }
}