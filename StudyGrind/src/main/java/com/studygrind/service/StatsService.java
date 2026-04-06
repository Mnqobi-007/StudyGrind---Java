package com.studygrind.service;

import com.studygrind.model.User;
import com.studygrind.repository.AssignmentRepository;
import com.studygrind.repository.NoteRepository;
import com.studygrind.repository.QuizAttemptRepository;
import com.studygrind.repository.QuizRepository;
import com.studygrind.repository.SubmissionRepository;
import com.studygrind.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private AssignmentRepository assignmentRepository;
    private QuizRepository quizRepository;
    private NoteRepository noteRepository;
    private SubmissionRepository submissionRepository;
    private QuizAttemptRepository quizAttemptRepository;
    private UserRepository userRepository;

    public StatsService(AssignmentRepository assignmentRepository,
                        QuizRepository quizRepository,
                        NoteRepository noteRepository,
                        SubmissionRepository submissionRepository,
                        QuizAttemptRepository quizAttemptRepository,
                        UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.quizRepository = quizRepository;
        this.noteRepository = noteRepository;
        this.submissionRepository = submissionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getStats(User user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Map<String, Object> stats = new HashMap<>();

        if (user.isAdmin()) {
            stats.put("assignmentsCount", assignmentRepository.findAll().size());
            stats.put("quizzesCount", quizRepository.findAll().size());
            stats.put("notesCount", noteRepository.findAll().size());
            stats.put("submissionsCount", submissionRepository.findAll().size());
            stats.put("studentsCount", userRepository.findByRole("student").size());
            return stats;
        }

        if (user.isTeacher()) {
            stats.put("assignmentsCount", assignmentRepository.findByTeacherId(user.getId()).size());
            stats.put("quizzesCount", quizRepository.findByTeacherId(user.getId()).size());
            stats.put("notesCount", noteRepository.findByTeacherId(user.getId()).size());
            stats.put("submissionsCount", submissionRepository.findByTeacherId(user.getId()).size());
            stats.put("studentsCount", userRepository.findByRole("student").size());
            return stats;
        }

        // Student stats
        long pendingAssignments = 0;
        List<com.studygrind.model.Assignment> allAssignments = assignmentRepository.findAll();
        for (com.studygrind.model.Assignment assignment : allAssignments) {
            boolean submitted = submissionRepository.existsByAssignmentIdAndStudentId(
                    assignment.getId(), user.getId());
            if (!submitted) {
                pendingAssignments++;
            }
        }
        stats.put("pendingAssignments", pendingAssignments);

        long completedQuizzes = quizAttemptRepository.countByStudentId(user.getId());
        stats.put("completedQuizzes", completedQuizzes);

        double avgScore = calculateAverageScore(user.getId());
        stats.put("avgScore", avgScore);

        return stats;
    }

    private double calculateAverageScore(Long studentId) {
        List<com.studygrind.model.QuizAttempt> attempts = quizAttemptRepository.findByStudentId(studentId);

        if (attempts == null || attempts.isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        int count = 0;

        for (com.studygrind.model.QuizAttempt attempt : attempts) {
            if (attempt.getScore() != null) {
                totalScore += attempt.getScore();
                count++;
            }
        }

        if (count == 0) {
            return 0.0;
        }

        return totalScore / count;
    }
}
