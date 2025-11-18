package com.ieltsgrading.ielts_evaluator.service;

import com.ieltsgrading.ielts_evaluator.dto.speaking.QuestionQueueItemDTO;
import com.ieltsgrading.ielts_evaluator.dto.speaking.SpeakingTestFullDTO;
import com.ieltsgrading.ielts_evaluator.dto.speaking.TestListItemDTO;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTest;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestDetail;
import com.ieltsgrading.ielts_evaluator.model.speaking.SpeakingTestQuestion;
import com.ieltsgrading.ielts_evaluator.repository.speaking.SpeakingTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for managing IELTS Speaking Test data.
 * Handles business logic and transaction management.
 */
@Service
public class SpeakingTestService {

    private final SpeakingTestRepository testRepository;

    @Autowired
    public SpeakingTestService(SpeakingTestRepository testRepository) {
        this.testRepository = testRepository;
    }

    /**
     * Retrieves all Speaking Tests.
     * Use @Transactional(readOnly = true) for performance on read operations.
     */
    @Transactional(readOnly = true)
    public List<SpeakingTest> findAllTests() {
        return testRepository.findAll();
    }

    /**
     * Retrieves a specific Speaking Test by its ID, including all related details and questions.
     * * Note: Depending on your JPA setup (e.g., fetch type and how you load the data),
     * this method might need to be explicitly transactional to load lazy-fetched
     * associations (details and questions) before the session closes.
     */
    @Transactional(readOnly = true)
    public Optional<SpeakingTest> findTestById(Integer id) {
        // Assuming you have configured the relationship fetching (E.g., using
        // a custom query in the repository for EAGER loading or relying on
        // the transactional context for LAZY loading).
        return testRepository.findById(id);
    }

    /**
     * Searches for Speaking Tests whose main topic contains the given keyword.
     * Note: This requires defining a method in the SpeakingTestRepository:
     * List<SpeakingTest> findByMainTopicContainingIgnoreCase(String keyword);
     */
    @Transactional(readOnly = true)
    public List<SpeakingTest> searchTestsByMainTopic(String keyword) {
        // Implement the search logic using a derived query method
        return testRepository.findByMainTopicContainingIgnoreCase(keyword);
    }

    /**
     * Saves a new Speaking Test entity.
     */
    @Transactional
    public SpeakingTest saveTest(SpeakingTest test) {
        return testRepository.save(test);
    }

    /**
     * Deletes a Speaking Test by ID.
     * Due to CASCADE settings in the schema, this should delete all associated
     * details and questions.
     */
    @Transactional
    public void deleteTest(Integer id) {
        testRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public List<TestListItemDTO> findAllTestListItems() {
        // 1. Fetch all SpeakingTest entities
        List<SpeakingTest> tests = testRepository.findAll();

        // 2. Map the entities to the DTO list
        return tests.stream()
                .map(test -> new TestListItemDTO(
                        test.getTestId(),
                        test.getTestDate(),
                        test.getMainTopic()
                ))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public SpeakingTestFullDTO getFullTestDetails(Integer testId) {
        // Fetch the test entity. Use findById().orElseThrow() for clean error handling.
        SpeakingTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));

        SpeakingTestFullDTO dto = new SpeakingTestFullDTO();
        dto.setTestId(test.getTestId());
        dto.setTestDate(test.getTestDate());
        dto.setMainTopic(test.getMainTopic());

        // --- Process Speaking Test Details (Part 1 Topics & Cue Card) ---
        List<String> part1Topics = test.getDetails().stream()
                .filter(d -> d.getPartTopic().equals("Part 1 Topics"))
                // Split the detail_text (e.g., 'Work|Parties') into individual topics
                .flatMap(d -> List.of(d.getDetailText().split("\\|")).stream())
                .collect(Collectors.toList());

        String part2CueCard = test.getDetails().stream()
                .filter(d -> d.getPartTopic().equals("Part 2 Cue Card"))
                .map(SpeakingTestDetail::getDetailText)
                .findFirst()
                .orElse("Cue Card Not Found");

        dto.setPart1Topics(part1Topics);
        dto.setPart2CueCard(part2CueCard);

        // --- Process Questions (Part 1 & Part 3) ---
        Map<String, List<String>> questionsMap = test.getQuestions().stream()
                // Group questions by their part_number ('Part 1', 'Part 3')
                .collect(Collectors.groupingBy(
                        SpeakingTestQuestion::getPartNumber,
                        Collectors.mapping(SpeakingTestQuestion::getQuestionText, Collectors.toList())
                ));

        dto.setQuestionsByPart(questionsMap);

        return dto;
    }
    @Transactional(readOnly = true)
    public List<QuestionQueueItemDTO> buildQuestionQueue(Integer testId) {
        // 1. Fetch the main test entity (ensure questions are loaded)
        SpeakingTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));

        List<QuestionQueueItemDTO> queue = test.getQuestions().stream()
                .map(q -> new QuestionQueueItemDTO(q.getQuestionId(), q.getPartNumber(), q.getQuestionText()))
                .collect(Collectors.toList());

        // 2. Sort the queue sequentially: Part 1 questions first, then Part 3 questions.
        // We assume the database query naturally returned questions within a part in order.
        queue.sort(Comparator.comparing(QuestionQueueItemDTO::getPartNumber));

        // NOTE: Part 2 (Cue Card) is generally treated as a single event.
        // We'll add a dummy entry or handle the Cue Card separately in the Controller.

        return queue;
    }
}