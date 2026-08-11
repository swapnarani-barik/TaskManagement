package com.example.taskflow.dto;

import java.time.Instant;

public class TeamMemberResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private long tasksAssigned;
    private Instant joinedAt;

    public TeamMemberResponse(
            Long userId,
            String fullName,
            String email,
            String role,
            long tasksAssigned,
            Instant joinedAt
    ) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.tasksAssigned = tasksAssigned;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public long getTasksAssigned() { return tasksAssigned; }
    public Instant getJoinedAt() { return joinedAt; }
}
