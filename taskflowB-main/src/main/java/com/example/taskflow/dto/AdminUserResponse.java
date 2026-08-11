package com.example.taskflow.dto;

import java.time.Instant;
import java.util.List;

public class AdminUserResponse {

    public static class TeamBrief {
        private Long id;
        private String name;

        public TeamBrief(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
    }

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private boolean isActive;
    private Instant joinedAt;
    private List<TeamBrief> teams;

    public AdminUserResponse(
            Long id,
            String fullName,
            String email,
            String role,
            boolean isActive,
            Instant joinedAt,
            List<TeamBrief> teams
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.joinedAt = joinedAt;
        this.teams = teams;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return isActive; }
    public Instant getJoinedAt() { return joinedAt; }
    public List<TeamBrief> getTeams() { return teams; }
}

