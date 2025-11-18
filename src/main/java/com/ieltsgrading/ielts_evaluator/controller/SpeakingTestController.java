package com.ieltsgrading.ielts_evaluator.controller;

import com.ieltsgrading.ielts_evaluator.dto.speaking.QuestionQueueItemDTO;
import com.ieltsgrading.ielts_evaluator.dto.speaking.TestListItemDTO;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.model.ITestSubmission;
import com.ieltsgrading.ielts_evaluator.model.SpeakingSubmission;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingSubmissionDetail;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestQuestion;
import com.ieltsgrading.ielts_evaluator.service.SpeakingTestService;
import com.ieltsgrading.ielts_evaluator.service.TestSubmissionService;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingTestQuestionRepository;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingTestRepository;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingSubmissionDetailRepository;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingSubmissionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ✅ FIXED: Speaking Test Controller with proper submission handling
 */
@Controller
@RequestMapping("/speaking")
public class SpeakingTestController {

    private final SpeakingTestService testService;
    private final SpeakingTestQuestionRepository questionRepository;
    private final SpeakingSubmissionRepository submissionRepository;

    @Autowired
    private TestSubmissionService submissionService;

    @Autowired
    private SpeakingSubmissionDetailRepository speakingDetailRepo;

    @Autowired
    private SpeakingTestRepository speakingTestRepo;

    private static final String SESSION_QUESTION_QUEUE = "currentQuestionQueue";
    private static final String SESSION_CURRENT_INDEX = "currentQuestionIndex";
    private static final String SESSION_CURRENT_TEST_ID = "currentTestId";
    private static final String SESSION_ANSWERS = "speakingAnswers";

    @Autowired
    public SpeakingTestController(
            SpeakingTestService testService,
            SpeakingTestQuestionRepository questionRepository,
            SpeakingSubmissionRepository submissionRepository) {
        this.testService = testService;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
    }

    /**
     * Display list of speaking tests
     */
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

    /**
     * Start a speaking test
     */
    @GetMapping("/start/{id}")
    public String startTest(@PathVariable Integer id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/require-login?redirect=/speaking/start/" + id;
        }

        List<QuestionQueueItemDTO> queue = testService.buildQuestionQueue(id);
        if (queue == null || queue.isEmpty()) {
            model.addAttribute("message", "Test contains no practice questions.");
            return "error";
        }

        // Initialize session data
        session.setAttribute(SESSION_QUESTION_QUEUE, queue);
        session.setAttribute(SESSION_CURRENT_INDEX, 0);
        session.setAttribute(SESSION_CURRENT_TEST_ID, id);
        session.setAttribute(SESSION_ANSWERS, new HashMap<Integer, String>());

