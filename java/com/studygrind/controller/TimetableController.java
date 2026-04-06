package com.studygrind.controller;

import com.studygrind.config.SessionHelper;
import com.studygrind.dto.CreateTimetableRequest;
import com.studygrind.model.Timetable;
import com.studygrind.model.User;
import com.studygrind.service.AuthService;
import com.studygrind.service.TimetableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {
    private TimetableService timetableService;
    private SessionHelper sessionHelper;
    private AuthService authService;

    @Autowired
    public TimetableController(TimetableService timetableService, SessionHelper sessionHelper, AuthService authService) {
        this.timetableService = timetableService;
        this.sessionHelper = sessionHelper;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<Timetable>> getTimetable(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isStudent()) {
            return ResponseEntity.status(403).build();
        }
        List<Timetable> entries = timetableService.getTimetable(user.getId());
        return ResponseEntity.ok(entries);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEntry(@RequestBody CreateTimetableRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isStudent()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can create timetable entries"));
        }
        try {
            Timetable entry = timetableService.createEntry(request, user);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("id", entry.getId());
            result.put("message", "Timetable entry created successfully");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEntry(@PathVariable Long id, @RequestBody CreateTimetableRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isStudent()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can update timetable entries"));
        }
        try {
            Timetable entry = timetableService.updateEntry(id, request, user);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Timetable entry updated successfully");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEntry(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isStudent()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can delete timetable entries"));
        }
        try {
            timetableService.deleteEntry(id, user);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Timetable entry deleted successfully");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }
}
