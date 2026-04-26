package com.studygrind.dto.request;

public class SubmissionRequest {
    private String content;
    private String filePath;

    public SubmissionRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}