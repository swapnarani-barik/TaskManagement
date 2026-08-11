package com.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangeUserRoleRequest {
    @NotBlank
    private String role;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

