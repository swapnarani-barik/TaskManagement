package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class TeamCreateRequest {

    @NotBlank
    private String name;

    private String description;

    // Admin-only: can create team and assign any user as manager.
    private Long managerId;

    // Optional list of member user IDs to add on create (manager is always added).
    private List<Long> memberIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
}

