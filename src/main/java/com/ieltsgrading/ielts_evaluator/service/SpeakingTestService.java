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

import org.springframework.core.io.Resource;

import org.springframework.core.io.UrlResource;

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



import java.net.MalformedURLException;

import java.util.Comparator;

import java.util.List;

import java.util.Map;

import java.util.Optional;

import java.util.UUID;

import java.util.stream.Collectors;



/**

 * Service layer for managing IELTS Speaking Test data and external API communication.

 */

@Service

public class SpeakingTestService {



    private final SpeakingTestRepository testRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();



    @Value("${grading.api.base-url}")

    private String gradingApiBaseUrl;



    @Autowired

    public SpeakingTestService(SpeakingTestRepository testRepository) {

        this.testRepository = testRepository;

    }



// --- CRUD, Retrieval, and Queue Methods (Mostly unchanged) ---



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



    /**

     * Builds a single queue of questions for the test, combining database questions (P1, P3)

     * and the manually constructed Cue Card (P2).

     */

    @Transactional(readOnly = true)

    public List<QuestionQueueItemDTO> buildQuestionQueue(Integer testId) {



        SpeakingTest test = testRepository.findById(testId)

                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));



// 1. Initialize queue with Part 1 and Part 3 questions from the repository

        List<QuestionQueueItemDTO> queue = test.getQuestions().stream()

                .map(q -> new QuestionQueueItemDTO(q.getQuestionId(), q.getPartNumber(), q.getQuestionText()))

                .collect(Collectors.toList());



// 2. Fetch the Part 2 detail text (Cue Card)

        String part2Text = test.getDetails().stream()

                .filter(d -> d.getPartTopic().equals("Part 2 Cue Card"))

                .map(SpeakingTestDetail::getDetailText)

                .findFirst()

                .orElse("Part 2 Cue Card Not Found");



// 3. Manually create the queue item for Part 2

        QuestionQueueItemDTO part2Item = new QuestionQueueItemDTO();

        part2Item.setQuestionId(-99); // Use a non-DB, unique ID for the cue card task

        part2Item.setPartNumber("Part 2");

        part2Item.setQuestionText(part2Text);



// 4. CRITICAL FIX: Add the Part 2 item to the queue!

        queue.add(part2Item);



// 5. Sort the entire queue by Part Number (ensuring Part 1, 2, 3 sequence)

        queue.sort(Comparator.comparing(QuestionQueueItemDTO::getPartNumber));



        return queue;

    }



// --- External API Submission Logic (Unchanged) ---



    /**

     * Submits the collected answers to the external grading API using multipart/form-data.

     */

    public ResponseEntity<String> submitForGrading(GradingRequestDTO request, boolean isCueCardTask) {



// 1. Prepare Data Components

        List<String> questionTexts = request.getAnswers().stream()

                .map(UserAnswerDTO::getQuestionText)

                .collect(Collectors.toList());



        List<String> fileUrls = request.getAnswers().stream()

                .map(UserAnswerDTO::getRecordedAudioUrl)

                .collect(Collectors.toList());



        String questionsJsonString;

        try {

// A. Convert questions list to the required JSON String

            questionsJsonString = objectMapper.writeValueAsString(questionTexts);

        } catch (JsonProcessingException e) {

            System.err.println("Error serializing questions list: " + e.getMessage());

            return new ResponseEntity<>("{\"error_message\": \"Internal serialization error.\"}", HttpStatus.INTERNAL_SERVER_ERROR);

        }



// B. Determine topic value

        String topicValue = isCueCardTask ? "Speaking Part 2" : "Speaking Practice Task";



// 2. Build the MultiValueMap (the multipart/form-data body)

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();



// Field 1: Stringified Questions

        body.add("questions", questionsJsonString);



// Field 2: Topic

        body.add("topic", topicValue);



// Field 3: Files (Convert URLs to Resources)

        for (int i = 0; i < fileUrls.size(); i++) {

            String url = fileUrls.get(i);

            try {

// a. Create a Resource object from the URL string

                Resource resource = new UrlResource(url);



// b. Create specific headers for the file part

                HttpHeaders fileHeader = new HttpHeaders();



// Use a unique name for Content-Disposition

                String filename = "answer_" + UUID.randomUUID().toString() + ".webm";



// The media type should match the audio type uploaded (WebM)

                fileHeader.setContentType(MediaType.valueOf("audio/webm"));



// Set the Content-Disposition (filename is key for multipart validation)

                fileHeader.setContentDispositionFormData("files", filename);



// c. Create the HttpEntity, simulating a file upload part

                HttpEntity<Resource> filePart = new HttpEntity<>(resource, fileHeader);



// d. Add the file part to the form body

                body.add("files", filePart);



            } catch (MalformedURLException e) {

                System.err.println("Skipping file due to invalid URL: " + url + " Error: " + e.getMessage());

            } catch (Exception e) {

                System.err.println("Error creating resource for URL " + url + ": " + e.getMessage());

            }

        }



// 3. Set Headers and Execute Request

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);



        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);



// 4. Determine endpoint and Execute Request

// Ensure your base URL is set correctly in application.properties

        String endpoint = "/speaking/multi";

        String fullUrl = gradingApiBaseUrl + endpoint;



        try {

            System.out.println("Submitting request to: " + fullUrl + " (Multipart)");



            return restTemplate.exchange(

                    fullUrl,

                    HttpMethod.POST,

                    requestEntity,

                    String.class

            );



        } catch (HttpClientErrorException e) {

// Handles 4xx errors from the external API

            System.err.println("External API HTTP Error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());

            String errorMessage = "Grading API returned an error: " + e.getStatusCode().value();



            return new ResponseEntity<>(

                    "{\"error_message\": \"" + errorMessage + "\", \"details\": " + e.getResponseBodyAsString() + "\"",

                    e.getStatusCode()

            );



        } catch (ResourceAccessException e) {

// Handles connection errors (e.g., timeout, server down)

            String errorMessage = "Grading API server is unreachable. Please check configuration.";

            return new ResponseEntity<>(

                    "{\"error_message\": \"" + errorMessage + "\"}",

                    HttpStatus.SERVICE_UNAVAILABLE

            );

        }

    }

}