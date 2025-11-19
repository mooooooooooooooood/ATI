package com.ieltsgrading.ielts_evaluator.dto.speaking;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ExternalGradingPayload {

    @JsonProperty("questions") // Forces JSON key "questions"
    private List<String> questions;

    @JsonProperty("files")     // Forces JSON key "files"
    private List<String> files;

    // Optional: Add these if you plan to use them, otherwise they can be null
    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("test_version")
    private String testVersion;

    // --- Constructors ---

    public ExternalGradingPayload() {
    }

    // Your constructor
    public ExternalGradingPayload(List<String> questions, List<String> files, Integer userId, String testVersion) {
        this.questions = questions;
        this.files = files;
        this.userId = userId;
        this.testVersion = testVersion;
    }

    // --- Getters & Setters (Keep them as you have them) ---
    public List<String> getQuestions() { return questions; }
    public void setQuestions(List<String> questions) { this.questions = questions; }

    public List<String> getFiles() { return files; }
    public void setFiles(List<String> files) { this.files = files; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getTestVersion() { return testVersion; }
    public void setTestVersion(String testVersion) { this.testVersion = testVersion; }
}