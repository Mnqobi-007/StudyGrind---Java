package com.studygrind.dto.request;

public class UpdateUserRequest {
    private String fullName;
    private String phoneNumber;
    private String address;
    private String studentNumber;
    
    public UpdateUserRequest() {}
    
    // Getters
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getStudentNumber() { return studentNumber; }
    
    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
}