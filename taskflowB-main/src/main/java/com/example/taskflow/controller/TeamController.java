package com.example.taskflow.controller;

import com.example.taskflow.domain.Team;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.*;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.TeamMemberRepository;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teams;
    private final TeamMemberRepository teamMembers;
    private final TaskRepository tasks;

    public TeamController(TeamService teams, TeamMemberRepository teamMembers, TaskRepository tasks) {
        this.teams = teams;
        this.teamMembers = teamMembers;
        this.tasks = tasks;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    private TeamResponse toResponse(Team t) {
        long memberCount = teamMembers.countByIdTeamId(t.getId());
        long activeTasksCount = tasks.countActiveByTeamId(t.getId());

        List<String> initials = teamMembers.findByIdTeamId(t.getId()).stream()
                .map(tm -> initialsOf(tm.getUser().getFullName()))
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .limit(4)
                .toList();

        return new TeamResponse(
                t.getId(),
                t.getName(),
                t.getDescription(),
                t.getManager().getId(),
                t.getManager().getFullName(),
                memberCount,
                activeTasksCount,
                initials
        );
    }

    private String initialsOf(String fullName) {
        if (fullName == null) return null;
        String trimmed = fullName.trim();
        if (trimmed.isEmpty()) return null;
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        String first = parts[0];
        String last = parts[parts.length - 1];
        return ("" + first.charAt(0) + last.charAt(0)).toUpperCase();
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamCreateRequest req) {
        Team created = teams.createTeam(currentUser(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> update(@PathVariable Long id, @Valid @RequestBody TeamUpdateRequest req) {
        Team updated = teams.updateTeam(currentUser(), id, req);
        return ResponseEntity.ok(toResponse(updated));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> list() {
        List<TeamResponse> result = teams.listTeams(currentUser())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponse> get(@PathVariable Long id) {
        Team t = teams.getTeam(currentUser(), id);
        List<TeamMemberResponse> members = teamMembers.findByIdTeamId(id)
                .stream()
                .map(tm -> new TeamMemberResponse(
                        tm.getUser().getId(),
                        tm.getUser().getFullName(),
                        tm.getUser().getEmail(),
                        tm.getUser().getRole() != null ? tm.getUser().getRole().name() : null,
                        tasks.countAssignedToUserInTeam(id, tm.getUser().getId()),
                        tm.getJoinedAt()
                ))
                .toList();

        return ResponseEntity.ok(new TeamDetailResponse(
                t.getId(),
                t.getName(),
                t.getDescription(),
                t.getManager().getId(),
                t.getManager().getFullName(),
                members
        ));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(@PathVariable Long id, @Valid @RequestBody TeamAddMemberRequest req) {
        teams.addMember(currentUser(), id, req.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        teams.removeMember(currentUser(), id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teams.deleteTeam(currentUser(), id);
        return ResponseEntity.noContent().build();
    }
}