        Integer firstQuestionId = queue.get(0).getQuestionId();
        return "redirect:/speaking/practice/" + firstQuestionId;
    }

    /**
     * Display a single question page
     */
    @GetMapping("/practice/{questionId}")
    public String getQuestionPage(
            @PathVariable Integer questionId,
            Model model,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/require-login?redirect=/speaking/practice/" + questionId;
        }

        @SuppressWarnings("unchecked")
        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        if (queue == null || queue.isEmpty()) {
            return "redirect:/speaking/tests";
        }

        com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestQuestion question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        int currentQueueIndex = -1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getQuestionId().equals(questionId)) {
                currentQueueIndex = i;
                break;
            }
        }

        if (currentQueueIndex == -1) {
            return "redirect:/speaking/tests";
        }

        boolean hasNext = currentQueueIndex < queue.size() - 1;

        model.addAttribute("pageTitle", question.getPartNumber() + " Practice");
        model.addAttribute("questionText", question.getQuestionText());
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("nextQuestionId", hasNext ? queue.get(currentQueueIndex + 1).getQuestionId() : null);

        session.setAttribute(SESSION_CURRENT_INDEX, currentQueueIndex);

        return "speaking/speaking-question-practice";
    }

    /**
     * ✅ FIXED: Handle next question or finish test
     */
    @PostMapping("/next")
    public String nextQuestion(
            HttpSession session,
            @RequestParam String answerUrl) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/require-login";
        }

        @SuppressWarnings("unchecked")
        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        Integer currentIndex = (Integer) session.getAttribute(SESSION_CURRENT_INDEX);
        Integer testId = (Integer) session.getAttribute(SESSION_CURRENT_TEST_ID);
        @SuppressWarnings("unchecked")
        Map<Integer, String> answers = (Map<Integer, String>) session.getAttribute(SESSION_ANSWERS);

        if (queue == null || currentIndex == null || answers == null) {
            return "redirect:/speaking/tests";
        }

        // Save current answer
        Integer currentQuestionId = queue.get(currentIndex).getQuestionId();
        answers.put(currentQuestionId, answerUrl);
        session.setAttribute(SESSION_ANSWERS, answers);

        int nextIndex = currentIndex + 1;

        if (nextIndex < queue.size()) {
            // ✅ Move to next question
            session.setAttribute(SESSION_CURRENT_INDEX, nextIndex);
            Integer nextQuestionId = queue.get(nextIndex).getQuestionId();
            return "redirect:/speaking/practice/" + nextQuestionId;
        } else {
            // ✅ TEST FINISHED - Create submission and redirect to dashboard
            try {
                System.out.println("========================================");
                System.out.println("🎤 SPEAKING TEST COMPLETED");
                System.out.println("   User: " + user.getName());
                System.out.println("   Test ID: " + testId);
                System.out.println("   Questions answered: " + answers.size());
                System.out.println("========================================");

                // Create submission with status "pending"
                SpeakingSubmission submission = submissionService.createSpeakingSubmission(
                        user, testId, queue, answers);

                System.out.println("✅ Speaking submission created: " + submission.getSubmissionUuid());
                System.out.println("   Status: " + submission.getStatus());

                // Clear session
                session.removeAttribute(SESSION_QUESTION_QUEUE);
                session.removeAttribute(SESSION_CURRENT_INDEX);
                session.removeAttribute(SESSION_CURRENT_TEST_ID);
                session.removeAttribute(SESSION_ANSWERS);

                // ✅ Start async processing (will change status to "processing" then
                // "completed")
                submissionService.processSpeakingSubmissionAsync(submission.getSubmissionUuid());

                System.out.println("🔄 Async processing started, redirecting to dashboard...");

                // ✅ Redirect to dashboard WITH QUERY PARAM to show notification
                return "redirect:/dashboard?submitted=true";

            } catch (Exception e) {
                System.err.println("❌ Error creating submission: " + e.getMessage());
                e.printStackTrace();
                return "redirect:/dashboard";
            }

        }
    }

    // ========================================
    // ✅ THÊM VÀO CUỐI CLASS SpeakingTestController
    // ========================================

    /**
     * ✅ Display speaking test result page
     * This endpoint shows the grading results for a completed speaking test
     */
    @GetMapping("/result/{submissionUuid}")
    public String viewSpeakingResult(
            @PathVariable String submissionUuid,
            Model model,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/require-login?redirect=/speaking/result/" + submissionUuid;
        }

        try {
            // Get submission from service
            Optional<ITestSubmission> optSubmission = submissionService.getSubmission(submissionUuid);

            if (optSubmission.isEmpty()) {
                model.addAttribute("error", "Submission not found");
                return "error-404";
            }

            ITestSubmission submission = optSubmission.get();

            // Verify ownership
            if (!submission.getUserId().equals(user.getId())) {
                model.addAttribute("error", "Access denied");
                return "error-403";
            }

            // Verify it's a speaking submission
            if (!"speaking".equals(submission.getTestType())) {
                return "redirect:/result/" + submissionUuid; // Redirect to writing result
            }

            // Cast to SpeakingSubmission
            SpeakingSubmission speakingSubmission = (SpeakingSubmission) submission;

            // Load test details
            Optional<com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTest> testOpt = speakingTestRepo
                    .findById(speakingSubmission.getTestId());

            if (testOpt.isPresent()) {
                speakingSubmission.setTest(testOpt.get());
            }

            // Load detailed feedback if completed
            SpeakingSubmissionDetail detail = null;
            if (speakingSubmission.isCompleted()) {
                Optional<SpeakingSubmissionDetail> detailOpt = speakingDetailRepo
                        .findBySubmission_SubmissionId(speakingSubmission.getSubmissionId());

                if (detailOpt.isPresent()) {
                    detail = detailOpt.get();
                }
            }

            // Add to model
            model.addAttribute("pageTitle", "Speaking Test Result - " +
                    (testOpt.isPresent() ? testOpt.get().getMainTopic() : "Speaking Test"));
            model.addAttribute("submission", speakingSubmission);
            model.addAttribute("speakingDetail", detail);
            model.addAttribute("user", user);

            System.out.println("========================================");
            System.out.println("🎤 VIEWING SPEAKING RESULT");
            System.out.println("   UUID: " + submissionUuid);
            System.out.println("   Status: " + speakingSubmission.getStatus());
            System.out.println("   Score: " + speakingSubmission.getOverallScore());
            System.out.println("========================================");

            return "speaking/speaking-result";

        } catch (Exception e) {
            System.err.println("❌ Error loading speaking result: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading result: " + e.getMessage());
            return "error";
        }
    }
}
