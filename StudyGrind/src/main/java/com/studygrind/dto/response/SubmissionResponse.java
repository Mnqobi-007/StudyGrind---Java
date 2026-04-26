package com.studygrind.dto.response;

import java.time.LocalDateTime;

public class SubmissionResponse {
    private Long id;
    private String assignmentTitle;
    private String student;
    private Double score;
    private LocalDateTime submittedAt;
    private Boolean graded;
    private String content;
    private String feedback;
    private Integer maxScore;
    private String fileName;
    private String filePath;

    public SubmissionResponse() {}

    // Getters
    public Long getId() { return id; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public String getStudent() { return student; }
    public Double getScore() { return score; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public Boolean getGraded() { return graded; }
    public String getContent() { return content; }
    public String getFeedback() { return feedback; }
    public Integer getMaxScore() { return maxScore; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
    public void setStudent(String student) { this.student = student; }
    public void setScore(Double score) { this.score = score; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public void setGraded(Boolean graded) { this.graded = graded; }
    public void setContent(String content) { this.content = content; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}