package com.studygrind.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class CourseDetailResponse {
    private CourseResponse course;
    private Integer totalEnrollments;
    private Boolean canAccessContent;
    
    // Additional fields for backward compatibility
    private Long id;
    private String name;
    private String description;
    private String teacherName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer enrolledCount;
    private Boolean isEnrolled;
    private Boolean canEnroll;
    private Double price;
    private List<EnrollmentResponse> enrolledStudents;
    
    public CourseDetailResponse() {}
    
    // New fields getters/setters
    public CourseResponse getCourse() { return course; }
    public void setCourse(CourseResponse course) { this.course = course; }
    
    public Integer getTotalEnrollments() { return totalEnrollments; }
    public void setTotalEnrollments(Integer totalEnrollments) { this.totalEnrollments = totalEnrollments; }
    
    public Boolean getCanAccessContent() { return canAccessContent; }
    public void setCanAccessContent(Boolean canAccessContent) { this.canAccessContent = canAccessContent; }
    
    // Legacy getters/setters for backward compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public Integer getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
    
    public Boolean getIsEnrolled() { return isEnrolled; }
    public void setIsEnrolled(Boolean isEnrolled) { this.isEnrolled = isEnrolled; }
    
    public Boolean getCanEnroll() { return canEnroll; }
    public void setCanEnroll(Boolean canEnroll) { this.canEnroll = canEnroll; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public List<EnrollmentResponse> getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(List<EnrollmentResponse> enrolledStudents) { this.enrolledStudents = enrolledStudents; }
}