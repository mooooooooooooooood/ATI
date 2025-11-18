package com.ieltsgrading.ielts_evaluator.dto.speaking;

public class QuestionQueueItemDTO {

    private Integer questionId;
    private String partNumber; // "Part 1", "Part 3", or "Part 2" (for the cue card itself)
    private String questionText;

    // Constructor (required by logic)
    public QuestionQueueItemDTO(Integer questionId, String partNumber, String questionText) {
        this.questionId = questionId;
        this.partNumber = partNumber;
        this.questionText = questionText;
    }

    // Default Constructor (Required if using frameworks like Jackson/Spring for serialization)
    public QuestionQueueItemDTO() {
    }

    // --- Getters ---

    public Integer getQuestionId() {
        return questionId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    // --- Setters ---

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
}