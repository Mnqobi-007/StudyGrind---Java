package com.studygrind.controller;

import com.studygrind.dto.request.PayFastNotifyRequest;
import com.studygrind.service.PayFastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payfast")
public class PayFastWebhookController {
    
    @Autowired
    private PayFastService payFastService;
    
    @PostMapping("/notify")
    public ResponseEntity<String> handlePayFastNotify(@RequestParam Map<String, String> params) {
        // If PayFast is not enabled, just return OK (ignore)
        if (!payFastService.isPayFastEnabled()) {
            return ResponseEntity.ok("OK - PayFast disabled");
        }
        
        try {
            PayFastNotifyRequest notifyRequest = PayFastNotifyRequest.fromMap(params);
            boolean processed = payFastService.processPaymentNotification(notifyRequest);
            
            if (processed) {
                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid notification");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }
}