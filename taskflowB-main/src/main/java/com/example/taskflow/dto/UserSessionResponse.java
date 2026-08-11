package com.example.taskflow.dto;

import java.time.Instant;

public class UserSessionResponse {
    private String jti;
    private String deviceHint;
    private Instant loginTime;
    private Instant lastActive;
    private boolean active;

    public UserSessionResponse(String jti, String deviceHint, Instant loginTime, Instant lastActive, boolean active) {
        this.jti = jti;
        this.deviceHint = deviceHint;
        this.loginTime = loginTime;
        this.lastActive = lastActive;
        this.active = active;
    }

    public String getJti() { return jti; }
    public String getDeviceHint() { return deviceHint; }
    public Instant getLoginTime() { return loginTime; }
    public Instant getLastActive() { return lastActive; }
    public boolean isActive() { return active; }
}
