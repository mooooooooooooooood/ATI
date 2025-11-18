package com.ieltsgrading.ielts_evaluator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.speaking.*;
import com.ieltsgrading.ielts_evaluator.model.User;
import com.ieltsgrading.ielts_evaluator.service.SpeakingTestService;
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
import java.util.LinkedHashMap; // Import LinkedHashMap to keep Part order
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/speaking")
public class SpeakingTestController {

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
        if (user == null) { return "redirect:/require-login?redirect=/speaking/tests"; }
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
        if (user == null) { return "redirect:/require-login?redirect=/speaking/practice/" + questionId; }

        List<QuestionQueueItemDTO> queue = (List<QuestionQueueItemDTO>) session.getAttribute(SESSION_QUESTION_QUEUE);
        if (queue == null || queue.isEmpty()) { return "redirect:/speaking/tests"; }

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

        if (currentQueueIndex == -1 || currentQueueItem == null) { return "redirect:/speaking/tests"; }
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

        UserAnswerDTO userAnswer = new UserAnswerDTO();
        userAnswer.setQuestionId(currentQuestion.getQuestionId());
        userAnswer.setPartNumber(currentQuestion.getPartNumber());
        userAnswer.setQuestionText(currentQuestion.getQuestionText());
        userAnswer.setRecordedAudioUrl(answerUrl);

        collectedAnswers.add(userAnswer);

        int nextIndex = currentIndex + 1;
        session.setAttribute(SESSION_CURRENT_INDEX, nextIndex);

        if (nextIndex < queue.size()) {
            Integer nextQuestionId = queue.get(nextIndex).getQuestionId();
            return "redirect:/speaking/practice/" + nextQuestionId;
        } else {
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
        if (startIdx == -1) return result;

        int blockStart = fullJson.indexOf("{", startIdx);
        if (blockStart == -1) return result;

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

        if (!matcher.find()) { return ""; }

        int startIdx = matcher.end();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (int i = startIdx; i < source.length(); i++) {
            char c = source.charAt(i);
            if (escaped) { sb.append(c); escaped = false; }
            else {
                if (c == '\\') { escaped = true; }
                else if (c == '"') { break; }
                else { sb.append(c); }
            }
        }
        return sb.toString().replace("\\n", "\n").replaceAll("(?m)^[ \t]*\r?\n", "");
    }

    private Double extractOverallBand(String json) {
        Pattern p = Pattern.compile("\"overall_band\"\\s*:\\s*([0-9.]+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); } catch (Exception e) { return 0.0; }
        }
        return 0.0;
    }


