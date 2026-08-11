package com.example.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ChangeUserStatusRequest {
    @JsonAlias({"active", "isActive"})
    private boolean isActive;

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
