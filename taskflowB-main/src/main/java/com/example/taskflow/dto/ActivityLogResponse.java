package com.example.taskflow.dto;

import java.time.Instant;

public class ActivityLogResponse {

    private final Long id;
    private final Long taskId;
    private final Long actorId;
    private final String actorName;
    private final String actionCode;
    private final String message;
    private final Instant createdAt;

    public ActivityLogResponse(
            Long id,
            Long taskId,
            Long actorId,
            String actorName,
            String actionCode,
            String message,
            Instant createdAt
    ) {
        this.id = id;
        this.taskId = taskId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.actionCode = actionCode;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public Long getActorId() { return actorId; }
    public String getActorName() { return actorName; }
    public String getActionCode() { return actionCode; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}