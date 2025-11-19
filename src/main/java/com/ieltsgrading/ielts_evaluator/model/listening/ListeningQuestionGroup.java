package com.ieltsgrading.ielts_evaluator.model.listening;

import com.ieltsgrading.ielts_evaluator.model.listening.*;
import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

/**
 * Entity model representing the 'listening_question_group' table.
 * This groups related questions (e.g., all questions referring to one map)
 * and holds the main context image/instructions for that group.
 */
@Entity
@Table(name = "listening_question_group")
public class ListeningQuestionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "instructions", columnDefinition = "TEXT", nullable = false)
    private String instructions;

    @Column(name = "image_url", length = 500)
    private String imageUrl; // URL for the context image (Map, Table, Flow-chart)

    @Column(name = "group_order")
    private Integer groupOrder;

    // Foreign Key: group_id -> listening_section.section_id
    // Many Question Groups belong to One Section
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private ListeningSection section;

    // Reverse relationship (One-to-Many) to Questions
    // One Group has Many Questions
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC") // Sort questions by their order (1, 2, 3...)
    private Set<ListeningQuestion> questions = new HashSet<>();


    // ------------------------------------------------------------------
    // 🔨 CONSTRUCTORS
    // ------------------------------------------------------------------

    // Default No-Argument Constructor (Required by JPA)
    public ListeningQuestionGroup() {
    }

    /**
     * Constructor for required fields.
     */
    public ListeningQuestionGroup(String instructions, Integer groupOrder, ListeningSection section) {
        this.instructions = instructions;
        this.groupOrder = groupOrder;
        this.section = section;
    }


    // ------------------------------------------------------------------
    // ⚙️ EXPLICIT GETTERS AND SETTERS
    // ------------------------------------------------------------------

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getGroupOrder() {
        return groupOrder;
    }

    public void setGroupOrder(Integer groupOrder) {
        this.groupOrder = groupOrder;
    }

    public ListeningSection getSection() {
        return section;
    }

    public void setSection(ListeningSection section) {
        this.section = section;
    }

    public Set<ListeningQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<ListeningQuestion> questions) {
        this.questions = questions;
    }
}