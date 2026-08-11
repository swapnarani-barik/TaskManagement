package com.example.taskflow.dto;

public class MeProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String avatarColour;
    private String bio;

    public MeProfileResponse(Long id, String fullName, String email, String role, String avatarColour, String bio) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.avatarColour = avatarColour;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAvatarColour() { return avatarColour; }
    public String getBio() { return bio; }
}
