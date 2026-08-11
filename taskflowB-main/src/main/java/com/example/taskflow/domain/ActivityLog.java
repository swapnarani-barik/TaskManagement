package com.example.taskflow.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "FK_activity_log_tasks"))
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_activity_log_users"))
    private User actor;

    // New schema column (E-EXT-03)
    @Column(name = "action_code", length = 50)
    private String actionCode;

    // Legacy schema column (backward compatibility)
    @Column(name = "action", length = 50)
    private String legacyAction;

    // New schema column (E-EXT-05)
    @Column(name = "message", length = 500)
    private String message;

    // Legacy schema column (backward compatibility)
    @Lob
    @Column(name = "details")
    private String legacyDetails;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ActivityLog() {}

    public ActivityLog(Task task, User actor, String actionCode, String message) {
        this.task = task;
        this.actor = actor;
        setActionCode(actionCode);
        setMessage(message);
    }

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }
    public void setTask(Task task) {
        this.task = task;
    }

    public User getActor() {
        return actor;
    }
    public void setActor(User actor) {
        this.actor = actor;
    }

    public String getActionCode() {
        return actionCode != null ? actionCode : legacyAction;
    }
    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
        this.legacyAction = actionCode;
    }

    public String getMessage() {
        return message != null ? message : legacyDetails;
    }
    public void setMessage(String message) {
        this.message = message;
        this.legacyDetails = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    @PreUpdate
    void syncCompatibilityColumns() {
        if (actionCode == null && legacyAction != null)
            actionCode = legacyAction;

        if (legacyAction == null && actionCode != null)
            legacyAction = actionCode;

        if (message == null && legacyDetails != null)
            message = legacyDetails;

        if (legacyDetails == null && message != null)
            legacyDetails = message;
        }
}