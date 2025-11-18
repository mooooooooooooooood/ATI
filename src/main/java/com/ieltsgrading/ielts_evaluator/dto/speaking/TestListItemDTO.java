package com.ieltsgrading.ielts_evaluator.dto.speaking;

public class TestListItemDTO {
    private Integer testId;
    private String testDate;
    private String mainTopic;
    private String testLabel; // A derived field for display


    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public String getMainTopic() {
        return mainTopic;
    }

    public void setMainTopic(String mainTopic) {
        this.mainTopic = mainTopic;
    }

    public String getTestLabel() {
        return testLabel;
    }

    public void setTestLabel(String testLabel) {
        this.testLabel = testLabel;
    }


    // Constructor to map from the Entity to the DTO
    public TestListItemDTO(Integer testId, String testDate, String mainTopic) {
        this.testId = testId;
        this.testDate = testDate;
        this.mainTopic = mainTopic;
        this.testLabel = "Test " + testId + " (" + testDate + ")";
    }


}