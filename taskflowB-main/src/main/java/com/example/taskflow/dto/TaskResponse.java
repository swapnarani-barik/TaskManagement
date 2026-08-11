package com.example.taskflow.dto;

import com.example.taskflow.domain.Priority;
import com.example.taskflow.domain.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;

public class TaskResponse {

    public static class Assignee {
        private final Long id;
        private final String fullName;

        public Assignee(Long id, String fullName) {
            this.id = id;
            this.fullName = fullName;
        }

        public Long getId() { return id; }
        public String getFullName() { return fullName; }
    }

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private Priority priority;
    private Long ownerId;
    private String ownerName;
    private Long assignedToId;
    private String assignedToName;
    private Assignee assignee;
    private Long teamId;
    private String teamName;
    private Instant createdAt;
    private Instant updatedAt;

    // Builder-style constructor
    public TaskResponse(
            Long id,
            String title,
            String description,
            LocalDate dueDate,
            TaskStatus status,
            Priority priority,
            Long ownerId,
            String ownerName,
            Long assignedToId,
            String assignedToName,
            Long teamId,
            String teamName,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.priority = priority;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.assignee = assignedToId != null
                ? new Assignee(assignedToId, assignedToName)
                : null;
        this.teamId = teamId;
        this.teamName = teamName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters only
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public Long getAssignedToId() { return assignedToId; }
    public String getAssignedToName() { return assignedToName; }
    public Assignee getAssignee() { return assignee; }
    public Long getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
