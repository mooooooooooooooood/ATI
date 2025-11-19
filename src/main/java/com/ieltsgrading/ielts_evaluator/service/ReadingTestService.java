package com.ieltsgrading.ielts_evaluator.service;



import com.ieltsgrading.ielts_evaluator.dto.gemini.GeminiRequest;
import com.ieltsgrading.ielts_evaluator.dto.gemini.GeminiResponse;
import com.ieltsgrading.ielts_evaluator.dto.reading.ReadingResultDetailDTO;
import com.ieltsgrading.ielts_evaluator.dto.reading.ReadingSubmissionDTO;
import com.ieltsgrading.ielts_evaluator.dto.reading.ReviewResponseDTO;
import com.ieltsgrading.ielts_evaluator.model.reading.ReadingPassage;
import com.ieltsgrading.ielts_evaluator.model.reading.ReadingQuestion;
import com.ieltsgrading.ielts_evaluator.model.reading.ReadingTest;
import com.ieltsgrading.ielts_evaluator.model.reading.ReadingUserAnswer;
import com.ieltsgrading.ielts_evaluator.repository.reading.ReadingQuestionRepository;
import com.ieltsgrading.ielts_evaluator.repository.reading.ReadingTestRepository;
import com.ieltsgrading.ielts_evaluator.repository.reading.ReadingUserAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDateTime; // Needed for date
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ReadingTestService {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Value("${gemini.api.key}")
    private String GEMINI_API_KEY;

    private final String GEMINI_MODEL = "gemini-2.5-flash";
    private final String GENERATE_CONTENT_URL = "https://generativelanguage.googleapis.com/v1/models/" + GEMINI_MODEL + ":generateContent";

    @Autowired private ReadingUserAnswerRepository answerRepository;
    @Autowired private ReadingQuestionRepository questionRepository;
    @Autowired private ReadingTestRepository readingTestRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    // --- 1. BULK REVIEW METHOD (Keep this, it works!) ---
    public ReviewResponseDTO getTestReview(int testId) {
        List<ReadingResultDetailDTO> incorrectAnswers = getPendingResults(testId);

        if (incorrectAnswers.isEmpty()) {
            ReviewResponseDTO perfect = new ReviewResponseDTO();
            perfect.setOverviewSummary("<b>Perfect Score!</b> No errors found to analyze.");
            return perfect;
        }

        ReadingTest test = readingTestRepository.findById(testId).orElseThrow(
                () -> new RuntimeException("Test not found for ID: " + testId)
        );

        List<ReadingPassage> passages = new ArrayList<>(test.getPassages());
        String passageContent = passages.isEmpty() ? "Unavailable" : passages.get(0).getPassageText();

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Act as a Senior IELTS Examiner. Analyze these incorrect answers.\n");
        promptBuilder.append("IMPORTANT: Return ONLY a raw JSON object matching this schema: { overviewSummary, vocabularyWeaknesses, questionTypeInsights, strategyRecommendations }.\n");
        promptBuilder.append("Use HTML tags (<b>, <br>) for formatting.\n\n");
        promptBuilder.append("--- PASSAGE ---\n").append(passageContent).append("\n\n");
        promptBuilder.append("--- ERRORS ---\n");

        for (ReadingResultDetailDTO result : incorrectAnswers) {
            promptBuilder.append(String.format("Q%d: User:'%s', Correct:'%s', Type:%d\n",
                    result.getQuestionOrder(), result.getUserResponse(), result.getCorrectAnswer(), result.getQuestionTypeId()));
        }

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


    // --- 2. CORE GRADING LOGIC (UPDATED WITH BAND SCORE) ---

    @Transactional
    public ModelAndView processAndGradeSubmission(ReadingSubmissionDTO submissionDTO) {

        int testId = submissionDTO.getTestId();
        int userId = 1; // Placeholder

        List<ReadingQuestion> allQuestions = questionRepository.findAllByTestId(testId);
        Map<Integer, String> userResponsesMap = submissionDTO.getAnswers().stream()
                .collect(Collectors.toMap(
                        dto -> dto.getQuestionId(),
                        dto -> dto.getUserResponse() != null ? dto.getUserResponse() : ""
                ));

        List<ReadingUserAnswer> answersToStore = new ArrayList<>();
        List<ReadingResultDetailDTO> submissionResults = new ArrayList<>();

        int correctCount = 0;

        for (ReadingQuestion question : allQuestions) {
            String rawUserResponse = userResponsesMap.getOrDefault(question.getId(), "");
            String rawCorrectAnswer = question.getCorrectAnswer();
            boolean isCorrect = false;

            if (!rawUserResponse.trim().isEmpty()) {
                String normalizedUserResponse = normalizeAnswer(rawUserResponse);
                String normalizedCorrectAnswer = normalizeAnswer(rawCorrectAnswer);
                isCorrect = normalizedUserResponse.equals(normalizedCorrectAnswer);
            }

            if (isCorrect) {
                correctCount++;
            }

            ReadingUserAnswer userAnswer = new ReadingUserAnswer();
            userAnswer.setUserId(userId);
            userAnswer.setQuestion(question);
            userAnswer.setUserResponse(rawUserResponse.trim());
            userAnswer.setIsCorrect(isCorrect);
            answersToStore.add(userAnswer);

            submissionResults.add(new ReadingResultDetailDTO(
                    question.getId(),
                    question.getTypeId(),
                    question.getQuestionOrder(),
                    question.getQuestionText(),
                    userAnswer.getUserResponse(),
                    rawCorrectAnswer,
                    isCorrect,
                    ""
            ));
        }

        // 1. Save individual answers
        answerRepository.saveAll(answersToStore);

        double bandScore = calculateIELTSBandScore(correctCount);

        // 3. Prepare View
        ModelAndView mav = new ModelAndView("reading-result");
        mav.addObject("totalQuestions", allQuestions.size());
        mav.addObject("score", correctCount);
        mav.addObject("bandScore", bandScore); // Pass band score to view
        mav.addObject("submissionResults", submissionResults);

        String testName = "Test Results";
        Optional<ReadingTest> test = readingTestRepository.findById(testId);
        if (test.isPresent()) {
            testName = test.get().getTestName();
        }
        mav.addObject("testName", testName);

        return mav;
    }

    // --- Helper Methods ---

    private double calculateIELTSBandScore(int rawScore) {
        if (rawScore >= 39) return 9.0;
        if (rawScore >= 37) return 8.5;
        if (rawScore >= 35) return 8.0;
        if (rawScore >= 33) return 7.5;
        if (rawScore >= 30) return 7.0;
        if (rawScore >= 27) return 6.5;
        if (rawScore >= 23) return 6.0;
        if (rawScore >= 19) return 5.5;
        if (rawScore >= 15) return 5.0;
        if (rawScore >= 13) return 4.5;
        if (rawScore >= 10) return 4.0;
        if (rawScore >= 8)  return 3.5;
        if (rawScore >= 6)  return 3.0;
        if (rawScore >= 4)  return 2.5;
        if (rawScore >= 0)  return 2.5;
        return 2.0;
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) return "";
        String normalized = answer.trim();
        normalized = normalized.replaceAll("[\\[\\]]", "");
        normalized = normalized.replaceAll(",+", "");
        normalized = normalized.toLowerCase();
        normalized = normalized.replaceAll("\\s+", " ");
        return normalized.trim();
    }

    public List<ReadingResultDetailDTO> getPendingResults(int testId) {
        List<ReadingUserAnswer> userAnswers = answerRepository.findAllByTestId(testId);
        return userAnswers.stream()
                .filter(answer -> !answer.getIsCorrect())
                .map(answer -> new ReadingResultDetailDTO(
                        answer.getQuestion().getId(),
                        answer.getQuestion().getTypeId(),
                        answer.getQuestion().getQuestionOrder(),
                        answer.getQuestion().getQuestionText(),
                        answer.getUserResponse(),
                        answer.getQuestion().getCorrectAnswer(),
                        answer.getIsCorrect(),
                        ""
                ))
                .collect(Collectors.toList());
    }
}