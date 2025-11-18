package com.ieltsgrading.ielts_evaluator.model.speaking;

import com.ieltsgrading.ielts_evaluator.model.SpeakingSubmission;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ✅ NEW: SpeakingSubmissionDetail - Store detailed feedback from AI
 * This is DIFFERENT from SpeakingTestDetail (which stores test questions)
 * This stores the RESULTS/FEEDBACK after grading
 */
@Entity
@Table(name = "speaking_submission_detail")
public class SpeakingSubmissionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @OneToOne
    @JoinColumn(name = "submission_id", unique = true)
    private SpeakingSubmission submission;

    // Individual band scores
    @Column(name = "fluency")
    private Double fluency;

    @Column(name = "lexical_resource")
    private Double lexicalResource;

    @Column(name = "grammatical_range")
    private Double grammaticalRange;

    @Column(name = "pronunciation")
    private Double pronunciation;

    // Detailed feedback
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements;

    // Part-specific results (JSON)
    @Column(name = "part1_result", columnDefinition = "TEXT")
    private String part1Result;

    @Column(name = "part2_result", columnDefinition = "TEXT")
    private String part2Result;

    @Column(name = "part3_result", columnDefinition = "TEXT")
    private String part3Result;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public SpeakingSubmissionDetail() {
    }

    // Getters and Setters
    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public SpeakingSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(SpeakingSubmission submission) {
        this.submission = submission;
    }

    public Double getFluency() {
        return fluency;
    }

    public void setFluency(Double fluency) {
        this.fluency = fluency;
    }

    public Double getLexicalResource() {
        return lexicalResource;
    }

    public void setLexicalResource(Double lexicalResource) {
        this.lexicalResource = lexicalResource;
    }

    public Double getGrammaticalRange() {
        return grammaticalRange;
    }

    public void setGrammaticalRange(Double grammaticalRange) {
        this.grammaticalRange = grammaticalRange;
    }

    public Double getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(Double pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getImprovements() {
        return improvements;
    }

    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }

    public String getPart1Result() {
        return part1Result;
    }

    public void setPart1Result(String part1Result) {
        this.part1Result = part1Result;
    }

    public String getPart2Result() {
        return part2Result;
    }

    public void setPart2Result(String part2Result) {
        this.part2Result = part2Result;
    }

    public String getPart3Result() {
        return part3Result;
    }

    public void setPart3Result(String part3Result) {
        this.part3Result = part3Result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}