package com.studygrind.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class QuizRequest {
    @NotBlank(message = "Quiz title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @Positive(message = "Time limit must be positive")
    private Integer timeLimit;
    
    public QuizRequest() {}
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public Integer getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }
}