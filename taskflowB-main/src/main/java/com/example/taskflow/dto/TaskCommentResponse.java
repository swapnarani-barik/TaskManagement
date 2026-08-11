package com.example.taskflow.dto;

import java.time.Instant;

public class TaskCommentResponse {

    private Long id;
    private Long taskId;
    private Long authorId;
    private String authorName;
    private String body;
    private Instant createdAt;

    public TaskCommentResponse(
            Long id,
            Long taskId,
            Long authorId,
            String authorName,
            String body,
            Instant createdAt
    ) {
        this.id = id;
        this.taskId = taskId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}