package com.studygrind.dto.request;

import java.util.HashMap;
import java.util.Map;

public class QuizSubmitRequest {
    private Map<String, String> answers = new HashMap<>();
    
    public QuizSubmitRequest() {}
    
    public Map<String, String> getAnswers() {
        return answers;
    }
    
    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }
}