package com.studygrind.service;

import com.studygrind.dto.AddTeacherRequest;
import com.studygrind.model.User;
import com.studygrind.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public TeacherService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<User> getAllTeachers() {
        return userRepository.findByRole("teacher");
    }

    public User addTeacher(AddTeacherRequest request) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        User teacher = new User();
        teacher.setEmail(request.getEmail());
        teacher.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            teacher.setUsername(request.getUsername());
        } else {
            String email = request.getEmail();
            teacher.setUsername(email != null ? email.split("@")[0] : "teacher");
        }

        if (request.getFullName() != null) {
            teacher.setFullName(request.getFullName());
        }

        teacher.setRole("teacher");

        return userRepository.save(teacher);
    }

    public List<User> getAllStudents() {
        return userRepository.findByRole("student");
    }
}
