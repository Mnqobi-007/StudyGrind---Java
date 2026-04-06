package com.studygrind.dto.response;

import java.util.List;

public class BillingSummaryResponse {
    private Double totalOutstandingBalance;
    private Integer activeCourses;
    private Integer expiredTrials;
    private Integer activeSubscriptions;
    private List<CourseEnrollmentResponse> enrollments;

    public BillingSummaryResponse() {}

    // Getters and Setters
    public Double getTotalOutstandingBalance() {
        return totalOutstandingBalance;
    }

    public void setTotalOutstandingBalance(Double totalOutstandingBalance) {
        this.totalOutstandingBalance = totalOutstandingBalance;
    }

    public Integer getActiveCourses() {
        return activeCourses;
    }

    public void setActiveCourses(Integer activeCourses) {
        this.activeCourses = activeCourses;
    }

    public Integer getExpiredTrials() {
        return expiredTrials;
    }

    public void setExpiredTrials(Integer expiredTrials) {
        this.expiredTrials = expiredTrials;
    }

    public Integer getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(Integer activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public List<CourseEnrollmentResponse> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<CourseEnrollmentResponse> enrollments) {
        this.enrollments = enrollments;
    }
}