package com.example.taskflow.dto;

import java.util.List;

public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private long memberCount;
    private long activeTasksCount;
    private List<String> memberInitials;

    public TeamResponse(
            Long id,
            String name,
            String description,
            Long managerId,
            String managerName,
            long memberCount,
            long activeTasksCount,
            List<String> memberInitials
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.managerName = managerName;
        this.memberCount = memberCount;
        this.activeTasksCount = activeTasksCount;
        this.memberInitials = memberInitials;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getManagerId() { return managerId; }
    public String getManagerName() { return managerName; }
    public long getMemberCount() { return memberCount; }
    public long getActiveTasksCount() { return activeTasksCount; }
    public List<String> getMemberInitials() { return memberInitials; }
}
