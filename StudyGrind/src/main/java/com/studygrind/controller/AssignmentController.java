package com.studygrind.controller;

import com.studygrind.config.SessionHelper;
import com.studygrind.dto.CreateAssignmentRequest;
import com.studygrind.dto.GradeSubmissionRequest;
import com.studygrind.dto.SubmitAssignmentRequest;
import com.studygrind.model.Assignment;
import com.studygrind.model.Submission;
import com.studygrind.model.User;
import com.studygrind.service.AssignmentService;
import com.studygrind.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AssignmentController {
    private AssignmentService assignmentService;
    private SessionHelper sessionHelper;
    private AuthService authService;

    @Autowired
    public AssignmentController(AssignmentService assignmentService, SessionHelper sessionHelper, AuthService authService) {
        this.assignmentService = assignmentService;
        this.sessionHelper = sessionHelper;
        this.authService = authService;
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<Assignment>> getAssignments(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        List<Assignment> assignments = assignmentService.getAllAssignments(user);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/assignments")
    public ResponseEntity<Map<String, Object>> createAssignment(@RequestBody CreateAssignmentRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isTeacher() && !user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admins can create assignments"));
        }
        Assignment assignment = assignmentService.createAssignment(request, user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", assignment.getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/assignments/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitAssignment(@PathVariable Long id, @RequestBody SubmitAssignmentRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isStudent()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can submit assignments"));
        }
        try {
            Submission submission = assignmentService.submitAssignment(id, request, user);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", submission.getId());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<Submission>> getSubmissions(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        List<Submission> submissions = assignmentService.getSubmissions(user);
        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/submissions/{id}/grade")
    public ResponseEntity<Map<String, Object>> gradeSubmission(@PathVariable Long id, @RequestBody GradeSubmissionRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isTeacher() && !user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admins can grade submissions"));
        }
        Submission submission = assignmentService.gradeSubmission(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}
