package com.studygrind.dto.response;

public class CourseDetailResponse {
    private CourseResponse course;
    private Integer totalEnrollments;
    private Boolean canAccessContent;
    
    public CourseDetailResponse() {}
    
    public CourseResponse getCourse() { return course; }
    public void setCourse(CourseResponse course) { this.course = course; }
    
    public Integer getTotalEnrollments() { return totalEnrollments; }
    public void setTotalEnrollments(Integer totalEnrollments) { this.totalEnrollments = totalEnrollments; }
    
    public Boolean getCanAccessContent() { return canAccessContent; }
    public void setCanAccessContent(Boolean canAccessContent) { this.canAccessContent = canAccessContent; }
}