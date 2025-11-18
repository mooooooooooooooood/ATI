package com.ieltsgrading.ielts_evaluator.dto.speaking;

import java.util.List;
import java.util.Map;

public class SpeakingTestFullDTO {
    private Integer testId;
    private String testDate;
    private String mainTopic;

    // Part 1 & Part 2 Topics/Cue Card details
    private List<String> part1Topics; // List of Part 1 topics (from detail_text)

    public String getPart2CueCard() {
        return part2CueCard;
    }

    public void setPart2CueCard(String part2CueCard) {
        this.part2CueCard = part2CueCard;
    }
    private String part2CueCard;      // Full Cue Card text (from detail_text)

    private Map<String, List<String>> questionsByPart;


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

    public List<String> getPart1Topics() {
        return part1Topics;
    }

    public void setPart1Topics(List<String> part1Topics) {
        this.part1Topics = part1Topics;
    }

    public Map<String, List<String>> getQuestionsByPart() {
        return questionsByPart;
    }

    public void setQuestionsByPart(Map<String, List<String>> questionsByPart) {
        this.questionsByPart = questionsByPart;
    }



    public SpeakingTestFullDTO(Integer testId, String testDate, String mainTopic, List<String> part1Topics, String part2CueCard, Map<String, List<String>> questionsByPart) {
        this.testId = testId;
        this.testDate = testDate;
        this.mainTopic = mainTopic;
        this.part1Topics = part1Topics;
        this.part2CueCard = part2CueCard;
        this.questionsByPart = questionsByPart;
    }

    // Map of questions: Key is "Part 1" or "Part 3", Value is a List of questions
    public SpeakingTestFullDTO() {
    }

}