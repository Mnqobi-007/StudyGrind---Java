package com.studygrind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
public class CourseEnrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;
    
    @Column(name = "trial_start_date")
    private LocalDateTime trialStartDate;
    
    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;
    
    @Column(name = "trial_used")
    private Boolean trialUsed = false;
    
    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;
    
    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;
    
    @Column(name = "subscription_active")
    private Boolean subscriptionActive = false;
    
    @Column(name = "outstanding_balance")
    private Double outstandingBalance = 0.0;
    
    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate;
    
    @Column(name = "payment_status")
    private String paymentStatus = "pending";
    
    @Column(name = "is_enrolled")
    private Boolean isEnrolled = true;
    
    @Column(name = "unenrolled_date")
    private LocalDateTime unenrolledDate;
    
    @Column(name = "unenrollment_reason")
    private String unenrollmentReason;
    
    public CourseEnrollment() {
        this.enrollmentDate = LocalDateTime.now();
    }
    
    public CourseEnrollment(User student, Course course) {
        this.student = student;
        this.course = course;
        this.enrollmentDate = LocalDateTime.now();
        this.isEnrolled = true;
        this.outstandingBalance = course.getPrice();
        this.paymentStatus = "pending";
        this.trialUsed = false;
    }
    
    public void startTrial() {
        if (Boolean.TRUE.equals(trialUsed)) {
            throw new RuntimeException("Trial already used for this course");
        }
        this.trialStartDate = LocalDateTime.now();
        this.trialEndDate = this.trialStartDate.plusDays(14);
        this.trialUsed = true;
        this.outstandingBalance = this.course.getPrice();
    }
    
    public boolean isTrialActive() {
        return Boolean.TRUE.equals(trialUsed) && 
               trialEndDate != null && 
               LocalDateTime.now().isBefore(trialEndDate);
    }
    
    public boolean isTrialExpired() {
        return Boolean.TRUE.equals(trialUsed) && 
               trialEndDate != null && 
               LocalDateTime.now().isAfter(trialEndDate);
    }
    
    public boolean isSubscriptionExpired() {
        return Boolean.TRUE.equals(subscriptionActive) && 
               subscriptionEndDate != null && 
               LocalDateTime.now().isAfter(subscriptionEndDate);
    }
    
    public long getTrialDaysRemaining() {
        if (!isTrialActive()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), trialEndDate).toDays();
    }
    
    public long getSubscriptionDaysRemaining() {
        if (!Boolean.TRUE.equals(subscriptionActive) || subscriptionEndDate == null) return 0;
        if (LocalDateTime.now().isAfter(subscriptionEndDate)) return 0;
        return java.time.Duration.between(LocalDateTime.now(), subscriptionEndDate).toDays();
    }
    
    public boolean canAccessContent() {
        return Boolean.TRUE.equals(isEnrolled) && 
               (isTrialActive() || (Boolean.TRUE.equals(subscriptionActive) && !isSubscriptionExpired()));
    }
    
    public void unenroll(String reason) {
        this.isEnrolled = false;
        this.unenrolledDate = LocalDateTime.now();
        this.unenrollmentReason = reason;
        this.subscriptionActive = false;
        this.outstandingBalance = 0.0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public LocalDateTime getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(LocalDateTime trialStartDate) { this.trialStartDate = trialStartDate; }
    public LocalDateTime getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDateTime trialEndDate) { this.trialEndDate = trialEndDate; }
    public Boolean getTrialUsed() { return trialUsed; }
    public void setTrialUsed(Boolean trialUsed) { this.trialUsed = trialUsed; }
    public LocalDateTime getSubscriptionStartDate() { return subscriptionStartDate; }
    public void setSubscriptionStartDate(LocalDateTime subscriptionStartDate) { this.subscriptionStartDate = subscriptionStartDate; }
    public LocalDateTime getSubscriptionEndDate() { return subscriptionEndDate; }
    public void setSubscriptionEndDate(LocalDateTime subscriptionEndDate) { this.subscriptionEndDate = subscriptionEndDate; }
    public Boolean getSubscriptionActive() { return subscriptionActive; }
    public void setSubscriptionActive(Boolean subscriptionActive) { this.subscriptionActive = subscriptionActive; }
    public Double getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(Double outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    public LocalDateTime getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(LocalDateTime lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Boolean getIsEnrolled() { return isEnrolled; }
    public void setIsEnrolled(Boolean isEnrolled) { this.isEnrolled = isEnrolled; }
    public LocalDateTime getUnenrolledDate() { return unenrolledDate; }
    public void setUnenrolledDate(LocalDateTime unenrolledDate) { this.unenrolledDate = unenrolledDate; }
    public String getUnenrollmentReason() { return unenrollmentReason; }
    public void setUnenrollmentReason(String unenrollmentReason) { this.unenrollmentReason = unenrollmentReason; }
}