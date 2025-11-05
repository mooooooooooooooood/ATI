package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set; // NEW IMPORT
@Entity
@Table(name = "reading_question_group")
public class ReadingQuestionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- FIX: Added missing generation strategy
    @Column(name = "group_id") // <-- FIX: Mapped Java 'id' field to SQL column 'group_id'
    private int id;

    @ManyToOne
    @JoinColumn(name = "passage_id", nullable = false)
    private ReadingPassage passage;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "group_order")
    private Integer groupOrder;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC") // Good practice for Sets
    private Set<ReadingQuestion> questions; // CHANGED from List to Set

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ReadingPassage getPassage() {
        return passage;
    }

    public void setPassage(ReadingPassage passage) {
        this.passage = passage;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getGroupOrder() {
        return groupOrder;
    }

    public void setGroupOrder(Integer groupOrder) {
        this.groupOrder = groupOrder;
    }

    public Set<ReadingQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<ReadingQuestion> questions) {
        this.questions = questions;
    } // Changed method signature
}