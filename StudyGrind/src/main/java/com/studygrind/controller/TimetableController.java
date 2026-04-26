package com.studygrind.controller;

import com.studygrind.dto.request.TimetableRequest;
import com.studygrind.dto.response.TimetableResponse;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.TimetableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    @Autowired
    private TimetableService timetableService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TimetableResponse>> getTimetable(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(timetableService.getStudentTimetableView(currentUser.getId()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<TimetableResponse>> getMyTimetable(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(timetableService.getEntriesForStudent(currentUser.getId()));
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<TimetableResponse>> getTeacherTimetable(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(timetableService.getTeacherTimetable(currentUser.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createTimetableEntry(@Valid @RequestBody TimetableRequest request,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            timetableService.createEntry(request, currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Timetable entry created successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createTeacherTimetableEntry(@Valid @RequestBody TimetableRequest request,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            timetableService.createTeacherEntry(request, currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Teacher schedule slot created successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{entryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateTimetableEntry(@PathVariable Long entryId,
                                                  @Valid @RequestBody TimetableRequest request,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            timetableService.updateEntry(entryId, request, currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Timetable entry updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{entryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteTimetableEntry(@PathVariable Long entryId,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            timetableService.deleteEntry(entryId, currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Timetable entry deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}