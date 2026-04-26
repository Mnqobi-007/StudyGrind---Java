package com.studygrind.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public class CourseRequest {
    @NotBlank(message = "Course name is required")
    private String name;
    
    private String description;
    
    // Optional fields - no @NotNull validation
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private Long teacherId;
    
    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price;
    
    public CourseRequest() {}
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}