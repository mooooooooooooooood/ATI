package com.ieltsgrading.ielts_evaluator.dto.listening;

// NOTE: This DTO is often placed in a generic 'dto.gemini' package
// if shared by Reading and Speaking modules, but it's listed here for Listening.

/**
 * Data Transfer Object (DTO) used to receive structured analytical feedback
 * from the external AI model (Gemini). The fields must match the JSON schema
 * requested in the service layer prompt exactly.
 */
public class ReviewResponseDTO {

    // --- Core Feedback Fields ---
    private String overviewSummary;
    private String vocabularyWeaknesses;
    private String questionTypeInsights;
    private String strategyRecommendations;

    // ------------------------------------------------------------------
    // 🔨 CONSTRUCTORS
    // ------------------------------------------------------------------

    // Default No-Argument Constructor (Required for JSON deserialization by Jackson)
    public ReviewResponseDTO() {
    }

    /**
     * Full Constructor (Optional, but good for testing/initialization)
     */
    public ReviewResponseDTO(String overviewSummary, String vocabularyWeaknesses,
                             String questionTypeInsights, String strategyRecommendations) {
        this.overviewSummary = overviewSummary;
        this.vocabularyWeaknesses = vocabularyWeaknesses;
        this.questionTypeInsights = questionTypeInsights;
        this.strategyRecommendations = strategyRecommendations;
    }


    // ------------------------------------------------------------------
    // ⚙️ EXPLICIT GETTERS AND SETTERS
    // ------------------------------------------------------------------

    public String getOverviewSummary() {
        return overviewSummary;
    }

    public void setOverviewSummary(String overviewSummary) {
        this.overviewSummary = overviewSummary;
    }

    public String getVocabularyWeaknesses() {
        return vocabularyWeaknesses;
    }

    public void setVocabularyWeaknesses(String vocabularyWeaknesses) {
        this.vocabularyWeaknesses = vocabularyWeaknesses;
    }

    public String getQuestionTypeInsights() {
        return questionTypeInsights;
    }

    public void setQuestionTypeInsights(String questionTypeInsights) {
        this.questionTypeInsights = questionTypeInsights;
    }

    public String getStrategyRecommendations() {
        return strategyRecommendations;
    }

    public void setStrategyRecommendations(String strategyRecommendations) {
        this.strategyRecommendations = strategyRecommendations;
    }
}