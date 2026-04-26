package com.studygrind.dto.response;

import java.time.LocalDateTime;

public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private String teacherName;
    private Long teacherId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer enrolledCount;
    private Boolean isEnrolled;
    private LocalDateTime createdAt;
    private Double price;
    
    public CourseResponse() {}
    
    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getTeacherName() { return teacherName; }
    public Long getTeacherId() { return teacherId; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public Integer getEnrolledCount() { return enrolledCount; }
    public Boolean getIsEnrolled() { return isEnrolled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Double getPrice() { return price; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
    public void setIsEnrolled(Boolean isEnrolled) { this.isEnrolled = isEnrolled; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setPrice(Double price) { this.price = price; }
}