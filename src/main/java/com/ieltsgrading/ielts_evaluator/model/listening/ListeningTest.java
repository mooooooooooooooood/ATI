package com.ieltsgrading.ielts_evaluator.model.listening;

import com.ieltsgrading.ielts_evaluator.model.TestlevelEnums;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * Entity model representing the 'listening_test' table.
 * Stores core test metadata and manages the one-to-many relationship with ListeningSection.
 */
@Entity
@Table(name = "listening_test")
public class ListeningTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Integer testId;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_level")
    private TestlevelEnums testLevel; // Assuming TestLevel Enum is defined elsewhere

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // One-to-Many Relationship: One Test has Many Sections (Parts 1-4)
    // Mapped by the 'test' field in the ListeningSection entity.
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sectionOrder ASC") // Good practice: retrieve sections in the correct order
    private Set<ListeningSection> sections = new HashSet<>();


    // ------------------------------------------------------------------
    // 🔨 CONSTRUCTORS
    // ------------------------------------------------------------------

    // Default No-Argument Constructor (Required by JPA)
    public ListeningTest() {
    }

    // Constructor for required fields
    public ListeningTest(String testName, TestlevelEnums testLevel) {
        this.testName = testName;
        this.testLevel = testLevel;
        // The createdAt field will often be managed automatically by a @PrePersist listener
    }

    // ------------------------------------------------------------------
    // ⚙️ EXPLICIT GETTERS AND SETTERS
    // ------------------------------------------------------------------

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public TestlevelEnums getTestLevel() {
        return testLevel;
    }

    public void setTestLevel(TestlevelEnums testLevel) {
        this.testLevel = testLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<ListeningSection> getSections() {
        return sections;
    }

    public void setSections(Set<ListeningSection> sections) {
        this.sections = sections;
    }
}