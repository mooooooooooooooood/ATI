package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class IeltsSpeakingTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String testTitle;     // e.g., "IELTS Speaking Test 1"
    private String part1Question; // Intro & Interview
    private String part2Question; // Cue Card
    private String part3Question; // Discussion

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTestTitle() { return testTitle; }
    public void setTestTitle(String testTitle) { this.testTitle = testTitle; }

    public String getPart1Question() { return part1Question; }
    public void setPart1Question(String part1Question) { this.part1Question = part1Question; }

    public String getPart2Question() { return part2Question; }
    public void setPart2Question(String part2Question) { this.part2Question = part2Question; }

    public String getPart3Question() { return part3Question; }
    public void setPart3Question(String part3Question) { this.part3Question = part3Question; }
}
