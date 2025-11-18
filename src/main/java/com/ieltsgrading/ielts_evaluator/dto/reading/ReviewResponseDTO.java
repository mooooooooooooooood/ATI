package com.ieltsgrading.ielts_evaluator.dto.reading; // Ensure package matches your project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true) // Safely ignore extra fields if Gemini adds them
public class ReviewResponseDTO {

    // Change all fields to Object to handle String, Array, or nested Object responses safely
    private Object overviewSummary;
    private Object vocabularyWeaknesses;
    private Object questionTypeInsights;
    private Object strategyRecommendations;

    public ReviewResponseDTO() {}

    // --- Smart Getters: Convert whatever object comes in to a clean String ---

    public String getOverviewSummary() {
        return formatToString(overviewSummary);
    }

    public void setOverviewSummary(Object overviewSummary) {
        this.overviewSummary = overviewSummary;
    }

    public String getVocabularyWeaknesses() {
        return formatToString(vocabularyWeaknesses);
    }

    public void setVocabularyWeaknesses(Object vocabularyWeaknesses) {
        this.vocabularyWeaknesses = vocabularyWeaknesses;
    }

    public String getQuestionTypeInsights() {
        return formatToString(questionTypeInsights);
    }

    public void setQuestionTypeInsights(Object questionTypeInsights) {
        this.questionTypeInsights = questionTypeInsights;
    }

    public String getStrategyRecommendations() {
        return formatToString(strategyRecommendations);
    }

    public void setStrategyRecommendations(Object strategyRecommendations) {
        this.strategyRecommendations = strategyRecommendations;
    }

    // Helper method to handle String vs Object/List
    private String formatToString(Object obj) {
        if (obj == null) return "No data provided.";

        if (obj instanceof String) {
            return (String) obj;
        }

        // ⭐ FIX: If it's a List, join elements with a line break instead of brackets
        if (obj instanceof List) {
            return ((List<?>) obj).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("<br><br>"));
        }

        return obj.toString();
    }
}