package com.ieltsgrading.ielts_evaluator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieltsgrading.ielts_evaluator.dto.speaking.*;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTest;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestDetail;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestQuestion;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for managing IELTS Speaking Test data and external API communication.
 */
@Service
public class SpeakingTestService {

    private final SpeakingTestRepository testRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${grading.api.base-url}")
    private String gradingApiBaseUrl;

    @Autowired
    public SpeakingTestService(SpeakingTestRepository testRepository, RestTemplate restTemplate) {
        this.testRepository = testRepository;
        this.restTemplate = restTemplate;
    }

    // --- CRUD Methods (unchanged) ---
    
    @Transactional(readOnly = true)
    public List<SpeakingTest> findAllTests() {
        return testRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<SpeakingTest> findTestById(Integer id) {
        return testRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<SpeakingTest> searchTestsByMainTopic(String keyword) {
        return testRepository.findByMainTopicContainingIgnoreCase(keyword);
    }

    @Transactional
    public SpeakingTest saveTest(SpeakingTest test) {
        return testRepository.save(test);
    }

    @Transactional
    public void deleteTest(Integer id) {
        testRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TestListItemDTO> findAllTestListItems() {
        List<SpeakingTest> tests = testRepository.findAll();
        return tests.stream()
                .map(test -> new TestListItemDTO(test.getTestId(), test.getTestDate(), test.getMainTopic()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SpeakingTestFullDTO getFullTestDetails(Integer testId) {
        SpeakingTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));

        SpeakingTestFullDTO dto = new SpeakingTestFullDTO();
        dto.setTestId(test.getTestId());
        dto.setTestDate(test.getTestDate());
        dto.setMainTopic(test.getMainTopic());

        List<String> part1Topics = test.getDetails().stream()
                .filter(d -> d.getPartTopic().equals("Part 1 Topics"))
                .flatMap(d -> List.of(d.getDetailText().split("\\|")).stream())
                .collect(Collectors.toList());

        String part2CueCard = test.getDetails().stream()
                .filter(d -> d.getPartTopic().equals("Part 2 Cue Card"))
                .map(SpeakingTestDetail::getDetailText)
                .findFirst()
                .orElse("Cue Card Not Found");

        dto.setPart1Topics(part1Topics);
        dto.setPart2CueCard(part2CueCard);

        Map<String, List<String>> questionsMap = test.getQuestions().stream()
                .collect(Collectors.groupingBy(
                        SpeakingTestQuestion::getPartNumber,
                        Collectors.mapping(SpeakingTestQuestion::getQuestionText, Collectors.toList())
                ));

        dto.setQuestionsByPart(questionsMap);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<QuestionQueueItemDTO> buildQuestionQueue(Integer testId) {
        SpeakingTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));

        List<QuestionQueueItemDTO> queue = test.getQuestions().stream()
                .map(q -> new QuestionQueueItemDTO(q.getQuestionId(), q.getPartNumber(), q.getQuestionText()))
                .collect(Collectors.toList());

        String part2Text = test.getDetails().stream()
                .filter(d -> d.getPartTopic().equals("Part 2 Cue Card"))
                .map(SpeakingTestDetail::getDetailText)
                .findFirst()
                .orElse("Part 2 Cue Card Not Found");

        QuestionQueueItemDTO part2Item = new QuestionQueueItemDTO();
        part2Item.setQuestionId(-99);
        part2Item.setPartNumber("Part 2");
        part2Item.setQuestionText(part2Text);

        queue.add(part2Item);
        queue.sort(Comparator.comparing(QuestionQueueItemDTO::getPartNumber));

        return queue;
    }

    // --- FIXED: External API Submission Logic ---

    /**
     * Submits the collected answers to the external grading API using multipart/form-data.
     * ✅ FIXED: Now correctly reads files from file system instead of treating URLs as resources
     */
    public ResponseEntity<String> submitForGrading(GradingRequestDTO request, boolean isCueCardTask) {

        // 1. Prepare question texts
        List<String> questionTexts = request.getAnswers().stream()
                .map(UserAnswerDTO::getQuestionText)
                .collect(Collectors.toList());

        // 2. Get file URLs (these are like "/uploads/audio/speaking_xxx.webm")
        List<String> fileUrls = request.getAnswers().stream()
                .map(UserAnswerDTO::getRecordedAudioUrl)
                .collect(Collectors.toList());

        // 3. Serialize questions to JSON string
        String questionsJsonString;
        try {
            questionsJsonString = objectMapper.writeValueAsString(questionTexts);
        } catch (JsonProcessingException e) {
            System.err.println("❌ Error serializing questions: " + e.getMessage());
            return new ResponseEntity<>(
                "{\"error_message\": \"Internal serialization error.\"}",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // 4. Determine topic value based on task type
        String topicValue = isCueCardTask ? "Speaking Part 2" : "Speaking Practice Task";

        // 5. Build multipart form data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Add questions field
        body.add("questions", questionsJsonString);
        
        // Add topic field
        body.add("topic", topicValue);

        // 6. ✅ CRITICAL FIX: Add files from file system, not URLs
        for (int i = 0; i < fileUrls.size(); i++) {
            String fileUrl = fileUrls.get(i);
            
            try {
                // Convert URL to file path
                // fileUrl is like "/uploads/audio/speaking_xxx.webm"
                // We need to convert it to "uploads/audio/speaking_xxx.webm"
                String filePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
                
                File audioFile = new File(filePath);
                
                // Check if file exists
                if (!audioFile.exists()) {
                    System.err.println("❌ File not found: " + audioFile.getAbsolutePath());
                    System.err.println("   Original URL: " + fileUrl);
                    continue;
                }

                // Create FileSystemResource (this is the correct way to upload files)
                FileSystemResource fileResource = new FileSystemResource(audioFile);
                
                // Add file to form data
                body.add("files", fileResource);
                
                System.out.println("✅ Added file to request: " + audioFile.getName() + " (" + audioFile.length() + " bytes)");

            } catch (Exception e) {
                System.err.println("❌ Error adding file " + fileUrl + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 7. Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("ngrok-skip-browser-warning", "true"); // ✅ Required by ngrok

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 8. Determine endpoint
        String endpoint = isCueCardTask ? "/speaking/2" : "/speaking/multi";
        String fullUrl = gradingApiBaseUrl + endpoint;

        // 9. Execute request
        try {
            System.out.println("📤 Submitting to: " + fullUrl);
            System.out.println("   - Questions count: " + questionTexts.size());
            System.out.println("   - Files count: " + fileUrls.size());
            System.out.println("   - Task type: " + (isCueCardTask ? "Part 2" : "Part 1/3"));

            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println("✅ API Response Status: " + response.getStatusCode());
            return response;

        } catch (HttpClientErrorException e) {
            System.err.println("❌ External API HTTP Error (" + e.getStatusCode() + ")");
            System.err.println("   Response: " + e.getResponseBodyAsString());
            
            return new ResponseEntity<>(
                    "{\"error_message\": \"Grading API returned error: " + e.getStatusCode().value() + 
                    "\", \"details\": " + e.getResponseBodyAsString() + "\"}",
                    e.getStatusCode()
            );

        } catch (ResourceAccessException e) {
            System.err.println("❌ Cannot reach grading API server");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   URL: " + fullUrl);
            
            return new ResponseEntity<>(
                    "{\"error_message\": \"Grading API server is unreachable. Please check ngrok.\"}",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during API call");
            e.printStackTrace();
            
            return new ResponseEntity<>(
                    "{\"error_message\": \"Unexpected error: " + e.getMessage() + "\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}