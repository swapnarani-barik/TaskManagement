package com.example.taskflow.dto;

import java.util.List;

public class TeamDetailResponse {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private List<TeamMemberResponse> members;

    public TeamDetailResponse(
            Long id,
            String name,
            String description,
            Long managerId,
            String managerName,
            List<TeamMemberResponse> members
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.managerName = managerName;
        this.members = members;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getManagerId() { return managerId; }
    public String getManagerName() { return managerName; }
    public List<TeamMemberResponse> getMembers() { return members; }
}

