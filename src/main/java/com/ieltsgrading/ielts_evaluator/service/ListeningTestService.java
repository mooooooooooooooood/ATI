package com.ieltsgrading.ielts_evaluator.service;

import com.fasterxml.jackson.core.type.TypeReference;
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

    private final String GEMINI_MODEL = "gemini-2.5-flash";
    private final String GENERATE_CONTENT_URL = "https://generativelanguage.googleapis.com/v1/models/" + GEMINI_MODEL + ":generateContent";

    @Autowired private ListeningUserAnswerRepository answerRepository;
    @Autowired private ListeningQuestionRepository questionRepository;
    @Autowired private ListeningTestRepository listeningTestRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    // --- 1. CORE GRADING LOGIC ---

    @Transactional
    public ModelAndView processAndGradeSubmission(ListeningSubmissionDTO submissionDTO, int userId) {

        int testId = submissionDTO.getTestId();

        // 1. Fetch all questions for the test
        List<ListeningQuestion> allQuestions = questionRepository.findAllByGroup_Section_Test_TestId(testId);
        // 2. Map user responses for quick lookup
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

            // Simple grading logic: normalize and compare
            if (!rawUserResponse.trim().isEmpty()) {
                String normalizedUserResponse = normalizeAnswer(rawUserResponse);
                String normalizedCorrectAnswer = normalizeAnswer(rawCorrectAnswer);

                // IMPORTANT: For Listening, case/pluralization variations are usually NOT accepted
                // But for basic matching, we stick to normalized string equality.
                isCorrect = normalizedUserResponse.equals(normalizedCorrectAnswer);
            }

            if (isCorrect) {
                correctCount++;
            }

            // 3. Prepare to save individual answer
            ListeningUserAnswer userAnswer = new ListeningUserAnswer();
            userAnswer.setUserId(userId);
            userAnswer.setQuestion(question);
            userAnswer.setUserResponse(rawUserResponse.trim());
            userAnswer.setIsCorrect(isCorrect);
            answersToStore.add(userAnswer);

            // Prepare result for display
            submissionResults.add(new ListeningResultDetailDTO(
                    question.getQuestionId(),
                    // Pass the String representation using the helper method we added to the Entity
                    question.getQuestionType(),
                    question.getQuestionOrder(),
                    question.getQuestionText(),
                    userAnswer.getUserResponse(),
                    rawCorrectAnswer,
                    isCorrect,
                    "",
                    question.getGroup().getImageUrl()
            ));
        }

        // 4. Save individual answers to the database
        answerRepository.saveAll(answersToStore);

        double bandScore = calculateIELTSListeningBandScore(correctCount);

        // 5. Prepare View
        ModelAndView mav = new ModelAndView("listening-result");
        mav.addObject("totalQuestions", allQuestions.size());
        mav.addObject("score", correctCount);
        mav.addObject("bandScore", bandScore);
        mav.addObject("submissionResults", submissionResults);

        String testName = "Test Results";
        Optional<ListeningTest> test = listeningTestRepository.findById(testId);
        if (test.isPresent()) {
            testName = test.get().getTestName();
        }
        mav.addObject("testName", testName);

        return mav;
    }

    // --- 2. GEMINI REVIEW METHOD (Adapted for Listening Transcripts) ---

    public ReviewResponseDTO getTestReview(int testId) {
        List<ListeningResultDetailDTO> incorrectAnswers = getPendingResults(testId);

        if (incorrectAnswers.isEmpty()) {
            ReviewResponseDTO perfect = new ReviewResponseDTO();
            perfect.setOverviewSummary("<b>Perfect Score!</b> No errors found to analyze.");
            return perfect;
        }

        ListeningTest test = listeningTestRepository.findById(testId).orElseThrow(
                () -> new RuntimeException("Test not found for ID: " + testId)
        );

        // For Listening, get the full transcript of all sections
        String fullTranscript = test.getSections().stream()
                .sorted( (s1, s2) -> Integer.compare(s1.getSectionOrder(), s2.getSectionOrder()))
                .map(s -> "--- SECTION " + s.getSectionOrder() + " ---\n" + s.getTranscript())
                .collect(Collectors.joining("\n\n"));

        if (fullTranscript.trim().isEmpty()) {
            ReviewResponseDTO noTranscript = new ReviewResponseDTO();
            noTranscript.setOverviewSummary("<b>Error:</b> Full audio transcript unavailable for analysis.");
            return noTranscript;
        }


        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Act as a Senior IELTS Examiner. Analyze these incorrect answers from a Listening test.\n");
        promptBuilder.append("IMPORTANT: Return ONLY a raw JSON object matching this schema: { overviewSummary, vocabularyWeaknesses, questionTypeInsights, strategyRecommendations }.\n");
        promptBuilder.append("Use HTML tags (<b>, <br>) for formatting.\n\n");
        promptBuilder.append("--- FULL TRANSCRIPT ---\n").append(fullTranscript).append("\n\n");
        promptBuilder.append("--- ERRORS ---\n");

        for (ListeningResultDetailDTO result : incorrectAnswers) {
            promptBuilder.append(String.format("Q%d: User:'%s', Correct:'%s', Type:%s\n",
                    result.getQuestionOrder(), result.getUserResponse(), result.getCorrectAnswer(), result.getQuestionType()));
        }

        // --- API Call Logic (Identical to Reading Service) ---
        GeminiRequest requestBody = new GeminiRequest(promptBuilder.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeminiRequest> requestEntity = new HttpEntity<>(requestBody, headers);
        String finalApiUrl = GENERATE_CONTENT_URL + "?key=" + this.GEMINI_API_KEY;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                        finalApiUrl, HttpMethod.POST, requestEntity, GeminiResponse.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String rawText = response.getBody().getCandidates().stream()
                            .findFirst().map(c -> c.getContent().getParts().stream()
                                    .findFirst().map(p -> p.getText()).orElse("{}")).orElse("{}");

                    String cleanJson = rawText.trim().replace("```json", "").replace("```", "").trim();
                    try {
                        return objectMapper.readValue(cleanJson, ReviewResponseDTO.class);
                    } catch (Exception e) {
                        return new ReviewResponseDTO(); // Parse error fallback
                    }
                }
                break;
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode().value() == 503 && attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) {}
                    continue;
                }
                break;
            } catch (Exception e) { break; }
        }
        return null;
    }


    // --- Helper Methods ---

    /**
     * Calculates the IELTS Band Score based on the raw Listening score (0-40).
     * @param rawScore The number of correct answers.
     * @return The corresponding band score.
     */
    private double calculateIELTSListeningBandScore(int rawScore) {
        // Scoring table for IELTS Listening (Academic/General)
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
        return 1.0; // Should not be reached
    }

    /**
     * Normalizes the user's answer and the correct answer for grading comparison.
     * @param answer The answer string.
     * @return Normalized string.
     */
    private String normalizeAnswer(String answer) {
        if (answer == null) return "";
        // Remove extra spaces and convert to lowercase for comparison robustness
        String normalized = answer.trim().replaceAll("\\s+", " ").toLowerCase();

        // Remove common non-essential punctuation
        normalized = normalized.replaceAll("[()]", "");

        return normalized.trim();
    }

    /**
     * Retrieves the list of incorrect answers (used for the Gemini review prompt).
     */
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
                        answer.getQuestion().getGroup().getImageUrl()

                ))
                .collect(Collectors.toList());
    }
}