    // --- 5. ENDPOINT: SUBMIT TEST (UPDATED FOR 3 PARTS WITH PLACEHOLDERS) ---
    // --- 5. ENDPOINT: SUBMIT TEST ---
    @GetMapping("/submit")
    public String submitTest(Model model, HttpSession session) {

        Integer testId = (Integer) session.getAttribute(SESSION_CURRENT_TEST_ID);
        List<UserAnswerDTO> answers = (List<UserAnswerDTO>) session.getAttribute(SESSION_COLLECTED_ANSWERS);

        if (testId == null || answers == null || answers.isEmpty()) {
            model.addAttribute("errorTitle", "Submission Failed");
            model.addAttribute("message", "No answers were recorded.");
            return "error";
        }

        // 1. Group answers
        Map<String, List<UserAnswerDTO>> answersByPart = answers.stream()
                .collect(Collectors.groupingBy(UserAnswerDTO::getPartNumber));

        // 2. Container for all results
        Map<String, Map<String, Object>> allPartsResults = new LinkedHashMap<>();
        String[] parts = {"Part 1", "Part 2", "Part 3"};

        double totalBandSum = 0.0;
        int validPartsCount = 0;

        // 3. Loop through each part
        for (String partName : parts) {
            List<UserAnswerDTO> partAnswers = answersByPart.get(partName);
            if (partAnswers == null || partAnswers.isEmpty()) continue;

            System.out.println("--- Processing " + partName + " ---");

            // Prepare API Request
            GradingRequestDTO partRequest = new GradingRequestDTO();
            partRequest.setTestId(testId);
            partRequest.setAnswers(partAnswers);

            String rawJsonBody = null;
            boolean apiCallSuccess = false;

            try {
                ResponseEntity<String> response = testService.submitForGrading(partRequest, partName.equals("Part 2"));
                rawJsonBody = response.getBody();
                // CRITICAL FIX: Check the status code!
                apiCallSuccess = response.getStatusCode().is2xxSuccessful();
            } catch (Exception e) {
                System.err.println("API Error for " + partName + ": " + e.getMessage());
                apiCallSuccess = false;
            }

            // 4. FORCE PLACEHOLDER IF API FAILED OR BODY IS EMPTY
            if (!apiCallSuccess || rawJsonBody == null || rawJsonBody.trim().isEmpty()) {
                System.err.println("--- API FAILED for " + partName + ". Using Placeholder.");

                String assessmentText = "Grading unavailable for " + partName + ". The grading server is offline or unreachable.";

                rawJsonBody = "{\"message\":\"{\\n" +
                        "  \\\"overall_band\\\": 6.0,\\n" +
                        "  \\\"fluency_and_coherence\\\": {\\n" +
                        "    \\\"band\\\": 6.0,\\n" +
                        "    \\\"assessment\\\": \\\"" + assessmentText + "\\\",\\n" +
                        "    \\\"evaluation\\\": \\\"N/A\\\",\\n" +
                        "    \\\"improvement_suggestions\\\": \\\"Please check ngrok.\\\"\\n" +
                        "  },\\n" +
                        "  \\\"lexical_resource\\\": {\\n" +
                        "    \\\"band\\\": 6.0,\\n" +
                        "    \\\"assessment\\\": \\\"" + assessmentText + "\\\",\\n" +
                        "    \\\"evaluation\\\": \\\"N/A\\\",\\n" +
                        "    \\\"improvement_suggestions\\\": \\\"Please check ngrok.\\\"\\n" +
                        "  },\\n" +
                        "  \\\"grammatical_range_accuracy\\\": {\\n" +
                        "    \\\"band\\\": 6.0,\\n" +
                        "    \\\"assessment\\\": \\\"" + assessmentText + "\\\",\\n" +
                        "    \\\"evaluation\\\": \\\"N/A\\\",\\n" +
                        "    \\\"improvement_suggestions\\\": \\\"Please check ngrok.\\\"\\n" +
                        "  },\\n" +
                        "  \\\"pronunciation\\\": {\\n" +
                        "    \\\"band\\\": 6.0,\\n" +
                        "    \\\"assessment\\\": \\\"" + assessmentText + "\\\",\\n" +
                        "    \\\"evaluation\\\": \\\"N/A\\\",\\n" +
                        "    \\\"improvement_suggestions\\\": \\\"Please check ngrok.\\\"\\n" +
                        "  }\\n" +
                        "}\"}";
            }

            // 5. Parsing Logic (Same as before)
            try {
                String innerJsonString = "";
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, Object> outerMap = mapper.readValue(rawJsonBody, new TypeReference<Map<String, Object>>() {});
                    innerJsonString = (String) outerMap.get("message");
                } catch (Exception outerEx) {
                    int start = rawJsonBody.indexOf("{\"");
                    if (start == -1) start = rawJsonBody.indexOf("{");
                    int end = rawJsonBody.lastIndexOf("}");
                    if (start != -1 && end != -1) innerJsonString = rawJsonBody.substring(start, end+1);
                }

                if (innerJsonString == null) innerJsonString = "";
                innerJsonString = innerJsonString.replaceAll("\\u00A0", " ").trim();

                Double partOverall = extractOverallBand(innerJsonString);

                Map<String, Object> partResultData = new HashMap<>();
                partResultData.put("overall", partOverall);
                partResultData.put("fluency", manualExtractSection(innerJsonString, "fluency_and_coherence"));
                partResultData.put("lexical", manualExtractSection(innerJsonString, "lexical_resource"));
                partResultData.put("grammar", manualExtractSection(innerJsonString, "grammatical_range_accuracy"));
                partResultData.put("pronunciation", manualExtractSection(innerJsonString, "pronunciation"));

                allPartsResults.put(partName, partResultData);

                totalBandSum += partOverall;
                validPartsCount++;

            } catch (Exception e) {
                System.err.println("Error parsing " + partName + ": " + e.getMessage());
            }
        }

        // 6. Final Model Update
        double finalAverageBand = validPartsCount > 0 ? totalBandSum / validPartsCount : 0.0;
        finalAverageBand = Math.round(finalAverageBand * 2) / 2.0;

        model.addAttribute("finalOverallBand", finalAverageBand);
        model.addAttribute("allPartsResults", allPartsResults);

        session.removeAttribute(SESSION_QUESTION_QUEUE);
        session.removeAttribute(SESSION_CURRENT_INDEX);
        session.removeAttribute(SESSION_CURRENT_TEST_ID);
        session.removeAttribute(SESSION_COLLECTED_ANSWERS);

        return "speaking/speaking-review-results";
    }
}