package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reading_question")
public class ReadingQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id") // <-- FIX: Mapped Java 'id' field to SQL column 'question_id'
    private int id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private ReadingQuestionGroup group;

    @Column(name = "type_id")
    private Integer typeId; // e.g., 1 = T/F/NG, 2 = Y/N/NG, 3 = MCQ, 8 = Summary Completion

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String options; // JSON string or null

    @Column(name = "correct_answer")
    private String correctAnswer;

    @Column(name = "question_order")
    private Integer questionOrder;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ReadingQuestionGroup getGroup() { return group; }
    public void setGroup(ReadingQuestionGroup group) { this.group = group; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
}