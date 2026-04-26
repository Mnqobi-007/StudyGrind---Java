package com.studygrind.service;

import com.studygrind.dto.response.StatsResponse;
import com.studygrind.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserService userService;

    public StatsResponse getTeacherStats(Long teacherId) {
        com.studygrind.model.User teacher = userService.findById(teacherId);

        StatsResponse response = new StatsResponse();
        response.setAssignments(assignmentRepository.findByTeacher(teacher).size());
        response.setQuizzes(quizRepository.findByTeacher(teacher).size());
        response.setNotes(noteRepository.findByAuthor(teacher).size());
        response.setSubmissions(submissionRepository.findPendingSubmissionsForTeacher(teacherId).size());
        response.setStudents(userService.getAllStudents().size());

        return response;
    }

    public StatsResponse getStudentStats(Long studentId) {
        int pendingAssignments = assignmentRepository.findPendingAssignmentsForStudent(studentId).size();
        int completedQuizzes = (int) quizAttemptRepository.countCompletedQuizzesForStudent(studentId);
        Double avgScore = quizAttemptRepository.getAverageScoreForStudent(studentId);

        StatsResponse response = new StatsResponse();
        response.setPendingAssignments(pendingAssignments);
        response.setCompletedQuizzes(completedQuizzes);
        response.setAvgScore(avgScore != null ? avgScore : 0.0);

        return response;
    }
}