package com.studygrind.dto.response;

import java.time.LocalDateTime;

public class TimetableResponse {
    private Long id;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String subject;
    private String location;
    private String notes;
    private LocalDateTime createdAt;
    private String teacherName;
    private String teacherRole;

    public TimetableResponse() {}

    // Getters
    public Long getId() { return id; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getSubject() { return subject; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getTeacherName() { return teacherName; }
    public String getTeacherRole() { return teacherRole; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setLocation(String location) { this.location = location; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public void setTeacherRole(String teacherRole) { this.teacherRole = teacherRole; }
}