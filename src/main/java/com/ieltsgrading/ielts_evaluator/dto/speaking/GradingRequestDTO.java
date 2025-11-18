package com.ieltsgrading.ielts_evaluator.dto.speaking;

import java.util.List;

/**
 * Data Transfer Object representing the full payload sent to the external
 * grading service. It contains the test metadata and all recorded answers.
 */
public class GradingRequestDTO {

    private Integer testId;
    private List<UserAnswerDTO> answers; // List of all collected answers (Part 1, 3, or Part 2 Cue Card)

    // --- Constructors ---

    /**
     * Default constructor required by Spring/Jackson for serialization/deserialization.
     */
    public GradingRequestDTO() {
    }

    public GradingRequestDTO(Integer testId, List<UserAnswerDTO> answers) {
        this.testId = testId;
        this.answers = answers;
    }

    // --- Getters ---

    public Integer getTestId() {
        return testId;
    }

    public List<UserAnswerDTO> getAnswers() {
        return answers;
    }

    // --- Setters ---

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public void setAnswers(List<UserAnswerDTO> answers) {
        this.answers = answers;
    }
}