package com.example.taskflow.controller;

import com.example.taskflow.domain.User;
import com.example.taskflow.dto.*;
import com.example.taskflow.security.JwtTokenService;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.UserSettingsService;
import com.example.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final UserSettingsService userSettings;
    private final AuthenticationManager authManager;
    private final JwtTokenService jwt;

    public AuthController(UserService users, UserSettingsService userSettings, AuthenticationManager authManager, JwtTokenService jwt) {
        this.users = users;
        this.userSettings = userSettings;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        users.register(req.getFullName(), req.getEmail(), req.getPassword());
        // On success, 201 Created as per SRS
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req, HttpServletRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User u = principal.getUser();

        String jti = UUID.randomUUID().toString();
        String token = jwt.generateToken(u.getEmail(),
                Map.of(
                        "jti", jti,
                        "userId", u.getId(),
                        "fullName", u.getFullName(),
                        "role", u.getRole() != null ? u.getRole().name() : null,
                        // backward-compat (older Angular builds)
                        "uid", u.getId(),
                        "name", u.getFullName()
                ));
        userSettings.recordLogin(u, jti, request.getHeader("User-Agent"), jwt.getExpiration(token));

        return ResponseEntity.ok(new AuthResponse(token, u.getId(), u.getFullName(), u.getEmail()));
    }
}
