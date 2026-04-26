package com.studygrind.service;

import com.studygrind.dto.request.CompleteProfileRequest;
import com.studygrind.model.User;
import com.studygrind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileCompletionService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    public boolean isProfileComplete(Long userId) {
        User user = userService.findById(userId);
        return checkProfileCompleteness(user);
    }
    
    private boolean checkProfileCompleteness(User user) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return false;
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return false;
        }
        
        if ("student".equals(user.getRole())) {
            if (user.getStudentNumber() == null || user.getStudentNumber().trim().isEmpty()) {
                return false;
            }
            if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
                return false;
            }
            if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
                return false;
            }
            if (user.getDateOfBirth() == null) {
                return false;
            }
        }
        
        if ("teacher".equals(user.getRole())) {
            if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    @Transactional
    public User completeProfile(Long userId, CompleteProfileRequest request) {
        User user = userService.findById(userId);
        
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getStudentNumber() != null && "student".equals(user.getRole())) {
            user.setStudentNumber(request.getStudentNumber());
        }
        // FIXED: Remove .atStartOfDay() - dateOfBirth should be LocalDate
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth().atStartOfDay());
        }
        
        user.setProfileCompleted(true);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void markProfileCompleted(Long userId) {
        User user = userService.findById(userId);
        user.setProfileCompleted(true);
        userRepository.save(user);
    }
    
    public ProfileCompletionStatus getCompletionStatus(Long userId) {
        User user = userService.findById(userId);
        ProfileCompletionStatus status = new ProfileCompletionStatus();
        status.setCompleted(checkProfileCompleteness(user));
        
        List<String> missingFields = new ArrayList<>();
        
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            missingFields.add("Full Name");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            missingFields.add("Email");
        }
        
        if ("student".equals(user.getRole())) {
            if (user.getStudentNumber() == null || user.getStudentNumber().trim().isEmpty()) {
                missingFields.add("Student Number");
            }
            if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
                missingFields.add("Phone Number");
            }
            if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
                missingFields.add("Address");
            }
            if (user.getDateOfBirth() == null) {
                missingFields.add("Date of Birth");
            }
        }
        
        if ("teacher".equals(user.getRole())) {
            if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
                missingFields.add("Phone Number");
            }
        }
        
        status.setMissingFields(missingFields);
        status.setCompletionPercentage(calculateCompletionPercentage(user));
        
        return status;
    }
    
    private int calculateCompletionPercentage(User user) {
        int totalFields = 2;
        int completedFields = 0;
        
        if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) completedFields++;
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) completedFields++;
        
        if ("student".equals(user.getRole())) {
            totalFields = 6;
            if (user.getStudentNumber() != null && !user.getStudentNumber().trim().isEmpty()) completedFields++;
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) completedFields++;
            if (user.getAddress() != null && !user.getAddress().trim().isEmpty()) completedFields++;
            if (user.getDateOfBirth() != null) completedFields++;
        } else if ("teacher".equals(user.getRole())) {
            totalFields = 3;
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) completedFields++;
        }
        
        return (completedFields * 100) / totalFields;
    }
    
    public static class ProfileCompletionStatus {
        private boolean completed;
        private List<String> missingFields;
        private int completionPercentage;
        
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        
        public List<String> getMissingFields() { return missingFields; }
        public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }
        
        public int getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
    }
}