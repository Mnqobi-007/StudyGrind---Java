package com.studygrind.controller;

import com.studygrind.dto.request.*;
import com.studygrind.dto.response.*;
import com.studygrind.model.Assignment;
import com.studygrind.model.Submission;
import com.studygrind.model.User;
import com.studygrind.repository.SubmissionRepository;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private NoteService noteService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private StatsService statsService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TextbookService textbookService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private TimetableService timetableService;

    // ==================== PROFILE API ====================

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userService.findById(currentUser.getId());
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("fullName", user.getFullName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("address", user.getAddress());
        profile.put("studentNumber", user.getStudentNumber());
        profile.put("profileCompleted", user.getProfileCompleted() != null && user.getProfileCompleted());
        profile.put("emailVerified", user.getEmailVerified() != null && user.getEmailVerified());
        profile.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> request,
                                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            User user = userService.findById(currentUser.getId());
            if (request.containsKey("fullName") && request.get("fullName") != null && !request.get("fullName").trim().isEmpty()) {
                user.setFullName(request.get("fullName"));
            }
            if (request.containsKey("phoneNumber") && request.get("phoneNumber") != null) {
                user.setPhoneNumber(request.get("phoneNumber"));
            }
            if (request.containsKey("address") && request.get("address") != null) {
                user.setAddress(request.get("address"));
            }
            if (request.containsKey("studentNumber") && request.get("studentNumber") != null && "student".equals(user.getRole())) {
                user.setStudentNumber(request.get("studentNumber"));
            }
            userService.updateUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ==================== ANNOUNCEMENTS API ====================

    @PostMapping("/announcements")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createAnnouncement(@Valid @RequestBody AnnouncementRequest request,
                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            if (request.getCourseId() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Course ID is required"));
            }
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Title is required"));
            }
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Content is required"));
            }
            announcementService.createAnnouncement(request.getCourseId(), currentUser.getId(), request.getTitle(), request.getContent());
            return ResponseEntity.ok(Map.of("success", true, "message", "Announcement posted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/announcements")
    public ResponseEntity<?> getAnnouncements(@RequestParam(value = "courseId", required = false) Long courseId,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            if ("teacher".equals(currentUser.getRole()) || "admin".equals(currentUser.getRole())) {
                if (courseId != null) {
                    return ResponseEntity.ok(announcementService.getCourseAnnouncementsAsMap(courseId));
                }
                return ResponseEntity.ok(new ArrayList<>());
            } else {
                return ResponseEntity.ok(announcementService.getAllAnnouncementsForStudentAsMap(currentUser.getId()));
            }
        } catch (Exception e) {
            logger.error("Error loading announcements: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== NOTES API ====================

    @GetMapping("/notes")
    public ResponseEntity<?> getNotes(@RequestParam(value = "courseId", required = false) Long courseId,
                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.ok(new ArrayList<>());
        try {
            if ("teacher".equals(currentUser.getRole()) || "admin".equals(currentUser.getRole())) {
                if (courseId != null) {
                    return ResponseEntity.ok(noteService.getNotesForTeacher(currentUser.getId(), courseId));
                } else {
                    List<CourseResponse> teacherCourses = courseService.getCoursesForTeacher(currentUser.getId());
                    List<NoteResponse> allNotes = new ArrayList<>();
                    for (CourseResponse course : teacherCourses) {
                        try {
                            List<NoteResponse> courseNotes = noteService.getNotesForTeacher(currentUser.getId(), course.getId());
                            allNotes.addAll(courseNotes);
                        } catch (Exception e) {
                            logger.warn("Could not load notes for course {}: {}", course.getId(), e.getMessage());
                        }
                    }
                    logger.info("Teacher {} has {} total notes across {} courses",
                            currentUser.getId(), allNotes.size(), teacherCourses.size());
                    return ResponseEntity.ok(allNotes);
                }
            } else {
                if (courseId != null) {
                    return ResponseEntity.ok(noteService.getNotesForStudent(currentUser.getId(), courseId));
                }
                return ResponseEntity.ok(noteService.getAllNotesForStudent(currentUser.getId()));
            }
        } catch (RuntimeException e) {
            logger.error("Error loading notes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<?> getNoteDetails(@PathVariable Long noteId, @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(noteService.getNoteDetails(noteId, currentUser.getId(), currentUser.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/notes")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createNote(@Valid @RequestBody NoteRequest request, @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            if (request.getCourseId() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Course ID is required"));
            }
            noteService.createNote(request, currentUser.getId(), request.getCourseId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Note created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/notes/{noteId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateNote(@PathVariable Long noteId, @Valid @RequestBody NoteRequest request,
                                        @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            NoteResponse updatedNote = noteService.updateNote(noteId, request, currentUser.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Note updated successfully", "note", updatedNote));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/notes/{noteId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId, @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // FIXED: Pass all 3 required parameters
            noteService.deleteNote(noteId, currentUser.getId(), currentUser.getRole());
            return ResponseEntity.ok(Map.of("success", true, "message", "Note deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== ASSIGNMENTS API ====================

    @GetMapping("/assignments")
    public ResponseEntity<?> getAssignments(@RequestParam(value = "courseId", required = false) Long courseId,
                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            if ("teacher".equals(currentUser.getRole())) {
                return ResponseEntity.ok(assignmentService.getAssignmentsForTeacher(currentUser.getId(), courseId));
            } else {
                if (courseId != null) {
                    return ResponseEntity.ok(assignmentService.getAssignmentsForStudent(currentUser.getId(), courseId));
                }
                return ResponseEntity.ok(assignmentService.getAllAssignmentsForStudent(currentUser.getId()));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createAssignment(@Valid @RequestBody AssignmentRequest request, @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            if (request.getCourseId() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Course ID is required"));
            }
            assignmentService.createAssignment(request, currentUser.getId(), request.getCourseId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Assignment created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping(value = "/assignments/{assignmentId}/submit", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitAssignment(@PathVariable Long assignmentId,
                                              @RequestParam(value = "content", required = false) String content,
                                              @RequestParam(value = "file", required = false) MultipartFile file,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            SubmissionRequest request = new SubmissionRequest();
            request.setContent(content);
            assignmentService.submitAssignment(assignmentId, currentUser.getId(), request, file);
            return ResponseEntity.ok(Map.of("success", true, "message", "Assignment submitted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/submissions")
    public ResponseEntity<?> getSubmissions(@RequestParam(value = "courseId", required = false) Long courseId,
                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            if ("teacher".equals(currentUser.getRole())) {
                return ResponseEntity.ok(assignmentService.getSubmissionsForTeacher(currentUser.getId(), courseId));
            } else {
                return ResponseEntity.ok(assignmentService.getSubmissionsForStudent(currentUser.getId()));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/submissions/{submissionId}/download")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadSubmissionFile(@PathVariable Long submissionId, @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            byte[] fileContent = assignmentService.downloadSubmissionFile(submissionId, currentUser.getId());
            com.studygrind.model.Submission submission = assignmentService.getSubmissionById(submissionId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + submission.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @PostMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> gradeSubmission(@PathVariable Long submissionId, @Valid @RequestBody GradeRequest request) {
        try {
            assignmentService.gradeSubmission(submissionId, request.getScore(), request.getFeedback());
            return ResponseEntity.ok(Map.of("success", true, "message", "Grade saved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== QUIZZES API ====================

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizResponse>> getQuizzes(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        if ("teacher".equals(currentUser.getRole())) {
            return ResponseEntity.ok(quizService.getQuizzesForTeacher(currentUser.getId()));
        } else {
            return ResponseEntity.ok(quizService.getQuizzesForStudent(currentUser.getId()));
        }
    }

    @PostMapping("/quizzes")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createQuiz(@Valid @RequestBody QuizRequest request, @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            quizService.createQuiz(request, currentUser.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Quiz created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> addQuestion(@PathVariable Long quizId, @Valid @RequestBody QuestionRequest request) {
        try {
            quizService.addQuestion(quizId, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Question added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/quizzes/{quizId}/take")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizResponse> takeQuiz(@PathVariable Long quizId, @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(quizService.getQuizForTaking(quizId, currentUser.getId()));
    }

    @PostMapping("/quizzes/{quizId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitQuiz(@PathVariable Long quizId, @RequestBody QuizSubmitRequest request, @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            Double score = quizService.submitQuiz(quizId, currentUser.getId(), request);
            return ResponseEntity.ok(Map.of("success", true, "score", score, "message", "Quiz submitted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/quizzes/{quizId}/results")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizResponse> getQuizResults(@PathVariable Long quizId, @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(quizService.getQuizResults(quizId, currentUser.getId()));
    }

    // ==================== STATS API ====================

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        if ("teacher".equals(currentUser.getRole())) {
            return ResponseEntity.ok(statsService.getTeacherStats(currentUser.getId()));
        } else if ("student".equals(currentUser.getRole())) {
            return ResponseEntity.ok(statsService.getStudentStats(currentUser.getId()));
        } else {
            return ResponseEntity.ok(Map.of("message", "Admin stats available in admin panel"));
        }
    }

    // ==================== ADMIN API ====================

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getTeachers() {
        return ResponseEntity.ok(userService.getAllTeachers());
    }

    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addTeacher(@RequestBody Map<String, String> request) {
        try {
            String fullName = request.get("full_name");
            String email = request.get("email");
            String username = request.get("username");
            String password = request.getOrDefault("password", "password123");
            userService.createTeacher(fullName, email, username, password);
            return ResponseEntity.ok(Map.of("success", true, "message", "Teacher added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteAccount(userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getStudents() {
        return ResponseEntity.ok(userService.getAllStudents());
    }
}