package com.example.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    @NotBlank
    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @Size(max = 120)
    private String currentPassword;

    @Size(max = 7)
    private String avatarColour;

    @Size(max = 200)
    private String bio;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getAvatarColour() { return avatarColour; }
    public void setAvatarColour(String avatarColour) { this.avatarColour = avatarColour; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
