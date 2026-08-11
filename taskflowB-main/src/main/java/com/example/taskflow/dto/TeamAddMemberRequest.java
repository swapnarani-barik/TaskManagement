package com.example.taskflow.dto;

import jakarta.validation.constraints.NotNull;

public class TeamAddMemberRequest {
    @NotNull
    private Long userId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

