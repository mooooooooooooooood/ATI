package com.ieltsgrading.ielts_evaluator.dto.speaking;

/**
 * Data Transfer Object representing a single recorded answer from the user,
 * linked to the specific question asked. Used for session storage and final submission.
 */
public class UserAnswerDTO {

    private Integer questionId;
    private String partNumber; // "Part 1", "Part 3", or "Part 2"
    private String questionText;
    private String recordedAudioUrl; // The URL returned by your file upload API (S3, local storage, etc.)

    // --- Constructors ---

    /**
     * Default constructor required by Spring/Jackson for serialization/deserialization.
     */
    public UserAnswerDTO() {
    }

    public UserAnswerDTO(Integer questionId, String partNumber, String questionText, String recordedAudioUrl) {
        this.questionId = questionId;
        this.partNumber = partNumber;
        this.questionText = questionText;
        this.recordedAudioUrl = recordedAudioUrl;
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

    public String getRecordedAudioUrl() {
        return recordedAudioUrl;
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

    public void setRecordedAudioUrl(String recordedAudioUrl) {
        this.recordedAudioUrl = recordedAudioUrl;
    }
}