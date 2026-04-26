package com.studygrind.controller;

import com.studygrind.dto.response.PaymentSummaryResponse;
import com.studygrind.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @GetMapping("/payments/summary")
    public ResponseEntity<PaymentSummaryResponse> getPaymentSummary() {
        return ResponseEntity.ok(adminService.getPaymentSummary());
    }
}