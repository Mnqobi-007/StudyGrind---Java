package com.studygrind.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_payments")
public class CoursePayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CourseEnrollment enrollment;
    
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "payment_id")
    private String paymentId;
    
    @Column(name = "billing_cycle_start")
    private LocalDateTime billingCycleStart;
    
    @Column(name = "billing_cycle_end")
    private LocalDateTime billingCycleEnd;
    
    @Column(name = "status")
    private String status = "completed";
    
    public CoursePayment() {
        this.paymentDate = LocalDateTime.now();
    }
    
    public CoursePayment(CourseEnrollment enrollment, Double amount, String paymentMethod, String paymentId) {
        this.enrollment = enrollment;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentId = paymentId;
        this.paymentDate = LocalDateTime.now();
        this.billingCycleStart = LocalDateTime.now();
        this.billingCycleEnd = this.billingCycleStart.plusDays(30);
        this.status = "completed";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CourseEnrollment getEnrollment() { return enrollment; }
    public void setEnrollment(CourseEnrollment enrollment) { this.enrollment = enrollment; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public LocalDateTime getBillingCycleStart() { return billingCycleStart; }
    public void setBillingCycleStart(LocalDateTime billingCycleStart) { this.billingCycleStart = billingCycleStart; }
    public LocalDateTime getBillingCycleEnd() { return billingCycleEnd; }
    public void setBillingCycleEnd(LocalDateTime billingCycleEnd) { this.billingCycleEnd = billingCycleEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}