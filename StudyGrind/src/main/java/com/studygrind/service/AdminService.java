package com.studygrind.service;

import com.studygrind.dto.response.PaymentResponse;
import com.studygrind.dto.response.PaymentSummaryResponse;
import com.studygrind.model.Subscription;
import com.studygrind.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Cacheable(value = "paymentSummary")
    public PaymentSummaryResponse getPaymentSummary() {
        PaymentSummaryResponse response = new PaymentSummaryResponse();
        
        Double totalRevenue = subscriptionRepository.getTotalRevenue();
        Long activeSubscriptions = subscriptionRepository.getActiveSubscriptionsCount();
        Double monthlyRevenue = subscriptionRepository.getMonthlyRevenue();
        
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);
        response.setActiveSubscriptions(activeSubscriptions != null ? activeSubscriptions.intValue() : 0);
        response.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : 0.0);
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Subscription> recentSubscriptions = subscriptionRepository.findRecentPayments(pageable);
        
        List<PaymentResponse> recentPayments = new ArrayList<>();
        for (Subscription sub : recentSubscriptions) {
            PaymentResponse payment = new PaymentResponse();
            payment.setId(sub.getId());
            payment.setStudentName(sub.getStudent().getFullName());
            payment.setAmount(sub.getAmount());
            payment.setPaymentDate(sub.getPaymentDate());
            payment.setStatus(sub.getStatus());
            recentPayments.add(payment);
        }
        response.setRecentPayments(recentPayments);
        
        return response;
    }
}