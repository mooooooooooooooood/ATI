package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "speaking_tests")
public class IeltsSpeakingTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long id;

    @Column(name = "test_date", nullable = false, length = 50)
    private String testDate;

    @Column(name = "main_topic", length = 255)
    private String mainTopic;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods for display
    public String getTitle() {
        return "Speaking Test - " + testDate;
    }

    public String getViews() {
        // Mock data - in production, track actual views
        return ((id % 10) + 1) + "K lượt làm";
    }

    public String getBackground() {
        // Rotate through 5 background colors
        String[] backgrounds = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8"};
        return backgrounds[(int)(id % backgrounds.length)];
    }

    public String getCam() {
        // Extract CAM number from mainTopic if exists
        // For now, return empty as data doesn't have this
        return "";
    }

    public String getTestNumber() {
        // Use test_id as test number
        return String.valueOf(id);
    }

    // Generate testId for URL (e.g., "test-1")
    public String getTestId() {
        return "test-" + id;
    }

    @Override
    public String toString() {
        return "IeltsSpeakingTest{" +
                "id=" + id +
                ", testDate='" + testDate + '\'' +
                ", mainTopic='" + mainTopic + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}