package com.studygrind.dto.response;

import java.time.LocalDateTime;

public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private LocalDateTime dueDate;
    private Integer maxScore;
    private String teacher;
    private Boolean submitted;
    private Long courseId;
    private String courseName;
    private Integer submissions;
    private Double score;
    private String feedback;

    public AssignmentResponse() {}

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public LocalDateTime getDueDate() { return dueDate; }
    public Integer getMaxScore() { return maxScore; }
    public String getTeacher() { return teacher; }
    public Boolean getSubmitted() { return submitted; }
    public Long getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public Integer getSubmissions() { return submissions; }
    public Double getScore() { return score; }
    public String getFeedback() { return feedback; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public void setSubmitted(Boolean submitted) { this.submitted = submitted; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setSubmissions(Integer submissions) { this.submissions = submissions; }
    public void setScore(Double score) { this.score = score; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}