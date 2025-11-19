package com.ieltsgrading.ielts_evaluator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.speaking.*;
import com.ieltsgrading.ielts_evaluator.model.SpeakingSubmission;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.service.SpeakingTestService;
import com.ieltsgrading.ielts_evaluator.service.TestSubmissionService;
import com.ieltsgrading.ielts_evaluator.service.UserService;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingTestQuestionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/speaking")
public class SpeakingTestController {
    @Autowired
    private TestSubmissionService testSubmissionService;

    @Autowired
    private UserService userService;

    private final SpeakingTestService testService;
    private final SpeakingTestQuestionRepository questionRepository;

    // --- Session Constants ---
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

    // --- 1. ENDPOINT: LIST ALL TESTS ---
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

    // --- 2. ENDPOINT: START/SETUP THE TEST ---
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

        session.setAttribute(SESSION_QUESTION_QUEUE, queue);
        session.setAttribute(SESSION_CURRENT_INDEX, 0);
        session.setAttribute(SESSION_CURRENT_TEST_ID, id);
        session.setAttribute(SESSION_COLLECTED_ANSWERS, new ArrayList<UserAnswerDTO>());

        Integer firstQuestionId = queue.get(0).getQuestionId();
        return "redirect:/speaking/practice/" + firstQuestionId;
    }

    // --- 3. ENDPOINT: QUESTION PRACTICE PAGE ---
    @GetMapping("/practice/{questionId}")
    public String getQuestionPage(
            @PathVariable Integer questionId,
            Model model,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/require-login?redirect=/speaking/practice/" + questionId;
        }

        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        if (queue == null || queue.isEmpty()) {
            return "redirect:/speaking/tests";
        }

        // Find the current item in the queue (handles Part 2 placeholder ID)
        QuestionQueueItemDTO currentQueueItem = null;
        int currentQueueIndex = -1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getQuestionId().equals(questionId)) {
                currentQueueIndex = i;
                currentQueueItem = queue.get(i);
                break;
            }
        }

        if (currentQueueIndex == -1 || currentQueueItem == null) {
            return "redirect:/speaking/tests";
        }
        boolean hasNext = currentQueueIndex < queue.size() - 1;

        model.addAttribute("pageTitle", currentQueueItem.getPartNumber() + " Practice");
        model.addAttribute("questionText", currentQueueItem.getQuestionText());
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("nextQuestionId", hasNext ? queue.get(currentQueueIndex + 1).getQuestionId() : null);
        session.setAttribute(SESSION_CURRENT_INDEX, currentQueueIndex);

        return "speaking/speaking-question-practice";
    }

    // --- 4. ENDPOINT: SAVE ANSWER ---
    @PostMapping("/next")
    public String nextQuestion(HttpSession session, @RequestParam String answerUrl) {

        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        Integer currentIndex = (Integer) session.getAttribute(SESSION_CURRENT_INDEX);
        List<UserAnswerDTO> collectedAnswers = (List<UserAnswerDTO>) session.getAttribute(SESSION_COLLECTED_ANSWERS);

        if (queue == null || currentIndex == null || collectedAnswers == null || currentIndex >= queue.size()) {
            return "redirect:/speaking/tests";
        }

        QuestionQueueItemDTO currentQuestion = queue.get(currentIndex);

        // ✅ Chuyển URL thành FILE PATH (quan trọng!)
        String filePath = answerUrl.startsWith("/") ? answerUrl.substring(1) : answerUrl;
        // Ví dụ: "/uploads/audio/abc.webm" -> "uploads/audio/abc.webm"

        UserAnswerDTO userAnswer = new UserAnswerDTO();
        userAnswer.setQuestionId(currentQuestion.getQuestionId());
        userAnswer.setPartNumber(currentQuestion.getPartNumber());
        userAnswer.setQuestionText(currentQuestion.getQuestionText());
        userAnswer.setRecordedAudioUrl(filePath); // ✅ Lưu file path, không phải URL

        collectedAnswers.add(userAnswer);

        System.out.println("✅ Saved answer for question " + currentQuestion.getQuestionId());
        System.out.println("   File path: " + filePath);

        int nextIndex = currentIndex + 1;
        session.setAttribute(SESSION_CURRENT_INDEX, nextIndex);

        if (nextIndex < queue.size()) {
            Integer nextQuestionId = queue.get(nextIndex).getQuestionId();
            return "redirect:/speaking/practice/" + nextQuestionId;
        } else {
            // ✅ Đã hết câu hỏi -> chuyển sang submit
            return "redirect:/speaking/submit";
        }
    }

    // ====================================================================
    // --- MANUAL STRING PARSING HELPERS (UNCHANGED) ---
    // ====================================================================

    private Map<String, Object> manualExtractSection(String fullJson, String sectionKey) {
        Map<String, Object> result = new HashMap<>();

        String searchKey = "\"" + sectionKey + "\":";
        int startIdx = fullJson.indexOf(searchKey);
        if (startIdx == -1)
            return result;

        int blockStart = fullJson.indexOf("{", startIdx);
        if (blockStart == -1)
            return result;

        String sectionBlock = fullJson.substring(blockStart);

        Pattern bandPattern = Pattern.compile("\"band\"\\s*:\\s*([0-9.]+)");
        Matcher bandMatcher = bandPattern.matcher(sectionBlock);
        if (bandMatcher.find()) {
            try {
                result.put("band", Double.parseDouble(bandMatcher.group(1)));
            } catch (NumberFormatException e) {
                result.put("band", 0.0);
            }
        }

        result.put("assessment", extractTextValue(sectionBlock, "assessment"));
        result.put("evaluation", extractTextValue(sectionBlock, "evaluation"));
        result.put("improvement_suggestions", extractTextValue(sectionBlock, "improvement_suggestions"));

        return result;
    }

    private String extractTextValue(String source, String key) {
        Pattern keyPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"");
        Matcher matcher = keyPattern.matcher(source);

        if (!matcher.find()) {
            return "";
        }

        int startIdx = matcher.end();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (int i = startIdx; i < source.length(); i++) {
            char c = source.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
            } else {
                if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString().replace("\\n", "\n").replaceAll("(?m)^[ \t]*\r?\n", "");
    }

    private Double extractOverallBand(String json) {
        Pattern p = Pattern.compile("\"overall_band\"\\s*:\\s*([0-9.]+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    // --- 5. ENDPOINT: SUBMIT TEST ---
    @GetMapping("/submit")
    public String submitTest(Model model, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        Integer testId = (Integer) session.getAttribute(SESSION_CURRENT_TEST_ID);
        List<UserAnswerDTO> answers = (List<UserAnswerDTO>) session.getAttribute(SESSION_COLLECTED_ANSWERS);
        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);

        if (testId == null || answers == null || answers.isEmpty()) {
            model.addAttribute("errorTitle", "Submission Failed");
            model.addAttribute("message", "No answers were recorded.");
            return "error";
        }

        try {
            System.out.println("========================================");
            System.out.println("🎤 SUBMITTING SPEAKING TEST");
            System.out.println("   User: " + user.getName());
            System.out.println("   Test ID: " + testId);
            System.out.println("   Total answers: " + answers.size());
            System.out.println("========================================");

            // ✅ Convert answers to Map<questionId, filePath>
            Map<Integer, String> audioFilePaths = new HashMap<>();
            for (UserAnswerDTO answer : answers) {
                audioFilePaths.put(answer.getQuestionId(), answer.getRecordedAudioUrl());
            }

            // ✅ CREATE SUBMISSION trong database
            SpeakingSubmission submission = testSubmissionService.createSpeakingSubmission(
                    user,
                    testId,
                    queue, // ✅ Truyền questions để lưu vào JSON
                    audioFilePaths // ✅ Truyền file paths
            );

            System.out.println("✅ Created submission: " + submission.getSubmissionUuid());

            // ✅ GỌI API CHẤM ĐIỂM BẤT ĐỒNG BỘ
            testSubmissionService.processSpeakingSubmissionAsync(submission.getSubmissionUuid());

            System.out.println("✅ Started async processing");

            // ✅ CLEAN UP SESSION
            session.removeAttribute(SESSION_QUESTION_QUEUE);
            session.removeAttribute(SESSION_CURRENT_INDEX);
            session.removeAttribute(SESSION_CURRENT_TEST_ID);
            session.removeAttribute(SESSION_COLLECTED_ANSWERS);

            // ✅ REDIRECT VỀ DASHBOARD với flag submitted=true
            return "redirect:/dashboard?submitted=true";

        } catch (Exception e) {
            System.err.println("❌ Error submitting test: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("errorTitle", "Submission Error");
            model.addAttribute("message", "Failed to submit test: " + e.getMessage());
            return "error";
        }
    }
}