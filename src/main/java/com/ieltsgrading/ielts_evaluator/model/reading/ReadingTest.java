package com.ieltsgrading.ielts_evaluator.model.reading;

import com.ieltsgrading.ielts_evaluator.model.TestlevelEnums;
import jakarta.persistence.*;

import java.util.Set; // NEW IMPORT

@Entity
@Table(name = "reading_test")
public class ReadingTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id") // <-- FIX: Mapped Java 'id' field to SQL column 'test_id'
    private int id; // Using 'int'

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_level")
    private TestlevelEnums testLevel;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("passageOrder ASC") // Good practice for Sets
    private Set<ReadingPassage> passages; // CHANGED from List to Set

    @Column(name = "gemini_cache_name")
    private String geminiCacheName;

    public String getGeminiCacheName() { return geminiCacheName; }
    public void setGeminiCacheName(String geminiCacheName) { this.geminiCacheName = geminiCacheName; }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public TestlevelEnums getTestLevel() { return testLevel; }
    public void setTestLevel(TestlevelEnums testLevel) { this.testLevel = testLevel; }

    public Set<ReadingPassage> getPassages() { return passages; }
    public void setPassages(Set<ReadingPassage> passages) { this.passages = passages; } // Changed method signature}
}