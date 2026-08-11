package com.example.taskflow.controller;

import com.example.taskflow.domain.User;
import com.example.taskflow.dto.ChangePasswordRequest;
import com.example.taskflow.dto.DeleteAccountRequest;
import com.example.taskflow.dto.MeProfileResponse;
import com.example.taskflow.dto.PreferencesResponse;
import com.example.taskflow.dto.UpdatePreferencesRequest;
import com.example.taskflow.dto.UpdateProfileRequest;
import com.example.taskflow.dto.UserSessionResponse;
import com.example.taskflow.security.JwtTokenService;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.UserSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class UserSettingsController {

    private final UserSettingsService settings;
    private final JwtTokenService jwtTokenService;

    public UserSettingsController(UserSettingsService settings, JwtTokenService jwtTokenService) {
        this.settings = settings;
        this.jwtTokenService = jwtTokenService;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }

    private String currentJti(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return jwtTokenService.getJti(authHeader.substring(7));
    }

    @GetMapping
    public ResponseEntity<MeProfileResponse> me() {
        return ResponseEntity.ok(settings.getMe(currentUser()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<MeProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(settings.updateProfile(currentUser(), req));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        settings.changePassword(currentUser(), req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<PreferencesResponse> getPreferences() {
        return ResponseEntity.ok(settings.getPreferences(currentUser()));
    }

    @PatchMapping("/preferences")
    public ResponseEntity<PreferencesResponse> updatePreferences(@RequestBody UpdatePreferencesRequest req) {
        return ResponseEntity.ok(settings.updatePreferences(currentUser(), req));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteOwnAccount(
            @Valid @RequestBody DeleteAccountRequest req,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        settings.deleteOwnAccount(currentUser(), req, currentJti(authHeader));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<UserSessionResponse>> listSessions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        return ResponseEntity.ok(settings.listSessions(currentUser(), currentJti(authHeader)));
    }

    @DeleteMapping("/sessions/{jti}")
    public ResponseEntity<Void> revokeSession(@PathVariable String jti) {
        settings.revokeSession(currentUser(), jti);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> revokeAllOtherSessions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        settings.revokeAllOtherSessions(currentUser(), currentJti(authHeader));
        return ResponseEntity.noContent().build();
    }
}
