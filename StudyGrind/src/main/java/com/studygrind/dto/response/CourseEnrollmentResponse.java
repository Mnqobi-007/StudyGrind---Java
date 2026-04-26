package com.studygrind.dto.response;

public class CourseEnrollmentResponse {
    private Long enrollmentId;
    private Long courseId;
    private String courseName;
    private Double coursePrice;
    private Double outstandingBalance;
    private Boolean trialActive;
    private Boolean trialUsed;
    private Long trialDaysRemaining;
    private Boolean subscriptionActive;
    private Long subscriptionDaysRemaining;
    private Boolean canAccessContent;
    private String paymentStatus;
    
    public CourseEnrollmentResponse() {}
    
    // Getters and Setters
    public Long getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(Long enrollmentId) { this.enrollmentId = enrollmentId; }
    
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public Double getCoursePrice() { return coursePrice; }
    public void setCoursePrice(Double coursePrice) { this.coursePrice = coursePrice; }
    
    public Double getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(Double outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    
    public Boolean getTrialActive() { return trialActive; }
    public void setTrialActive(Boolean trialActive) { this.trialActive = trialActive; }
    
    public Boolean getTrialUsed() { return trialUsed; }
    public void setTrialUsed(Boolean trialUsed) { this.trialUsed = trialUsed; }
    
    public Long getTrialDaysRemaining() { return trialDaysRemaining; }
    public void setTrialDaysRemaining(Long trialDaysRemaining) { this.trialDaysRemaining = trialDaysRemaining; }
    
    public Boolean getSubscriptionActive() { return subscriptionActive; }
    public void setSubscriptionActive(Boolean subscriptionActive) { this.subscriptionActive = subscriptionActive; }
    
    public Long getSubscriptionDaysRemaining() { return subscriptionDaysRemaining; }
    public void setSubscriptionDaysRemaining(Long subscriptionDaysRemaining) { this.subscriptionDaysRemaining = subscriptionDaysRemaining; }
    
    public Boolean getCanAccessContent() { return canAccessContent; }
    public void setCanAccessContent(Boolean canAccessContent) { this.canAccessContent = canAccessContent; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    // Helper methods
    public boolean needsPayment() {
        return outstandingBalance != null && outstandingBalance > 0 && !Boolean.TRUE.equals(trialActive);
    }
    
    public String getFormattedOutstandingBalance() {
        return String.format("$%.2f", outstandingBalance != null ? outstandingBalance : 0.0);
    }
}