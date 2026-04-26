package com.studygrind.controller;

import com.studygrind.model.User;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ResponseEntity<List<User>> getPendingVerifications() {
        return ResponseEntity.ok(userService.getPendingVerificationStudents());
    }

    @GetMapping("/student-card/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getStudentCard(@PathVariable Long studentId) {
        try {
            byte[] imageBytes = userService.getStudentCardImage(studentId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"student-card.jpg\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/verify/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verifyStudent(@PathVariable Long studentId,
                                            @RequestBody Map<String, Object> request,
                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            boolean approve = (boolean) request.getOrDefault("approve", false);
            String notes = (String) request.get("notes");
            
            User verified = userService.verifyStudent(studentId, currentUser.getId(), approve, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", approve ? "Student verified successfully" : "Student verification rejected");
            response.put("verificationStatus", verified.getVerificationStatus());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/resubmit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> resubmitStudentCard(@RequestParam("studentCard") MultipartFile studentCardFile,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            userService.resubmitStudentCard(currentUser.getId(), studentCardFile);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Student card resubmitted successfully. Please wait for admin approval."
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to upload student card: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}