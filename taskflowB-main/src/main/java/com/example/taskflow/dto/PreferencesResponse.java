package com.example.taskflow.dto;

public class PreferencesResponse {
    private String theme;
    private boolean notifyAssigned;
    private boolean notifyComment;
    private boolean notifySubtask;
    private boolean notifyOverdue;
    private boolean notifyTeam;
    private String avatarColour;
    private String bio;

    public PreferencesResponse(
            String theme,
            boolean notifyAssigned,
            boolean notifyComment,
            boolean notifySubtask,
            boolean notifyOverdue,
            boolean notifyTeam,
            String avatarColour,
            String bio
    ) {
        this.theme = theme;
        this.notifyAssigned = notifyAssigned;
        this.notifyComment = notifyComment;
        this.notifySubtask = notifySubtask;
        this.notifyOverdue = notifyOverdue;
        this.notifyTeam = notifyTeam;
        this.avatarColour = avatarColour;
        this.bio = bio;
    }

    public String getTheme() { return theme; }
    public boolean isNotifyAssigned() { return notifyAssigned; }
    public boolean isNotifyComment() { return notifyComment; }
    public boolean isNotifySubtask() { return notifySubtask; }
    public boolean isNotifyOverdue() { return notifyOverdue; }
    public boolean isNotifyTeam() { return notifyTeam; }
    public String getAvatarColour() { return avatarColour; }
    public String getBio() { return bio; }
}
