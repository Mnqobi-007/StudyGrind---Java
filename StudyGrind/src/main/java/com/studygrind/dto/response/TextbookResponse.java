package com.studygrind.dto.response;

import java.time.LocalDateTime;

public class TextbookResponse {
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private Long fileSize;
    private String uploadedBy;
    private String courseName;
    private LocalDateTime uploadDate;
    private String downloadUrl;

    public TextbookResponse() {}

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFileName() { return fileName; }
    public Long getFileSize() { return fileSize; }
    public String getUploadedBy() { return uploadedBy; }
    public String getCourseName() { return courseName; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public String getDownloadUrl() { return downloadUrl; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}