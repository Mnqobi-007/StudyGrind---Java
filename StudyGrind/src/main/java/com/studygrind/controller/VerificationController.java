package com.studygrind.controller;

import com.studygrind.dto.response.UserResponse;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    @Autowired
    private UserService userService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getPendingVerifications() {
        return ResponseEntity.ok(userService.getPendingVerificationStudents());
    }

    @GetMapping("/student-card/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getStudentCard(@PathVariable Long studentId) {
        try {
            byte[] imageData = userService.getStudentCardImage(studentId);
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(imageData);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/verify/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> verifyStudent(
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        try {
            boolean approve = (boolean) request.getOrDefault("approve", false);
            String notes = (String) request.getOrDefault("notes", "");
            
            userService.verifyStudent(studentId, currentUser.getId(), approve, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", approve ? "Student approved successfully" : "Student rejected");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}