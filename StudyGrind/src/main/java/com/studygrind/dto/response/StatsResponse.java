package com.studygrind.dto.response;

public class StatsResponse {
    // Teacher stats
    private Integer assignments = 0;
    private Integer quizzes = 0;
    private Integer notes = 0;
    private Integer submissions = 0;
    private Integer students = 0;
    
    // Student stats
    private Integer pendingAssignments = 0;
    private Integer completedQuizzes = 0;
    private Double avgScore = 0.0;
    
    public StatsResponse() {}
    
    // Teacher Stats Getters/Setters
    public Integer getAssignments() {
        return assignments;
    }
    
    public void setAssignments(Integer assignments) {
        this.assignments = assignments;
    }
    
    public Integer getQuizzes() {
        return quizzes;
    }
    
    public void setQuizzes(Integer quizzes) {
        this.quizzes = quizzes;
    }
    
    public Integer getNotes() {
        return notes;
    }
    
    public void setNotes(Integer notes) {
        this.notes = notes;
    }
    
    public Integer getSubmissions() {
        return submissions;
    }
    
    public void setSubmissions(Integer submissions) {
        this.submissions = submissions;
    }
    
    public Integer getStudents() {
        return students;
    }
    
    public void setStudents(Integer students) {
        this.students = students;
    }
    
    // Student Stats Getters/Setters
    public Integer getPendingAssignments() {
        return pendingAssignments;
    }
    
    public void setPendingAssignments(Integer pendingAssignments) {
        this.pendingAssignments = pendingAssignments;
    }
    
    public Integer getCompletedQuizzes() {
        return completedQuizzes;
    }
    
    public void setCompletedQuizzes(Integer completedQuizzes) {
        this.completedQuizzes = completedQuizzes;
    }
    
    public Double getAvgScore() {
        return avgScore;
    }
    
    public void setAvgScore(Double avgScore) {
        this.avgScore = avgScore;
    }
}