package com.ieltsgrading.ielts_evaluator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class for IELTS Writing Test
 * Maps to table: ielts_writing_test
 */
@Entity
@Table(name = "ielts_writing_test")
public class IeltsWritingTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Integer testId;
    
    @Column(name = "task1_link", length = 600)
    private String task1Link;
    
    @Column(name = "task1_question", columnDefinition = "TEXT")
    private String task1Question;
    
    @Column(name = "task2_question", columnDefinition = "TEXT")
    private String task2Question;
    
    // Constructors
    public IeltsWritingTest() {
    }
    
    public IeltsWritingTest(String task1Link, String task1Question, String task2Question) {
        this.task1Link = task1Link;
        this.task1Question = task1Question;
        this.task2Question = task2Question;
    }
    
    // Getters and Setters
    public Integer getTestId() {
        return testId;
    }
    
    public void setTestId(Integer testId) {
        this.testId = testId;
    }
    
    public String getTask1Link() {
        return task1Link;
    }
    
    public void setTask1Link(String task1Link) {
        this.task1Link = task1Link;
    }
    
    public String getTask1Question() {
        return task1Question;
    }
    
    public void setTask1Question(String task1Question) {
        this.task1Question = task1Question;
    }
    
    public String getTask2Question() {
        return task2Question;
    }
    
    public void setTask2Question(String task2Question) {
        this.task2Question = task2Question;
    }
    
    // Helper methods
    
    /**
     * Get task 1 type based on keywords in question
     */
    public String getTask1Type() {
        if (task1Question == null) return "Unknown";
        
        String lowerQuestion = task1Question.toLowerCase();
        
        if (lowerQuestion.contains("chart") || lowerQuestion.contains("bar")) {
            return "Bar Chart";
        } else if (lowerQuestion.contains("pie")) {
            return "Pie Chart";
        } else if (lowerQuestion.contains("line") || lowerQuestion.contains("graph")) {
            return "Line Graph";
        } else if (lowerQuestion.contains("table")) {
            return "Table";
        } else if (lowerQuestion.contains("map")) {
            return "Map";
        } else if (lowerQuestion.contains("diagram") || lowerQuestion.contains("process")) {
            return "Process Diagram";
        } else {
            return "Graph/Chart";
        }
    }
    
    /**
     * Get display-friendly test ID (e.g., "cam20-test4")
     * Assumes test_id 1-4 = CAM 20, 5-8 = CAM 19, etc.
     */
    public String getDisplayId() {
        if (testId == null) return "unknown";
        
        int camNumber = 20 - ((testId - 1) / 4);
        int testNumber = ((testId - 1) % 4) + 1;
        
        return String.format("cam%d-test%d", camNumber, testNumber);
    }
    
    /**
     * Get CAM number (e.g., 20, 19, 18...)
     */
    public int getCamNumber() {
        if (testId == null) return 0;
        return 20 - ((testId - 1) / 4);
    }
    
    /**
     * Get test number within CAM (1-4)
     */
    public int getTestNumber() {
        if (testId == null) return 0;
        return ((testId - 1) % 4) + 1;
    }
    
    /**
     * Get background color based on CAM number
     */
    public String getBackgroundColor() {
        int cam = getCamNumber();
        String[] colors = {"purple", "beige", "dark", "green", "blue"};
        int index = (20 - cam) % colors.length;
        return colors[index];
    }
    
    /**
     * Convert Google Drive share link to direct image link
     * From: https://drive.google.com/file/d/FILE_ID/view?usp=drive_link
     * To: https://drive.google.com/uc?export=view&id=FILE_ID
     */
    public String getDirectImageUrl() {
        if (task1Link == null || task1Link.isEmpty()) {
            return null;
        }
        
        try {
            // Extract file ID from Google Drive link
            String fileId = extractGoogleDriveFileId(task1Link);
            if (fileId != null) {
                return "https://drive.google.com/uc?export=view&id=" + fileId;
            }
        } catch (Exception e) {
            System.err.println("Error converting Drive link: " + e.getMessage());
        }
        
        return task1Link; // Return original if conversion fails
    }
    
    /**
     * Extract file ID from Google Drive URL
     */
    private String extractGoogleDriveFileId(String url) {
        if (url == null) return null;
        
        // Pattern: /d/FILE_ID/
        String pattern = "/d/([^/]+)/";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(url);
        
        if (m.find()) {
            return m.group(1);
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return "IeltsWritingTest{" +
                "testId=" + testId +
                ", displayId='" + getDisplayId() + '\'' +
                ", task1Type='" + getTask1Type() + '\'' +
                '}';
    }
}