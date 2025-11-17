package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set; // NEW IMPORT
@Entity
@Table(name = "reading_passage")
public class ReadingPassage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- FIX: Added missing generation strategy
    @Column(name = "passage_id")
    private int passageId; // primary key (changed from int id to passageId for clarity)

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private ReadingTest test;

    @Column(nullable = false)
    private String title;

    @Column(name = "passage_text", columnDefinition = "TEXT")
    private String passageText;

    @Column(name = "passage_order")
    private Integer passageOrder;

    @OneToMany(mappedBy = "passage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("groupOrder ASC") // Good practice for Sets
    private Set<ReadingQuestionGroup> questionGroups;

    // Getters and Setters for all fields
    public int getPassageId() {
        return passageId;
    }

    public void setPassageId(int passageId) {
        this.passageId = passageId;
    }

    public ReadingTest getTest() {
        return test;
    }

    public void setTest(ReadingTest test) {
        this.test = test;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPassageText() {
        return passageText;
    }

    public void setPassageText(String passageText) {
        this.passageText = passageText;
    }

    public Integer getPassageOrder() {
        return passageOrder;
    }

    public void setPassageOrder(Integer passageOrder) {
        this.passageOrder = passageOrder;
    }

    public Set<ReadingQuestionGroup> getQuestionGroups() {
        return questionGroups;
    }

    public void setQuestionGroups(Set<ReadingQuestionGroup> questionGroups) {
        this.questionGroups = questionGroups;
    } // Changed method signature
}