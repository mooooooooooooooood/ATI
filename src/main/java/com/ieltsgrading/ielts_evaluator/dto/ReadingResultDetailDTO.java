package com.ieltsgrading.ielts_evaluator.dto;

public class ReadingResultDetailDTO {

    // --- All Fields Defined at the Top for Clarity ---

    private Integer questionId;      // CRITICAL: Used for async fetching (QID: X)
    private Integer questionTypeId;  // Used for Thymeleaf logic (e.g., Summary Completion)
    private int questionOrder;
    private String questionText;
    private String userResponse;
    private String correctAnswer;
    private boolean isCorrect;
    private String geminiExplanation;

    // --- Constructor ---

    /**
     * Constructor for mapping graded answers to the DTO.
     * Note: argument order should match the typical calling order from the Service layer.
     */public ReadingResultDetailDTO(){}

    public ReadingResultDetailDTO(
            Integer questionId,
            Integer questionTypeId,
            int questionOrder,
            String questionText,
            String userResponse,
            String correctAnswer,
            boolean isCorrect,
            String geminiExplanation)
    {
        this.questionId = questionId;
        this.questionTypeId = questionTypeId;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.userResponse = userResponse;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.geminiExplanation = geminiExplanation;
    }


    // --- Getters and Setters ---

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionTypeId() {
        return questionTypeId;
    }

    public void setQuestionTypeId(Integer questionTypeId) {
        this.questionTypeId = questionTypeId;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    // Use isCorrect() for compliance with boolean naming conventions
    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    // Provided getter for boolean field for Thymeleaf/JavaBean compatibility
    public boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getGeminiExplanation() {
        return geminiExplanation;
    }

    public void setGeminiExplanation(String geminiExplanation) {
        this.geminiExplanation = geminiExplanation;
    }
}