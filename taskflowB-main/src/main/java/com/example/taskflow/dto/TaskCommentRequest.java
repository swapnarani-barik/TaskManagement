package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskCommentRequest {

    @NotBlank
    private String body;

    public String getBody() { 
        return body; 
    }

    public void setBody(String body) { 
        this.body = body; 
    }
}