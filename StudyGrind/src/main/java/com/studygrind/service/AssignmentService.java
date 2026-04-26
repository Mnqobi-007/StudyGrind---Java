package com.studygrind.service;

import com.studygrind.dto.request.AssignmentRequest;
import com.studygrind.dto.request.SubmissionRequest;
import com.studygrind.dto.response.AssignmentResponse;
import com.studygrind.dto.response.SubmissionResponse;
import com.studygrind.model.Assignment;
import com.studygrind.model.Course;
import com.studygrind.model.Submission;
import com.studygrind.model.User;
import com.studygrind.repository.AssignmentRepository;
import com.studygrind.repository.CourseRepository;
import com.studygrind.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.upload.dir.submissions:uploads/submissions}")
    private String uploadDir;

    @Transactional
    public Assignment createAssignment(AssignmentRequest request, Long teacherId, Long courseId) {
        User teacher = userService.findById(teacherId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You can only create assignments for your own course");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setSubject(request.getSubject());
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxScore(request.getMaxScore());
        assignment.setTeacher(teacher);
        assignment.setCourse(course);
        assignment.setCreatedAt(LocalDateTime.now());

        Assignment saved = assignmentRepository.save(assignment);

        // Send notification to all enrolled students
        List<User> enrolledStudents = course.getEnrollments().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsEnrolled()))
                .map(e -> e.getStudent())
                .collect(Collectors.toList());

        for (User student : enrolledStudents) {
            notificationService.sendNotification(
                    student.getId(),
                    "New assignment: " + assignment.getTitle() + " is due on " + assignment.getDueDate(),
                    "assignment"
            );
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForStudent(Long studentId, Long courseId) {
        if (!courseService.canAccessContent(studentId, courseId)) {
            throw new RuntimeException("You need an active subscription to access course assignments");
        }

        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        List<AssignmentResponse> responses = new ArrayList<>();
        for (Assignment assignment : assignments) {
            AssignmentResponse response = convertToResponse(assignment, studentId);
            responses.add(response);
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForTeacher(Long teacherId, Long courseId) {
        List<Assignment> assignments;

        if (courseId == null) {
            assignments = assignmentRepository.findAll().stream()
                    .filter(a -> a.getTeacher() != null && a.getTeacher().getId().equals(teacherId))
                    .collect(Collectors.toList());
        } else {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            if (!course.getTeacher().getId().equals(teacherId)) {
                throw new RuntimeException("You can only view assignments for your own course");
            }
            assignments = assignmentRepository.findByCourseId(courseId);
        }

        List<AssignmentResponse> responses = new ArrayList<>();
        for (Assignment assignment : assignments) {
            AssignmentResponse response = convertToResponse(assignment, null);
            int submissionCount = submissionRepository.findByAssignment(assignment).size();
            response.setSubmissions(submissionCount);
            responses.add(response);
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignmentsForStudent(Long studentId) {
        List<Course> enrolledCourses = courseRepository.findCoursesByStudentId(studentId);
        List<AssignmentResponse> allAssignments = new ArrayList<>();

        for (Course course : enrolledCourses) {
            try {
                if (courseService.canAccessContent(studentId, course.getId())) {
                    List<Assignment> assignments = assignmentRepository.findByCourseId(course.getId());
                    for (Assignment assignment : assignments) {
                        AssignmentResponse response = convertToResponse(assignment, studentId);
                        allAssignments.add(response);
                    }
                }
            } catch (RuntimeException e) {
                // Skip courses that can't be accessed
            }
        }

        return allAssignments;
    }

    @Transactional
    public Submission submitAssignment(Long assignmentId, Long studentId, SubmissionRequest request, MultipartFile file) throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        User student = userService.findById(studentId);

        if (!courseService.canAccessContent(studentId, assignment.getCourse().getId())) {
            throw new RuntimeException("You need an active subscription to submit assignments");
        }

        if (submissionRepository.findByAssignmentAndStudent(assignment, student).isPresent()) {
            throw new RuntimeException("You have already submitted this assignment");
        }

        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new RuntimeException("Assignment deadline has passed");
        }

        String filePath = null;
        String fileName = null;

        if (file != null && !file.isEmpty()) {
            // Create upload directory
            String workingDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(workingDir, uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePathObj = uploadPath.resolve(filename);
            file.transferTo(filePathObj.toFile());

            filePath = uploadDir + "/" + filename;
            fileName = originalFilename;
        }

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(request.getContent());
        submission.setFilePath(filePath);
        submission.setFileName(fileName);
        submission.setSubmittedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);

        // Notify teacher
        notificationService.sendNotification(
                assignment.getTeacher().getId(),
                "New submission from " + student.getFullName() + " for assignment: " + assignment.getTitle(),
                "submission"
        );

        return saved;
    }

    @Transactional
    public Submission gradeSubmission(Long submissionId, Double score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (score < 0 || score > submission.getAssignment().getMaxScore()) {
            throw new RuntimeException("Score must be between 0 and " + submission.getAssignment().getMaxScore());
        }

        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setGradedAt(LocalDateTime.now());

        // Notify student
        notificationService.sendNotification(
                submission.getStudent().getId(),
                "Your submission for " + submission.getAssignment().getTitle() + " has been graded. Score: " + score + "/" + submission.getAssignment().getMaxScore() + ". Feedback: " + (feedback != null && !feedback.isEmpty() ? feedback : "No feedback provided"),
                "grade"
        );

        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsForTeacher(Long teacherId, Long courseId) {
        List<Submission> submissions;

        if (courseId == null) {
            submissions = submissionRepository.findAll().stream()
                    .filter(s -> s.getAssignment() != null &&
                            s.getAssignment().getTeacher() != null &&
                            s.getAssignment().getTeacher().getId().equals(teacherId))
                    .collect(Collectors.toList());
        } else {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            if (!course.getTeacher().getId().equals(teacherId)) {
                throw new RuntimeException("You can only view submissions for your own course");
            }
            submissions = submissionRepository.findPendingSubmissionsForTeacherAndCourse(teacherId, courseId);
        }

        return submissions.stream()
                .map(this::convertToSubmissionResponse)
                .collect(Collectors.toList());
    }

    public List<SubmissionResponse> getSubmissionsForStudent(Long studentId) {
        User student = userService.findById(studentId);
        List<Submission> submissions = submissionRepository.findByStudent(student);
        return submissions.stream()
                .map(this::convertToSubmissionResponse)
                .collect(Collectors.toList());
    }

    public Submission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    public byte[] downloadSubmissionFile(Long submissionId, Long teacherId) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Verify teacher has access to this submission
        if (!submission.getAssignment().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You don't have permission to download this file");
        }

        if (submission.getFilePath() == null) {
            throw new RuntimeException("No file attached to this submission");
        }

        String workingDir = System.getProperty("user.dir");
        Path filePath = Paths.get(workingDir, submission.getFilePath());

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }

        return Files.readAllBytes(filePath);
    }

    private AssignmentResponse convertToResponse(Assignment assignment, Long studentId) {
        boolean submitted = false;
        Submission studentSubmission = null;
        Double score = null;
        String feedback = null;

        if (studentId != null) {
            User student = userService.findById(studentId);
            java.util.Optional<Submission> submission = submissionRepository.findByAssignmentAndStudent(assignment, student);
            if (submission.isPresent()) {
                submitted = true;
                studentSubmission = submission.get();
                score = studentSubmission.getScore();
                feedback = studentSubmission.getFeedback();
            }
        }

        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setSubject(assignment.getSubject());
        response.setDueDate(assignment.getDueDate());
        response.setMaxScore(assignment.getMaxScore());
        response.setTeacher(assignment.getTeacher() != null ? assignment.getTeacher().getFullName() : "Unknown");
        response.setSubmitted(submitted);
        response.setCourseId(assignment.getCourse() != null ? assignment.getCourse().getId() : null);
        response.setCourseName(assignment.getCourse() != null ? assignment.getCourse().getName() : null);
        response.setScore(score);
        response.setFeedback(feedback);
        return response;
    }

    public SubmissionResponse convertToSubmissionResponse(Submission submission) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(submission.getId());
        response.setAssignmentTitle(submission.getAssignment() != null ? submission.getAssignment().getTitle() : "Unknown");
        response.setStudent(submission.getStudent() != null ? submission.getStudent().getFullName() : "Unknown");
        response.setScore(submission.getScore());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGraded(submission.getScore() != null);
        response.setContent(submission.getContent());
        response.setFeedback(submission.getFeedback());
        response.setMaxScore(submission.getAssignment() != null ? submission.getAssignment().getMaxScore() : 100);
        response.setFileName(submission.getFileName());
        response.setFilePath(submission.getFilePath());
        return response;
    }
}