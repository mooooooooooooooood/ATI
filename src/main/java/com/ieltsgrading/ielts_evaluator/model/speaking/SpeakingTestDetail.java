package com.ieltsgrading.ielts_evaluator.model.speaking;

import jakarta.persistence.*;

@Entity
@Table(name = "speaking_test_details")
public class SpeakingTestDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private SpeakingTest speakingTest; // FK to speaking_tests

    @Column(name = "part_topic", nullable = false, length = 255)
    private String partTopic; // e.g., 'Part 1 Topics', 'Part 2 Cue Card'

    // ✅ FIXED: Changed from TINYTEXT to TEXT to support longer content
    @Column(name = "detail_text", columnDefinition = "TEXT")
    private String detailText;

    public SpeakingTestDetail() {
    }

    public SpeakingTestDetail(Integer detailId, SpeakingTest speakingTest, String partTopic, String detailText) {
        this.detailId = detailId;
        this.speakingTest = speakingTest;
        this.partTopic = partTopic;
        this.detailText = detailText;
    }

    public String getPartTopic() {
        return partTopic;
    }

    public void setPartTopic(String partTopic) {
        this.partTopic = partTopic;
    }

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }

    public SpeakingTest getSpeakingTest() {
        return speakingTest;
    }

    public void setSpeakingTest(SpeakingTest speakingTest) {
        this.speakingTest = speakingTest;
    }

    public String getDetailText() {
        return detailText;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }
}