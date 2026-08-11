package com.example.taskflow.dto;

import java.time.Instant;
import java.time.LocalDate;

public class TaskTimeLogResponse {
    private Long id;
    private Long taskId;
    private Long loggedById;
    private String loggedByName;
    private int durationMinutes;
    private LocalDate logDate;
    private String note;
    private boolean manual;
    private Instant createdAt;

    public TaskTimeLogResponse(
            Long id,
            Long taskId,
            Long loggedById,
            String loggedByName,
            int durationMinutes,
            LocalDate logDate,
            String note,
            boolean manual,
            Instant createdAt
    ) {
        this.id = id;
        this.taskId = taskId;
        this.loggedById = loggedById;
        this.loggedByName = loggedByName;
        this.durationMinutes = durationMinutes;
        this.logDate = logDate;
        this.note = note;
        this.manual = manual;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getLoggedById() {
        return loggedById;
    }

    public String getLoggedByName() {
        return loggedByName;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public String getNote() {
        return note;
    }

    public boolean isManual() {
        return manual;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

