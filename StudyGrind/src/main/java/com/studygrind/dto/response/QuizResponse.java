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
    private Double score;
    private List<QuestionResponse> questions = new ArrayList<>();
    
    public QuizResponse() {}
    
    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public Integer getTimeLimit() { return timeLimit; }
    public Integer getQuestionCount() { return questionCount; }
    public Boolean getAttempted() { return attempted; }
    public Double getScore() { return score; }
    public List<QuestionResponse> getQuestions() { return questions; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setTimeLimit(Integer timeLimit) { this.timeLimit = timeLimit; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    public void setAttempted(Boolean attempted) { this.attempted = attempted; }
    public void setScore(Double score) { this.score = score; }
    public void setQuestions(List<QuestionResponse> questions) { this.questions = questions; }
}