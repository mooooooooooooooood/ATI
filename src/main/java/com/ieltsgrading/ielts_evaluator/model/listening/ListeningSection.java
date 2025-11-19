package com.ieltsgrading.ielts_evaluator.model.listening;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

/**
 * Entity model representing the 'listening_section' table.
 * Stores audio, transcript, and introductory text for one part of a test.
 */
@Entity
@Table(name = "listening_section")
public class ListeningSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Integer sectionId;

    @Column(name = "section_name")
    private String sectionName; // e.g., "Part 1", "Section 3"

    @Column(name = "intro_text", columnDefinition = "TEXT")
    private String introText; // Instructions before the audio starts

    @Column(name = "audio_url", nullable = false, length = 500)
    private String audioUrl; // URL to the MP3 file

    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript; // Full text of the audio script

    @Column(name = "section_order")
    private Integer sectionOrder; // 1, 2, 3, or 4

    // Foreign Key: section_id -> listening_test.test_id
    // Many Sections belong to One Test
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private ListeningTest test;

    // Reverse relationship (One-to-Many) to Question Groups
    // One Section has Many Question Groups
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("groupOrder ASC") // Sort groups by their order
    private Set<ListeningQuestionGroup> questionGroups = new HashSet<>();


    // ------------------------------------------------------------------
    // 🔨 CONSTRUCTORS
    // ------------------------------------------------------------------

    // Default No-Argument Constructor (Required by JPA)
    public ListeningSection() {
    }

    /**
     * Constructor for creating a new Section entity.
     */
    public ListeningSection(String sectionName, String audioUrl, String introText, Integer sectionOrder, ListeningTest test) {
        this.sectionName = sectionName;
        this.audioUrl = audioUrl;
        this.introText = introText;
        this.sectionOrder = sectionOrder;
        this.test = test;
    }


    // ------------------------------------------------------------------
    // ⚙️ EXPLICIT GETTERS AND SETTERS
    // ------------------------------------------------------------------

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getIntroText() {
        return introText;
    }

    public void setIntroText(String introText) {
        this.introText = introText;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public Integer getSectionOrder() {
        return sectionOrder;
    }

    public void setSectionOrder(Integer sectionOrder) {
        this.sectionOrder = sectionOrder;
    }

    public ListeningTest getTest() {
        return test;
    }

    public void setTest(ListeningTest test) {
        this.test = test;
    }

    public Set<ListeningQuestionGroup> getQuestionGroups() {
        return questionGroups;
    }

    public void setQuestionGroups(Set<ListeningQuestionGroup> questionGroups) {
        this.questionGroups = questionGroups;
    }
}