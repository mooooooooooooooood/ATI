package com.ieltsgrading.ielts_evaluator.model.listening;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity model representing the 'listening_user_answer' table.
 * Records a single response given by a user to a specific question.
 */
@Entity
@Table(name = "listening_user_answer")
public class ListeningUserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Integer answerId;

    // Optional: In a full application, this would link to a User entity
    @Column(name = "user_id", nullable = true)
    private Integer userId;

    // Foreign Key: Links this answer to the specific question answered
    // Many Answers belong to One Question
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ListeningQuestion question;

    @Column(name = "user_response", length = 255)
    private String userResponse; // The text the user entered

    @Column(name = "is_correct")
    private Boolean isCorrect; // Result of the grading logic

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;


    // ------------------------------------------------------------------
    // 🔨 CONSTRUCTORS
    // ------------------------------------------------------------------

    // Default No-Argument Constructor (Required by JPA)
    public ListeningUserAnswer() {
        this.submittedAt = LocalDateTime.now(); // Initialize timestamp on creation
    }

    /**
     * Constructor for submitting a new user answer.
     */
    public ListeningUserAnswer(Integer userId, ListeningQuestion question, String userResponse) {
        this.userId = userId;
        this.question = question;
        this.userResponse = userResponse;
        this.submittedAt = LocalDateTime.now();
        // isCorrect is typically set after grading logic runs in the service layer
    }


    // ------------------------------------------------------------------
    // ⚙️ EXPLICIT GETTERS AND SETTERS
    // ------------------------------------------------------------------

    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ListeningQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ListeningQuestion question) {
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}