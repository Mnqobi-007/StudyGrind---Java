package com.studygrind.controller;

import com.studygrind.dto.LoginRequest;
import com.studygrind.dto.RegisterRequest;
import com.studygrind.model.User;
import com.studygrind.service.AuthService;
import com.studygrind.config.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;
    private SessionHelper sessionHelper;

    @Autowired
    public AuthController(AuthService authService, SessionHelper sessionHelper) {
        this.authService = authService;
        this.sessionHelper = sessionHelper;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, HttpSession session) {
        Map<String, Object> result = authService.login(request);
        boolean success = (boolean) result.get("success");
        if (success) {
            User user = authService.getCurrentUser(request.getEmail());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userFullName", user.getFullName());
            session.setAttribute("userName", user.getUsername());
            if (request.isRememberMe()) {
                session.setMaxInactiveInterval(3600);
            }
        }
        return ResponseEntity.status(success ? 200 : 401).body(result);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request, HttpSession session) {
        Map<String, Object> result = authService.register(request);
        if ((boolean) result.get("success")) {
            User user = authService.getCurrentUser(request.getEmail());
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userFullName", user.getFullName());
            session.setAttribute("userName", user.getUsername());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current_user")
    public ResponseEntity<Map<String, Object>> currentUser(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        return ResponseEntity.ok(authService.getCurrentUserInfo(user));
    }
}
