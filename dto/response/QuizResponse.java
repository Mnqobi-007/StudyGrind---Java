package com.studygrind.dto.response;

import java.util.ArrayList;
import java.util.List;

public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private Integer timeLimit;
    private Integer questionCount;
    private Boolean attempted;
    private List<QuestionResponse> questions = new ArrayList<>();
    
    public QuizResponse() {}
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public Integer getQuestionCount() {
        return questionCount;
    }
    
    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
    
    public Boolean getAttempted() {
        return attempted;
    }
    
    public void setAttempted(Boolean attempted) {
        this.attempted = attempted;
    }
    
    public List<QuestionResponse> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}