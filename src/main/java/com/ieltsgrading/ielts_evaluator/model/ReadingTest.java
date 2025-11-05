package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.util.List;
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

    @Column(name = "test_level")
    private String testLevel;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("passageOrder ASC") // Good practice for Sets
    private Set<ReadingPassage> passages; // CHANGED from List to Set

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getTestLevel() { return testLevel; }
    public void setTestLevel(String testLevel) { this.testLevel = testLevel; }

    public Set<ReadingPassage> getPassages() { return passages; }
    public void setPassages(Set<ReadingPassage> passages) { this.passages = passages; } // Changed method signature}
}