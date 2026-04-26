package com.studygrind.controller;

import com.studygrind.dto.request.CourseRequest;
import com.studygrind.dto.response.CourseDetailResponse;
import com.studygrind.dto.response.CourseResponse;
import com.studygrind.dto.response.EnrollmentResponse;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.CourseService;
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
@RequestMapping("/api/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
    
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseResponse>> getMyCourses(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.getCoursesForStudent(currentUser.getId()));
    }
    
    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseResponse>> getAvailableCourses(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.getAvailableCoursesForStudent(currentUser.getId()));
    }
    
    @GetMapping("/my-course")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseResponse> getMyCourse(@AuthenticationPrincipal UserPrincipal currentUser) {
        CourseResponse course = courseService.getCourseForTeacher(currentUser.getId());
        if (course == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(course);
    }
    
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getTeacherCourses(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.getCoursesForTeacher(currentUser.getId()));
    }
    
    @GetMapping("/{courseId}/details")
    public ResponseEntity<CourseDetailResponse> getCourseDetails(@PathVariable Long courseId,
                                                                   @AuthenticationPrincipal UserPrincipal currentUser) {
        Long studentId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(courseService.getCourseDetails(courseId, studentId));
    }
    
    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enrollInCourse(@PathVariable Long courseId,
                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // Check if already used trial for this course
            if (courseService.hasUsedTrial(currentUser.getId(), courseId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "You have already used your free trial for this course");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            courseService.enrollStudent(currentUser.getId(), courseId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully enrolled in course! Your 14-day trial has started.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/{courseId}/unenroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> unenrollFromCourse(@PathVariable Long courseId,
                                                 @AuthenticationPrincipal UserPrincipal currentUser,
                                                 @RequestBody(required = false) Map<String, String> request) {
        try {
            String reason = request != null ? request.getOrDefault("reason", "User requested") : "User requested";
            courseService.unenrollStudent(currentUser.getId(), courseId, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully unenrolled from course");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createCourse(@Valid @RequestBody CourseRequest request,
                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        courseService.createCourse(request, currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Course created successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/enrolled-students/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrolledStudents(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getEnrolledStudents(courseId));
    }
}