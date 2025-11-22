package com.ieltsgrading.ielts_evaluator.dto.listening;

/**
 * DTO used to package the results of a single question for display on the results page
 * and for submission to the Gemini API for analysis.
 */
public class ListeningResultDetailDTO {

    private Integer questionId;
    private String questionType;
    private int questionOrder;
    private String questionText;
    private String userResponse;
    private String correctAnswer;
    private boolean isCorrect;
    private String feedback; // Optional: for detailed, per-question feedback
    private String groupImageUrl;


    private String listeningTaskDescription;


    // --- Full Constructor ---


    public ListeningResultDetailDTO(Integer questionId, String questionType, int questionOrder,
                                    String questionText, String userResponse, String correctAnswer,
                                    boolean isCorrect, String feedback, String groupImageUrl, String listeningTaskDescription) {
        this.questionId = questionId;
        this.questionType = questionType;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.userResponse = userResponse;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.feedback = feedback;
        this.groupImageUrl = groupImageUrl;
        this.listeningTaskDescription = listeningTaskDescription;
    }

    public String getListeningTaskDescription() {
        return listeningTaskDescription;
    }

    public void setListeningTaskDescription(String listeningTaskDescription) {
        this.listeningTaskDescription = listeningTaskDescription;
    }

    // --- Getters and Setters (Explicitly defined for clarity) ---
    public String getGroupImageUrl() {
        return groupImageUrl;
    }

    public void setGroupImageUrl(String groupImageUrl) {
        this.groupImageUrl = groupImageUrl;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(int questionTypeId) {
        this.questionType = questionType;
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

    public boolean isIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

}