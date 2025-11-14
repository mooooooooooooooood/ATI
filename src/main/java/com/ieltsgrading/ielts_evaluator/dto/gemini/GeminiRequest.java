// GeminiRequest.java
package com.ieltsgrading.ielts_evaluator.dto.gemini;
// Note: You must manually implement getters/setters/constructors 
// for these classes or use Lombok. For simplicity, getters/setters are omitted here.

import java.util.List;

public class GeminiRequest {
    private List<Content> contents;




    public GeminiRequest(String prompt) {
        this.contents = List.of(new Content(prompt));
    }
    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
    public static class Content {
        private List<Part> parts;


        public Content(String text) { this.parts = List.of(new Part(text)); }
        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }

    }


    public static class Part {
        private String text;

        public Part(String text) { this.text = text; }
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}