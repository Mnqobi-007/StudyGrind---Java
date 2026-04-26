package com.studygrind.dto.response;

import java.util.List;

public class PaymentSummaryResponse {
    private Double totalRevenue;
    private Integer activeSubscriptions;
    private Double monthlyRevenue;
    private List<PaymentResponse> recentPayments;
    
    public PaymentSummaryResponse() {}
    
    // Getters and Setters
    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    public Integer getActiveSubscriptions() { return activeSubscriptions; }
    public void setActiveSubscriptions(Integer activeSubscriptions) { this.activeSubscriptions = activeSubscriptions; }
    public Double getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(Double monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    public List<PaymentResponse> getRecentPayments() { return recentPayments; }
    public void setRecentPayments(List<PaymentResponse> recentPayments) { this.recentPayments = recentPayments; }
}