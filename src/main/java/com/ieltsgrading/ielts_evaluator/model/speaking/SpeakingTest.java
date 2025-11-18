package com.ieltsgrading.ielts_evaluator.model.speaking;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "speaking_tests")
public class SpeakingTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Integer testId;

    @Column(name = "test_date", nullable = false, length = 50)
    private String testDate; // Keeping as String based on your SQL: 'Ngày 02.01'

    @Column(name = "main_topic", length = 255)
    private String mainTopic;

    public SpeakingTest() {
    }

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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<SpeakingTestDetail> getDetails() {
        return details;
    }

    public void setDetails(List<SpeakingTestDetail> details) {
        this.details = details;
    }

    public List<SpeakingTestQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<SpeakingTestQuestion> questions) {
        this.questions = questions;
    }

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.sql.Timestamp createdAt;

    // --- Relationships ---

    public SpeakingTest(Integer testId, String testDate, String mainTopic, Timestamp createdAt, List<SpeakingTestDetail> details, List<SpeakingTestQuestion> questions) {
        this.testId = testId;
        this.testDate = testDate;
        this.mainTopic = mainTopic;
        this.createdAt = createdAt;
        this.details = details;
        this.questions = questions;
    }

    @OneToMany(mappedBy = "speakingTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpeakingTestDetail> details; // Part 1 Summary / Part 2 Cue Card

    @OneToMany(mappedBy = "speakingTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpeakingTestQuestion> questions; // Part 1 & Part 3 Questions

    // Constructors, Getters, and Setters go here...
}