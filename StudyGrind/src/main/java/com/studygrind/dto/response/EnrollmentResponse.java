package com.studygrind.dto.response;

import java.time.LocalDateTime;

public class EnrollmentResponse {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private LocalDateTime enrolledAt;
    private Boolean trialActive;
    private Boolean subscriptionActive;
    private LocalDateTime enrollmentDate; // Legacy field for compatibility
    
    public EnrollmentResponse() {}
    
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { 
        this.enrolledAt = enrolledAt;
        this.enrollmentDate = enrolledAt; // Sync with legacy field
    }
    
    public Boolean getTrialActive() { return trialActive; }
    public void setTrialActive(Boolean trialActive) { this.trialActive = trialActive; }
    
    public Boolean getSubscriptionActive() { return subscriptionActive; }
    public void setSubscriptionActive(Boolean subscriptionActive) { this.subscriptionActive = subscriptionActive; }
    
    // Legacy getter/setter for backward compatibility
    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime enrollmentDate) { 
        this.enrollmentDate = enrollmentDate;
        this.enrolledAt = enrollmentDate;
    }
}