package com.studygrind.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String username;
    private List<Long> courseIds;

    private String studentNumber;
    private String phoneNumber;
    private String address;
    private String dateOfBirth;

    // Student card upload fields
    private MultipartFile studentCardFile;
    private String studentCardBase64;

    public RegisterRequest() {}

    // Getters
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getUsername() { return username; }
    public List<Long> getCourseIds() { return courseIds; }
    public String getStudentNumber() { return studentNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getDateOfBirth() { return dateOfBirth; }
    public MultipartFile getStudentCardFile() { return studentCardFile; }
    public String getStudentCardBase64() { return studentCardBase64; }

    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setUsername(String username) { this.username = username; }
    public void setCourseIds(List<Long> courseIds) { this.courseIds = courseIds; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setStudentCardFile(MultipartFile studentCardFile) { this.studentCardFile = studentCardFile; }
    public void setStudentCardBase64(String studentCardBase64) { this.studentCardBase64 = studentCardBase64; }
}