package com.studygrind.controller;

import com.studygrind.config.SessionHelper;
import com.studygrind.model.User;
import com.studygrind.service.AuthService;
import com.studygrind.service.StatsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatsController {
    private StatsService statsService;
    private SessionHelper sessionHelper;
    private AuthService authService;

    @Autowired
    public StatsController(StatsService statsService, SessionHelper sessionHelper, AuthService authService) {
        this.statsService = statsService;
        this.sessionHelper = sessionHelper;
        this.authService = authService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        Map<String, Object> stats = statsService.getStats(user);
        return ResponseEntity.ok(stats);
    }
}
