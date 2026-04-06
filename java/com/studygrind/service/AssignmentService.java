package com.studygrind.service;

import com.studygrind.dto.CreateAssignmentRequest;
import com.studygrind.dto.GradeSubmissionRequest;
import com.studygrind.dto.SubmitAssignmentRequest;
import com.studygrind.model.Assignment;
import com.studygrind.model.Submission;
import com.studygrind.model.User;
import com.studygrind.repository.AssignmentRepository;
import com.studygrind.repository.SubmissionRepository;
import com.studygrind.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssignmentService {

    private AssignmentRepository assignmentRepository;
    private SubmissionRepository submissionRepository;
    private UserRepository userRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             SubmissionRepository submissionRepository,
                             UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    public List<Assignment> getAllAssignments(User user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isAdmin()) {
            return assignmentRepository.findAll();
        }

        if (user.isTeacher()) {
            return assignmentRepository.findByTeacherId(user.getId());
        }

        return assignmentRepository.findAll();
    }

    public List<Map<String, Object>> getAllAssignmentsWithStatus(User user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        List<Assignment> assignments;

        if (user.isAdmin()) {
            assignments = assignmentRepository.findAll();
        } else if (user.isTeacher()) {
            assignments = assignmentRepository.findByTeacherId(user.getId());
        } else {
            assignments = assignmentRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        if (user.isTeacher() || user.isAdmin()) {
            for (Assignment assignment : assignments) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", assignment.getId());
                map.put("title", assignment.getTitle());
                map.put("description", assignment.getDescription());
                map.put("dueDate", assignment.getDueDate());
                map.put("teacherId", assignment.getTeacherId());
                result.add(map);
            }
        } else {
            for (Assignment assignment : assignments) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", assignment.getId());
                map.put("title", assignment.getTitle());
                map.put("description", assignment.getDescription());
                map.put("dueDate", assignment.getDueDate());
                map.put("teacherId", assignment.getTeacherId());
                boolean submitted = submissionRepository.existsByAssignmentIdAndStudentId(
                        assignment.getId(), user.getId());
                map.put("submitted", submitted);
                result.add(map);
            }
        }

        return result;
    }

    public Assignment createAssignment(CreateAssignmentRequest request, User user) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.isTeacher() && !user.isAdmin()) {
            throw new RuntimeException("Only teachers can create assignments");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Assignment title is required");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setTeacherId(user.getId());

        if (request.getDueDate() != null && !request.getDueDate().isBlank()) {
            assignment.setDueDate(LocalDateTime.parse(request.getDueDate()));
        }

        return assignmentRepository.save(assignment);
    }

    public Submission submitAssignment(Long assignmentId, SubmitAssignmentRequest request, User user) {
        if (assignmentId == null) {
            throw new RuntimeException("Assignment ID is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isTeacher() || user.isAdmin()) {
            throw new RuntimeException("Only students can submit assignments");
        }

        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (!assignmentOpt.isPresent()) {
            throw new RuntimeException("Assignment not found with id: " + assignmentId);
        }

        boolean alreadySubmitted = submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, user.getId());
        if (alreadySubmitted) {
            throw new RuntimeException("You have already submitted this assignment");
        }

        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setAssignment(assignmentOpt.get());
        submission.setStudentId(user.getId());
        submission.setStudent(user);
        submission.setContent(request.getContent());
        if (request.getFilePath() != null) {
            submission.setFilePath(request.getFilePath());
        }
        submission.setSubmittedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    public List<Submission> getSubmissions(User user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isAdmin()) {
            return submissionRepository.findAll();
        }

        if (user.isTeacher()) {
            return submissionRepository.findByTeacherId(user.getId());
        }

        return submissionRepository.findByStudentId(user.getId());
    }

    public Submission gradeSubmission(Long submissionId, GradeSubmissionRequest request) {
        if (submissionId == null) {
            throw new RuntimeException("Submission ID is required");
        }

        if (request == null) {
            throw new RuntimeException("Request body is required");
        }

        Optional<Submission> submissionOpt = submissionRepository.findById(submissionId);
        if (!submissionOpt.isPresent()) {
            throw new RuntimeException("Submission not found with id: " + submissionId);
        }

        Submission submission = submissionOpt.get();
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setGradedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    public List<Assignment> getPendingAssignments(User user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.isAdmin() && !user.isTeacher()) {
            return assignmentRepository.findPendingByStudentId(user.getId());
        }

        return assignmentRepository.findAll();
    }
}
