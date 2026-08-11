package com.example.taskflow.controller;

import com.example.taskflow.domain.Role;
import com.example.taskflow.dto.*;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.TeamMemberRepository;
import com.example.taskflow.repository.TeamRepository;
import com.example.taskflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository users;
    private final TeamMemberRepository teamMembers;
    private final TeamRepository teams;

    public AdminController(UserRepository users, TeamMemberRepository teamMembers, TeamRepository teams) {
        this.users = users;
        this.teamMembers = teamMembers;
        this.teams = teams;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        List<AdminUserResponse> result = users.findAll().stream().map(u -> {
            var teams = teamMembers.findTeamsByUserId(u.getId()).stream()
                    .map(t -> new AdminUserResponse.TeamBrief(t.getId(), t.getName()))
                    .toList();

            return new AdminUserResponse(
                    u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getRole() != null ? u.getRole().name() : null,
                    u.isActive(),
                    u.getCreatedAt(),
                    teams
            );
        }).toList();

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @Valid @RequestBody ChangeUserRoleRequest req) {
        var u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setRole(Role.valueOf(req.getRole().trim().toUpperCase()));
        users.save(u);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestBody ChangeUserStatusRequest req) {
        var u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setActive(req.isActive());
        users.save(u);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        var u = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!teams.findByManagerId(id).isEmpty()) {
            throw new IllegalArgumentException("User manages one or more teams. Reassign or delete those teams first.");
        }
        teamMembers.deleteByIdUserId(id);
        teamMembers.flush();
        users.delete(u);
        return ResponseEntity.noContent().build();
    }
}
