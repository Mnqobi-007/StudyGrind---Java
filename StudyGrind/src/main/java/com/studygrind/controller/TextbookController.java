package com.studygrind.controller;

import com.studygrind.dto.response.TextbookResponse;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.TextbookService;
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
@RequestMapping("/api/textbooks")
public class TextbookController {

    @Autowired
    private TextbookService textbookService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadTextbook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("courseId") Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        try {
            if (file == null || file.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (file.getSize() > 50 * 1024 * 1024) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "File size exceeds 50MB limit");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            var textbook = textbookService.uploadTextbook(file, title, description, courseId, currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Textbook uploaded successfully");
            response.put("textbookId", textbook.getId());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getTextbooksByCourse(@PathVariable Long courseId) {
        try {
            List<TextbookResponse> textbooks = textbookService.getTextbooksForCourse(courseId);
            return ResponseEntity.ok(textbooks);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/download/{textbookId}")
    public ResponseEntity<byte[]> downloadTextbook(
            @PathVariable Long textbookId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Long userId = currentUser != null ? currentUser.getId() : null;
            String userRole = currentUser != null ? currentUser.getRole() : null;
            byte[] fileContent = textbookService.downloadTextbook(textbookId, userId, userRole);
            TextbookResponse textbook = textbookService.getTextbookDetails(textbookId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + textbook.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(null);
        }
    }
}