package com.example.taskflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 20)
    private ThemePreference theme = ThemePreference.LIGHT;

    @Column(name = "notify_assigned", nullable = false)
    private boolean notifyAssigned = true;

    @Column(name = "notify_comment", nullable = false)
    private boolean notifyComment = true;

    @Column(name = "notify_subtask", nullable = false)
    private boolean notifySubtask = true;

    @Column(name = "notify_overdue", nullable = false)
    private boolean notifyOverdue = true;

    @Column(name = "notify_team", nullable = false)
    private boolean notifyTeam = true;

    @Column(name = "avatar_colour", nullable = false, length = 7)
    private String avatarColour = "#2563EB";

    @Column(name = "bio", length = 200)
    private String bio;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ThemePreference getTheme() { return theme; }
    public void setTheme(ThemePreference theme) { this.theme = theme; }

    public boolean isNotifyAssigned() { return notifyAssigned; }
    public void setNotifyAssigned(boolean notifyAssigned) { this.notifyAssigned = notifyAssigned; }

    public boolean isNotifyComment() { return notifyComment; }
    public void setNotifyComment(boolean notifyComment) { this.notifyComment = notifyComment; }

    public boolean isNotifySubtask() { return notifySubtask; }
    public void setNotifySubtask(boolean notifySubtask) { this.notifySubtask = notifySubtask; }

    public boolean isNotifyOverdue() { return notifyOverdue; }
    public void setNotifyOverdue(boolean notifyOverdue) { this.notifyOverdue = notifyOverdue; }

    public boolean isNotifyTeam() { return notifyTeam; }
    public void setNotifyTeam(boolean notifyTeam) { this.notifyTeam = notifyTeam; }

    public String getAvatarColour() { return avatarColour; }
    public void setAvatarColour(String avatarColour) { this.avatarColour = avatarColour; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
