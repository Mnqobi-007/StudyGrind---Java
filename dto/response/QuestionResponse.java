package com.studygrind.dto.response;

import java.util.ArrayList;
import java.util.List;

public class QuestionResponse {
    private Long id;
    private String text;
    private List<String> options = new ArrayList<>();
    
    public QuestionResponse() {}
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<>();
    }
    
    // Convenience method to add option
    public void addOption(String option) {
        if (option != null && !option.isEmpty()) {
            this.options.add(option);
        }
    }
}