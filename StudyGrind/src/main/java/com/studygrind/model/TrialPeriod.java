package com.studygrind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trial_periods")
public class TrialPeriod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "trial_days")
    private Integer trialDays = 14;
    
    public TrialPeriod() {}
    
    public TrialPeriod(User student, Integer trialDays) {
        this.student = student;
        this.trialDays = trialDays;
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusDays(trialDays);
        this.isActive = true;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
    
    public long getRemainingDays() {
        if (isExpired()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), endDate).toDays();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Integer getTrialDays() { return trialDays; }
    public void setTrialDays(Integer trialDays) { this.trialDays = trialDays; }
}