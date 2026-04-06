package com.studygrind.dto.response;

import java.time.LocalDateTime;

public class SubmissionResponse {
    private Long id;
    private String assignmentTitle;
    private String student;
    private Double score;
    private LocalDateTime submittedAt;
    private Boolean graded;
    
    public SubmissionResponse() {}
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAssignmentTitle() {
        return assignmentTitle;
    }
    
    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }
    
    public String getStudent() {
        return student;
    }
    
    public void setStudent(String student) {
        this.student = student;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public Boolean getGraded() {
        return graded;
    }
    
    public void setGraded(Boolean graded) {
        this.graded = graded;
    }
}