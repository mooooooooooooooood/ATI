package com.ieltsgrading.ielts_evaluator.model.speaking;

import jakarta.persistence.*;

@Entity
@Table(name = "speaking_test_questions")
public class SpeakingTestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private SpeakingTest speakingTest; // FK to speaking_tests

    @Column(name = "part_number", nullable = false, length = 10)
    private String partNumber; // 'Part 1' or 'Part 3'

    public SpeakingTestQuestion(){}

    public SpeakingTestQuestion(Integer questionId, SpeakingTest speakingTest, String partNumber, String questionText) {
        this.questionId = questionId;
        this.speakingTest = speakingTest;
        this.partNumber = partNumber;
        this.questionText = questionText;
    }

    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public SpeakingTest getSpeakingTest() {
        return speakingTest;
    }

    public void setSpeakingTest(SpeakingTest speakingTest) {
        this.speakingTest = speakingTest;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }



}
