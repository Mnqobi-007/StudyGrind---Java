package com.studygrind.dto.response;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private LocalDateTime createdAt;
    private String studentNumber;
    private String phoneNumber;
    private String address;
    private Boolean emailVerified;
    private String verificationStatus;

    public UserResponse() {}

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStudentNumber() { return studentNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public Boolean getEmailVerified() { return emailVerified; }
    public String getVerificationStatus() { return verificationStatus; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
}