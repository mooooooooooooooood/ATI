package com.ieltsgrading.ielts_evaluator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.gemini.GeminiRequest;
import com.ieltsgrading.ielts_evaluator.dto.gemini.GeminiResponse;
import com.ieltsgrading.ielts_evaluator.dto.listening.ListeningResultDetailDTO;
import com.ieltsgrading.ielts_evaluator.dto.listening.ListeningSubmissionDTO;
import com.ieltsgrading.ielts_evaluator.dto.listening.ReviewResponseDTO;
import com.ieltsgrading.ielts_evaluator.model.listening.ListeningQuestion;
import com.ieltsgrading.ielts_evaluator.model.listening.ListeningTest;
import com.ieltsgrading.ielts_evaluator.model.listening.ListeningUserAnswer;
import com.ieltsgrading.ielts_evaluator.repository.listening.ListeningQuestionRepository;
import com.ieltsgrading.ielts_evaluator.repository.listening.ListeningTestRepository;
import com.ieltsgrading.ielts_evaluator.repository.listening.ListeningUserAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class ListeningTestService {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Value("${gemini.api.key}")
    private String GEMINI_API_KEY;

    // Use a slightly more creative temperature if possible, but standard model is fine
    private final String GEMINI_MODEL = "gemini-2.5-flash";
    private final String GENERATE_CONTENT_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent";

    @Autowired private ListeningUserAnswerRepository answerRepository;
    @Autowired private ListeningQuestionRepository questionRepository;
    @Autowired private ListeningTestRepository listeningTestRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- 1. CORE GRADING LOGIC (Kept same as your code) ---

    @Transactional
    public ModelAndView processAndGradeSubmission(ListeningSubmissionDTO submissionDTO, int userId) {
        int testId = submissionDTO.getTestId();
        List<ListeningQuestion> allQuestions = questionRepository.findAllByGroup_Section_Test_TestId(testId);

        Map<Integer, String> userResponsesMap = submissionDTO.getAnswers().stream()
                .collect(Collectors.toMap(
                        dto -> dto.getQuestionId(),
                        dto -> dto.getUserResponse() != null ? dto.getUserResponse() : ""
                ));

        List<ListeningUserAnswer> answersToStore = new ArrayList<>();
        List<ListeningResultDetailDTO> submissionResults = new ArrayList<>();
        int correctCount = 0;

        for (ListeningQuestion question : allQuestions) {
            String rawUserResponse = userResponsesMap.getOrDefault(question.getQuestionId(), "");
            String rawCorrectAnswer = question.getCorrectAnswer();
            boolean isCorrect = false;

            if (!rawUserResponse.trim().isEmpty()) {
                String normalizedUserResponse = normalizeAnswer(rawUserResponse);
                String normalizedCorrectAnswer = normalizeAnswer(rawCorrectAnswer);
                isCorrect = normalizedUserResponse.equals(normalizedCorrectAnswer);
            }

            if (isCorrect) correctCount++;

            ListeningUserAnswer userAnswer = new ListeningUserAnswer();
            userAnswer.setUserId(userId);
            userAnswer.setQuestion(question);
            userAnswer.setUserResponse(rawUserResponse.trim());
            userAnswer.setIsCorrect(isCorrect);
            answersToStore.add(userAnswer);

            submissionResults.add(new ListeningResultDetailDTO(
                    question.getQuestionId(),
                    question.getQuestionType(),
                    question.getQuestionOrder(),
                    question.getQuestionText(),
                    userAnswer.getUserResponse(),
                    rawCorrectAnswer,
                    isCorrect,
                    "",
                    question.getGroup().getImageUrl(),
                    getSpecificListeningTask(question)
            ));
        }

        answerRepository.saveAll(answersToStore);
        double bandScore = calculateIELTSListeningBandScore(correctCount);

        ModelAndView mav = new ModelAndView("listening-result");
        mav.addObject("totalQuestions", allQuestions.size());
        mav.addObject("score", correctCount);
        mav.addObject("bandScore", bandScore);
        mav.addObject("submissionResults", submissionResults);

        String testName = "Test Results";
        Optional<ListeningTest> test = listeningTestRepository.findById(testId);
        if (test.isPresent()) testName = test.get().getTestName();
        mav.addObject("testName", testName);

        return mav;
    }

    // --- 2. GEMINI REVIEW METHOD (FIXED FOR LISTENING CONTEXT) ---

    // --- REPLACE THIS METHOD IN ListeningTestService.java ---

    public ReviewResponseDTO getTestReview(int testId) {
        List<ListeningResultDetailDTO> incorrectAnswers = getPendingResults(testId);

        if (incorrectAnswers.isEmpty()) {
            ReviewResponseDTO perfect = new ReviewResponseDTO();
            perfect.setOverviewSummary("<b>Perfect Score!</b> Your listening skills are excellent. No errors found to analyze.");
            return perfect;
        }

        ListeningTest test = listeningTestRepository.findById(testId).orElseThrow(
                () -> new RuntimeException("Test not found for ID: " + testId)
        );

        // Build transcript
        String fullTranscript = test.getSections().stream()
                .sorted((s1, s2) -> Integer.compare(s1.getSectionOrder(), s2.getSectionOrder()))
                .map(s -> "--- AUDIO SECTION " + s.getSectionOrder() + " ---\n" + s.getTranscript())
                .collect(Collectors.joining("\n\n"));

        if (fullTranscript.trim().isEmpty()) {
            return new ReviewResponseDTO();
        }

        StringBuilder promptBuilder = new StringBuilder();

        // --- IMPROVED PROMPT TO PREVENT JSON ERRORS ---
        promptBuilder.append("You are an expert IELTS LISTENING Coach.\n");
        promptBuilder.append("Analyze the student's mistakes based on the provided AUDIO SCRIPT.\n\n");

        promptBuilder.append("CRITICAL JSON FORMATTING RULES:\n");
        promptBuilder.append("1. Return ONLY valid JSON. No markdown, no code blocks.\n");
        promptBuilder.append("2. Do NOT use double quotes (\") inside your content strings unless you escape them (\\\").\n");
        promptBuilder.append("3. For HTML attributes, use SINGLE QUOTES (e.g., <span style='color:red'>), NOT double quotes.\n");
        promptBuilder.append("4. Output schema: { \"overviewSummary\": \"...\", \"vocabularyWeaknesses\": \"...\", \"questionTypeInsights\": \"...\", \"strategyRecommendations\": \"...\" }\n\n");

        promptBuilder.append("--- AUDIO SCRIPT ---\n").append(fullTranscript).append("\n\n");
        promptBuilder.append("--- STUDENT ERRORS ---\n");

        for (ListeningResultDetailDTO result : incorrectAnswers) {
            promptBuilder.append(String.format("Q%d (%s) - User wrote: '%s' | Correct: '%s'\nPrompt: %s\n\n",
                    result.getQuestionOrder(),
                    result.getListeningTaskDescription(),
                    result.getUserResponse(),
                    result.getCorrectAnswer(),
                    result.getQuestionText()));
        }

        // --- API Call Logic ---
        GeminiRequest requestBody = new GeminiRequest(promptBuilder.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeminiRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        // Ensure you are using the URL you set up (v1 or v1beta)
        String finalApiUrl = GENERATE_CONTENT_URL + "?key=" + this.GEMINI_API_KEY;

        try {
            ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                    finalApiUrl, HttpMethod.POST, requestEntity, GeminiResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String rawText = response.getBody().getCandidates().stream()
                        .findFirst().map(c -> c.getContent().getParts().stream()
                                .findFirst().map(p -> p.getText()).orElse("{}")).orElse("{}");

                // Cleanup Markdown if Gemini ignores instruction
                String cleanJson = rawText.trim();
                if (cleanJson.startsWith("```json")) {
                    cleanJson = cleanJson.replace("```json", "").replace("```", "");
                } else if (cleanJson.startsWith("```")) {
                    cleanJson = cleanJson.replace("```", "");
                }
                cleanJson = cleanJson.trim();

                try {
                    // DEBUG PRINT: This will help you see the JSON in the console if it fails
                    // System.out.println("DEBUG GEMINI JSON: " + cleanJson);

                    return objectMapper.readValue(cleanJson, ReviewResponseDTO.class);
                } catch (Exception e) {
                    System.err.println("JSON Parsing Failed. Raw content was:");
                    System.err.println(cleanJson); // Print the bad JSON to logs
                    e.printStackTrace();

                    // Return a friendly error object to the frontend
                    ReviewResponseDTO errorDto = new ReviewResponseDTO();
                    errorDto.setOverviewSummary("<span style='color:red'>The AI analysis could not be processed due to a formatting error. Check server logs for 'JSON Parsing Failed'.</span>");
                    return errorDto;
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini API Call Failed: " + e.getMessage());
        }
        return new ReviewResponseDTO();
    }
    // --- Helper Methods ---

    private double calculateIELTSListeningBandScore(int rawScore) {
        if (rawScore >= 39) return 9.0;
        if (rawScore >= 37) return 8.5;
        if (rawScore >= 35) return 8.0;
        if (rawScore >= 32) return 7.5;
        if (rawScore >= 30) return 7.0;
        if (rawScore >= 27) return 6.5;
        if (rawScore >= 23) return 6.0;
        if (rawScore >= 20) return 5.5;
        if (rawScore >= 16) return 5.0;
        if (rawScore >= 13) return 4.5;
        if (rawScore >= 10) return 4.0;
        if (rawScore >= 8)  return 3.5;
        if (rawScore >= 6)  return 3.0;
        if (rawScore >= 4)  return 2.5;
        if (rawScore > 0)  return 2.0;
        return 1.0;
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) return "";
        String normalized = answer.trim().replaceAll("\\s+", " ").toLowerCase();
        normalized = normalized.replaceAll("[()]", ""); // Remove parentheses
        normalized = normalized.replaceAll("['.,]", ""); // Remove punctuation marks
        return normalized.trim();
    }

    private String getSpecificListeningTask(ListeningQuestion question) {
        int qNum = question.getQuestionOrder();
        // These descriptions help the AI understand the LISTENING context
        if (qNum >= 1 && qNum <= 10) return "Section 1: Conversation (Social/Survival) - Form Filling";
        if (qNum >= 11 && qNum <= 20) return "Section 2: Monologue (General Interest) - Map/Labeling";
        if (qNum >= 21 && qNum <= 30) return "Section 3: Conversation (Academic/Training) - Multiple Choice";
        if (qNum >= 31 && qNum <= 40) return "Section 4: Monologue (Academic Lecture) - Note Completion";
        return "Listening Task: " + question.getQuestionType();
    }

    public List<ListeningResultDetailDTO> getPendingResults(int testId) {
        List<ListeningUserAnswer> userAnswers = answerRepository.findAllByQuestion_Group_Section_Test_TestId(testId);
        return userAnswers.stream()
                .filter(answer -> answer.getIsCorrect() != null && !answer.getIsCorrect())
                .map(answer -> new ListeningResultDetailDTO(
                        answer.getQuestion().getQuestionId(),
                        answer.getQuestion().getQuestionType(),
                        answer.getQuestion().getQuestionOrder(),
                        answer.getQuestion().getQuestionText(),
                        answer.getUserResponse(),
                        answer.getQuestion().getCorrectAnswer(),
                        answer.getIsCorrect(),
                        "",
                        answer.getQuestion().getGroup().getImageUrl(),
                        getSpecificListeningTask(answer.getQuestion())
                ))
                .collect(Collectors.toList());
    }
}