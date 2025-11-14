package com.ieltsgrading.ielts_evaluator.dto;

public class ReadingResultDetailDTO {

    // Ensure the private field name matches the expected property name in the getter
    private int questionOrder;
    private String questionText;
    private String userResponse;
    private String correctAnswer;
    private boolean isCorrect;
    private String geminiExplanation;

    // --- Constructor to simplify mapping ---
    public ReadingResultDetailDTO(int questionOrder, String questionText, String userResponse, String correctAnswer, boolean isCorrect, String geminiExplanation) {
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.userResponse = userResponse;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.geminiExplanation = geminiExplanation; // <-- Set the new field
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) { // (Optional, but good practice)
        this.questionOrder = questionOrder;
    }

    // Ensure all other required getters are present (for questionText, userResponse, etc.)

    public String getQuestionText() {
        return questionText;
    }
    // ... (rest of getters/setters) ...
    public String getUserResponse() {
        return userResponse;
    }
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    // Note: boolean fields use 'is' or 'get'. getIsCorrect() is fine.
    public boolean getIsCorrect() {
        return isCorrect;
    }
    public String getGeminiExplanation() {
        return geminiExplanation;
    }
}