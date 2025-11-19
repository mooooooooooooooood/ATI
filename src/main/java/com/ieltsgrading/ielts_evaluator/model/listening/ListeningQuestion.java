package com.ieltsgrading.ielts_evaluator.model.listening;

import jakarta.persistence.*;

import java.util.Map;

@Entity
@Table(name = "listening_question")
public class ListeningQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ListeningQuestionGroup group;

    // --- FIX 1: Map this as Integer to match your SQL DB and satisfy the Foreign Key ---
    @Column(name = "type_id", nullable = false)
    private Integer typeId;
    // ----------------------------------------------------------------------------------

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "question_image_url", length = 500)
    private String questionImageUrl;

    @Column(name = "options", columnDefinition = "JSON")
    private String options;

    @Column(name = "correct_answer", length = 255)
    private String correctAnswer;

    @Column(name = "question_order")
    private Integer questionOrder;
    @Transient
    private Map<String, String> parsedOptions;

    // Constructors
    public ListeningQuestion() {}

    // Getters and Setters
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public ListeningQuestionGroup getGroup() { return group; }
    public void setGroup(ListeningQuestionGroup group) { this.group = group; }

    // --- FIX 2: Getters for the Integer Type ---
    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    // Helper to keep your Service code happy (returns "UNKNOWN" or you can map IDs to Names here)
    public String getQuestionType() {
        return String.valueOf(typeId);
    }
    // ------------------------------------------

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestionImageUrl() { return questionImageUrl; }
    public void setQuestionImageUrl(String questionImageUrl) { this.questionImageUrl = questionImageUrl; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }

    public Map<String, String> getParsedOptions() {
        return parsedOptions;
    }

    public void setParsedOptions(Map<String, String> parsedOptions) {
        this.parsedOptions = parsedOptions;
    }
}