package com.studygrind.service;

import com.studygrind.dto.LoginRequest;
import com.studygrind.dto.RegisterRequest;
import com.studygrind.model.User;
import com.studygrind.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Map<String, Object> login(LoginRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            result.put("success", false);
            result.put("error", "Email and password are required");
            return result;
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            result.put("success", false);
            result.put("error", "Invalid credentials");
            return result;
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            result.put("success", false);
            result.put("error", "Invalid credentials");
            return result;
        }

        result.put("success", true);

        if (user.isAdmin()) {
            result.put("redirect", "/admin/dashboard");
        } else if (user.isTeacher()) {
            result.put("redirect", "/teacher/dashboard");
        } else {
            result.put("redirect", "/student/dashboard");
        }

        return result;
    }

    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            result.put("success", false);
            result.put("error", "Email and password are required");
            return result;
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            result.put("success", false);
            result.put("error", "Email already exists");
            return result;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        } else {
            String email = request.getEmail();
            user.setUsername(email != null ? email.split("@")[0] : "user");
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        user.setRole("student");

        userRepository.save(user);

        result.put("success", true);
        result.put("redirect", "/student/dashboard");
        return result;
    }

    public User getCurrentUser(String email) {
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    public Map<String, Object> getCurrentUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();

        if (user == null) {
            info.put("error", "User not found");
            return info;
        }

        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("role", user.getRole());
        info.put("fullName", user.getFullName());

        return info;
    }
}
