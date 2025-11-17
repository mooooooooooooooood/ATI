package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "reading_user_answer")
public class ReadingUserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int answerId; // Maps to answer_id

    // User ID: nullable Integer type for optional user link
    @Column(name = "user_id")
    private Integer userId;

    // MAPPING FIX: Use @ManyToOne to map the question_id column to the ReadingQuestion entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false) // Maps to the foreign key column
    private ReadingQuestion question; // This object handles the relationship

    @Column(name = "user_response")
    private String userResponse;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    // DB managed timestamp
    @Column(name = "submitted_at", insertable = false, updatable = false)
    private Timestamp submittedAt;

    // --- Getters and Setters ---

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // Now uses the relationship field 'question'
    public ReadingQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ReadingQuestion question) {
        this.question = question;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }
}