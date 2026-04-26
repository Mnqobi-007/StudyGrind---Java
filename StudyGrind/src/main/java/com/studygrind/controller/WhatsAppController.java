package com.studygrind.controller;

import com.studygrind.security.UserPrincipal;
import com.studygrind.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {
    
    @Autowired
    private WhatsAppService whatsAppService;
    
    @PostMapping("/request-access")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> requestAccess(@AuthenticationPrincipal UserPrincipal currentUser) {
        String link = whatsAppService.requestAccess(currentUser.getId(), currentUser.getEmail(), currentUser.getFullName());
        Map<String, Object> response = new HashMap<>();
        
        if (link != null) {
            response.put("link", link);
            response.put("approved", true);
        } else {
            response.put("approved", false);
            response.put("message", "Access request submitted. You will receive the link once approved.");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/approve/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveUser(@PathVariable Long userId) {
        whatsAppService.approveUser(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User approved for WhatsApp group");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/revoke/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> revokeAccess(@PathVariable Long userId) {
        whatsAppService.removeAccess(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Access revoked");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingRequests() {
        return ResponseEntity.ok(whatsAppService.getAllPendingRequests());
    }
}