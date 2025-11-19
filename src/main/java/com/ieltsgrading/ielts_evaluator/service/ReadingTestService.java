package com.ieltsgrading.ielts_evaluator.service;

import com.ieltsgrading.ielts_evaluator.dto.reading.ReadingResultDetailDTO;
import com.ieltsgrading.ielts_evaluator.dto.reading.ReadingSubmissionDTO;
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
import com.ieltsgrading.ielts_evaluator.dto.gemini.GeminiRequest;
import com.ieltsgrading.ielts_evaluator.dto.gemini.GeminiResponse;
import com.ieltsgrading.ielts_evaluator.dto.reading.ReviewResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ReadingTestService {

    // Constants for Retry Logic (Kept for the future bulk review method)
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Value("${gemini.api.key}")
    private String GEMINI_API_KEY;

    // Define the Gemini model URL constants (Kept for the future bulk review method)
    private final String GEMINI_MODEL = "gemini-2.5-flash";
    private final String GENERATE_CONTENT_URL = "https://generativelanguage.googleapis.com/v1/models/" + GEMINI_MODEL + ":generateContent";

    @Autowired private ReadingUserAnswerRepository answerRepository;
    @Autowired private ReadingQuestionRepository questionRepository;
    @Autowired private ReadingTestRepository readingTestRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    // --- 1. NEW METHOD: Get Comprehensive Test Review ---

    // --- 1. NEW METHOD: Get Comprehensive Test Review ---

    public ReviewResponseDTO getTestReview(int testId) {

        List<ReadingResultDetailDTO> incorrectAnswers = getPendingResults(testId);

        if (incorrectAnswers.isEmpty()) {
            return new ReviewResponseDTO();
        }

        ReadingTest test = readingTestRepository.findById(testId).orElseThrow(
                () -> new RuntimeException("Test not found for ID: " + testId)
        );

        List<ReadingPassage> passages = new ArrayList<>(test.getPassages());
        String passageContent = passages.isEmpty() ? "Passage content unavailable." : passages.get(0).getPassageText();

        // 2. Build the "Senior Examiner" Prompt
        StringBuilder promptBuilder = new StringBuilder();

        // --- PERSONA & TONE ---
        promptBuilder.append("Act as a strict, professional Senior IELTS Examiner. Analyze the student's incorrect answers below.");
        promptBuilder.append("Your tone should be constructive, academic, and direct. Avoid fluff or generic encouragement.\n\n");

        // --- FORMATTING INSTRUCTIONS (CRITICAL FOR WEB DISPLAY) ---
        promptBuilder.append("IMPORTANT FORMATTING RULES:\n");
        promptBuilder.append("1. Return ONLY a raw JSON object.\n");
        promptBuilder.append("2. The output must match this schema: { overviewSummary, vocabularyWeaknesses, questionTypeInsights, strategyRecommendations }.\n");
        promptBuilder.append("3. Since this will be displayed in HTML, use HTML tags like <b> for emphasis and <br> for line breaks inside the JSON strings. Do NOT use markdown.\n\n");

        // --- CATEGORY DEFINITIONS ---
        promptBuilder.append("FILL THE CATEGORIES AS FOLLOWS:\n");

        promptBuilder.append("- 'overviewSummary': A 2-sentence high-level assessment of the student's reading level based on these errors.\n");

        promptBuilder.append("- 'vocabularyWeaknesses': Identify 3 specific words or phrases from the passage that caused the errors (synonyms the student missed). ");
        promptBuilder.append("Format as a list: '• <b>Word</b>: Definition/Context<br>'.\n");

        promptBuilder.append("- 'questionTypeInsights': Group the errors by question type (e.g., True/False, Matching Headings). Explain the specific logic trap the student fell into. ");
        promptBuilder.append("Format as: '<b>Type Name</b>: Specific advice...<br>'.\n");

        promptBuilder.append("- 'strategyRecommendations': Provide exactly 3 actionable bullet points for improvement. Focus on scanning, skimming, or keyword matching. ");
        promptBuilder.append("Format as: '1. <b>Action</b>: Detail...<br>'.\n\n");

        // --- DATA INJECTION ---
        promptBuilder.append("--- PASSAGE CONTEXT ---\n").append(passageContent).append("\n\n");
        promptBuilder.append("--- INCORRECT ANSWERS ---\n");

        for (ReadingResultDetailDTO result : incorrectAnswers) {
            promptBuilder.append(String.format(
                    "Q%d: User Answer: '%s' | Correct Answer: '%s' | Question Text: '%s'\n",
                    result.getQuestionOrder(),
                    result.getUserResponse(),
                    result.getCorrectAnswer(),
                    result.getQuestionText()
            ));
        }

        // 3. API Call Setup
        String finalPrompt = promptBuilder.toString();
        GeminiRequest requestBody = new GeminiRequest(finalPrompt);

        // ... (Rest of the API call logic remains exactly the same as before) ...
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
                    String rawTextResponse = response.getBody().getCandidates().stream()
                            .findFirst().map(c -> c.getContent().getParts().stream()
                                    .findFirst().map(p -> p.getText())
                                    .orElse("{}"))
                            .orElse("{}");

                    String jsonReviewText = rawTextResponse.trim().replace("```json", "").replace("```", "").trim();

                    // Log the clean JSON to check if the prompt worked
                    System.out.println("DEBUG: Cleaned JSON from Gemini: " + jsonReviewText);

                    try {
                        return objectMapper.readValue(jsonReviewText, ReviewResponseDTO.class);
                    } catch (Exception jsonEx) {
                        System.err.println("JSON MAPPING FAILED: " + jsonEx.getMessage());
                        return new ReviewResponseDTO();
                    }
                }
                // ... (error handling) ...
                break;
            } catch (Exception e) {
                System.err.println("FATAL ERROR: " + e.getMessage());
                break;
            }
        }
        return null;
    }

    // 🛑 REMOVED: public String getGeminiExplanation(...) (The old helper method)
    // 🛑 REMOVED: public String fetchSingleExplanation(...) (The old individual fetch method)

    // --- Existing Service Methods ---

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
                        "" // Pass empty string (no individual explanation status)
                ))
                .collect(Collectors.toList());
    }

    public ReadingTest getTestById(int id) {
        return readingTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reading Test not found with id: " + id));
    }

    @Transactional
    public ModelAndView processAndGradeSubmission(ReadingSubmissionDTO submissionDTO) {

        int testId = submissionDTO.getTestId();
        int userId = 1;

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
                    "" // Pass empty string for explanation
            ));
        }

        answerRepository.saveAll(answersToStore);

        ModelAndView mav = new ModelAndView("reading-result");
        mav.addObject("totalQuestions", allQuestions.size());
        mav.addObject("score", correctCount);
        mav.addObject("submissionResults", submissionResults);

        String testName = "Test Results";
        Optional<ReadingTest> test = readingTestRepository.findById(testId);
        if (test.isPresent()) {
            testName = test.get().getTestName();
        }
        mav.addObject("testName", testName);

        return mav;
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
}