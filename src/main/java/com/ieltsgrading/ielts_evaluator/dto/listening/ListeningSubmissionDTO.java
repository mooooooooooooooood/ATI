package com.ieltsgrading.ielts_evaluator.dto.listening;

import java.util.List;

/**
 * Data Transfer Object (DTO) used to receive a user's complete set of answers
 * for a single Listening test submission from the web form.
 */
public class ListeningSubmissionDTO {

    // Corresponds to the hidden input 'testId'
    private int testId;

    // Corresponds to the input name 'answers[...]'
    private List<AnswerDTO> answers;

    // --- Getters and Setters for ListeningSubmissionDTO ---
    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }
    public List<AnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDTO> answers) { this.answers = answers; }


    /**
     * Inner class representing one submitted answer pair:
     * the question ID and the user's text response.
     */
    public static class AnswerDTO {

        // Corresponds to the hidden input 'answers[...].questionId'
        private int questionId;

        // Corresponds to the text input 'answers[...].userResponse'
        private String userResponse;

        // --- Getters and Setters for AnswerDTO ---
        public int getQuestionId() { return questionId; }
        public void setQuestionId(int questionId) { this.questionId = questionId; }
        public String getUserResponse() { return userResponse; }
        public void setUserResponse(String userResponse) { this.userResponse = userResponse; }
    }
}