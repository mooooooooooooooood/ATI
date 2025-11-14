package com.ieltsgrading.ielts_evaluator.dto.gemini;


import java.util.List;

public class GeminiResponse {
    private List<Candidate> candidates;

    // REQUIRED: Default constructor for Jackson
    public GeminiResponse() {}

    // Existing Getters and Setters...
    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }


    public static class Candidate {
        private Content content;

        // REQUIRED: Default constructor for Jackson
        public Candidate() {}

        // Existing Getters and Setters...
        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }
    }

    public static class Content {
        private List<Part> parts;

        // REQUIRED: Default constructor for Jackson
        public Content() {}

        // Existing parameterized constructor (optional, but kept for completeness)
        public Content(List<Part> parts) {
            this.parts = parts;
        }

        // Existing Getters and Setters...
        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        private String text;

        // REQUIRED: Default constructor for Jackson
        public Part() {}

        // Existing parameterized constructor (optional, but kept for completeness)
        public Part(String text) {
            this.text = text;
        }

        // Existing Getters and Setters...
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}