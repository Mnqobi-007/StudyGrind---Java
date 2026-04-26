package com.studygrind.controller;

import com.studygrind.dto.response.SubscriptionStatusResponse;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.PayFastService;
import com.studygrind.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private PayFastService payFastService;
    
    @GetMapping("/status")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionStatus(currentUser.getId()));
    }
    
    @PostMapping("/create-payment")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createPayment(@AuthenticationPrincipal UserPrincipal currentUser) {
        Map<String, String> paymentRequest = payFastService.createPaymentRequest(
            currentUser.getId(),
            currentUser.getEmail(),
            currentUser.getFullName()
        );
        
        Map<String, Object> response = new HashMap<>();
        
        if (payFastService.isPayFastEnabled()) {
            // Sandbox Payment Initialization
            response.put("paymentUrl", "https://sandbox.payfast.co.za/eng/process");
            response.put("paymentData", paymentRequest);
            response.put("mode", "live");
        } else {
            response.put("mode", "mock");
            response.put("message", "PayFast not configured. This is a demo mode. No payment will be processed.");
            response.put("mockPayment", true);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/mock-subscribe")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> mockSubscribe(@AuthenticationPrincipal UserPrincipal currentUser) {
        payFastService.createMockSubscription(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Mock subscription activated! (No payment was processed)");
        response.put("redirect", "/student/dashboard");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/confirm")
    @PreAuthorize("hasRole('STUDENT')")
    // Subscription Check
    public ResponseEntity<?> confirmSubscription(@AuthenticationPrincipal UserPrincipal currentUser,
                                                  @RequestBody Map<String, String> request) {
        payFastService.createMockSubscription(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Subscription activated successfully!");
        
        return ResponseEntity.ok(response);
    }
}