package com.studygrind.controller;

import com.studygrind.config.SessionHelper;
import com.studygrind.dto.AddTeacherRequest;
import com.studygrind.model.User;
import com.studygrind.service.AuthService;
import com.studygrind.service.TeacherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TeacherController {
    private TeacherService teacherService;
    private SessionHelper sessionHelper;
    private AuthService authService;

    @Autowired
    public TeacherController(TeacherService teacherService, SessionHelper sessionHelper, AuthService authService) {
        this.teacherService = teacherService;
        this.sessionHelper = sessionHelper;
        this.authService = authService;
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<User>> getAllTeachers(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        List<User> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    }

    @PostMapping("/teachers")
    public ResponseEntity<Map<String, Object>> addTeacher(@RequestBody AddTeacherRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can add teachers"));
        }
        try {
            User teacher = teacherService.addTeacher(request);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", teacher.getId());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    @GetMapping("/students")
    public ResponseEntity<List<User>> getAllStudents(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isTeacher() && !user.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        List<User> students = teacherService.getAllStudents();
        return ResponseEntity.ok(students);
    }
}
