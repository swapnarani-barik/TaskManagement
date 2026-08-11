package com.example.taskflow.dto;

import java.time.Instant;

public class SubtaskResponse {

    private Long id;
    private Long taskId;
    private String title;
    private boolean complete;
    private Long assignedToId;
    private String assignedToName;
    private Long createdById;
    private String createdByName;
    private Instant createdAt;
    private Instant completedAt;

    public SubtaskResponse(
            Long id,
            Long taskId,
            String title,
            boolean complete,
            Long assignedToId,
            String assignedToName,
            Long createdById,
            String createdByName,
            Instant createdAt,
            Instant completedAt
    ) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.complete = complete;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isComplete() {
        return complete;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}

