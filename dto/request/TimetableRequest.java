package com.studygrind.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TimetableRequest {
    @NotBlank(message = "Day of week is required")
    @Pattern(regexp = "Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday", 
             message = "Day must be a valid day of week")
    private String dayOfWeek;
    
    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", 
             message = "Start time must be in HH:MM format")
    private String startTime;
    
    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", 
             message = "End time must be in HH:MM format")
    private String endTime;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    private String location;
    private String notes;
    
    public TimetableRequest() {}
    
    // Getters and Setters
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}