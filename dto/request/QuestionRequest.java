package com.studygrind.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class QuestionRequest {
    @NotBlank(message = "Question text is required")
    private String text;
    
    @NotBlank(message = "Option A is required")
    private String optionA;
    
    @NotBlank(message = "Option B is required")
    private String optionB;
    
    private String optionC;
    private String optionD;
    
    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;
    
    @NotNull(message = "Points are required")
    @Positive(message = "Points must be positive")
    private Integer points = 1;
    
    public QuestionRequest() {}
    
    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}