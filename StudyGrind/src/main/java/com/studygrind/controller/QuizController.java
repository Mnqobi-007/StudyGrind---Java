package com.studygrind.controller;

import com.studygrind.config.SessionHelper;
import com.studygrind.dto.CreateQuestionRequest;
import com.studygrind.dto.CreateQuizRequest;
import com.studygrind.dto.QuizSubmitRequest;
import com.studygrind.model.User;
import com.studygrind.service.AuthService;
import com.studygrind.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private QuizService quizService;
    private SessionHelper sessionHelper;
    private AuthService authService;

    @Autowired
    public QuizController(QuizService quizService, SessionHelper sessionHelper, AuthService authService) {
        this.quizService = quizService;
        this.sessionHelper = sessionHelper;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getQuizzes(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        List<Map<String, Object>> quizzes = quizService.getAllQuizzes(user);
        return ResponseEntity.ok(quizzes);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createQuiz(@RequestBody CreateQuizRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isTeacher() && !user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admins can create quizzes"));
        }
        Map<String, Object> result = quizService.createQuiz(request, user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<Map<String, Object>> addQuestion(@PathVariable Long quizId, @RequestBody CreateQuestionRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isTeacher() && !user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admins can add questions"));
        }
        Map<String, Object> result = quizService.addQuestion(quizId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{quizId}/take")
    public ResponseEntity<Map<String, Object>> takeQuiz(@PathVariable Long quizId, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isStudent()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can take quizzes"));
        }
        Map<String, Object> quizData = quizService.takeQuiz(quizId, user);
        if (quizData == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(quizData);
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<Map<String, Object>> submitQuiz(@PathVariable Long quizId, @RequestBody QuizSubmitRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isStudent()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only students can submit quizzes"));
        }
        Map<String, Object> result = quizService.submitQuiz(quizId, request, user);
        return ResponseEntity.ok(result);
    }
}
