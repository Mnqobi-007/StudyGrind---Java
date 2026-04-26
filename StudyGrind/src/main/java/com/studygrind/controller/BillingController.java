package com.studygrind.controller;

import com.studygrind.dto.response.BillingSummaryResponse;
import com.studygrind.dto.response.CourseEnrollmentResponse;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.BillingService;
import com.studygrind.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@PreAuthorize("hasRole('STUDENT')")
public class BillingController {
    
    @Autowired
    private BillingService billingService;
    
    @Autowired
    private CourseService courseService;
    
    @GetMapping("/summary")
    public ResponseEntity<BillingSummaryResponse> getBillingSummary(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(billingService.getBillingSummary(currentUser.getId()));
    }
    
    @GetMapping("/enrollments")
    public ResponseEntity<List<CourseEnrollmentResponse>> getEnrollments(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(billingService.getEnrollmentsWithBillingStatus(currentUser.getId()));
    }
    
    @PostMapping("/pay/{enrollmentId}")
    public ResponseEntity<?> payForCourse(@PathVariable Long enrollmentId,
                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Map<String, Object> paymentRequest = billingService.createPaymentForEnrollment(enrollmentId, currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentRequest", paymentRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/pay-all")
    public ResponseEntity<?> payAllOutstanding(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Map<String, Object> paymentRequest = billingService.createPaymentForAllOutstanding(currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentRequest", paymentRequest);
            response.put("totalAmount", paymentRequest.get("totalAmount"));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/webhook/payfast")
    public ResponseEntity<String> handlePayFastWebhook(@RequestBody Map<String, String> params) {
        boolean processed = billingService.processPaymentNotification(params);
        if (processed) {
            return ResponseEntity.ok("OK");
        }
        return ResponseEntity.badRequest().body("Invalid notification");
    }

    @PostMapping("/mock-pay/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> mockPayForCourse(@PathVariable Long enrollmentId,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            billingService.processMockPaymentForEnrollment(enrollmentId, currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mock payment processed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}