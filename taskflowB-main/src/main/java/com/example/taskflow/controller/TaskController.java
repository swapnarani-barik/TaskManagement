package com.example.taskflow.controller;

import com.example.taskflow.domain.Priority;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.TaskRequest;
import com.example.taskflow.dto.TaskResponse;
import com.example.taskflow.dto.TaskSummaryResponse;
import com.example.taskflow.repository.TeamRepository;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService tasks;
    private final UserRepository userRepo;
    private final TeamRepository teamRepo;

    public TaskController(TaskService tasks, UserRepository userRepo, TeamRepository teamRepo) {
        this.tasks = tasks;
        this.userRepo = userRepo;
        this.teamRepo = teamRepo;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getDueDate(),
                t.getStatus(),
                t.getPriority(),
                t.getOwner().getId(),
                t.getOwner().getFullName(),
                t.getAssignedTo() != null ? t.getAssignedTo().getId() : null,
                t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null,
                t.getTeam() != null ? t.getTeam().getId() : null,
                t.getTeam() != null ? t.getTeam().getName() : null,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(@RequestParam(required = false) Priority priority) {
        User user = currentUser();
        List<TaskResponse> result = tasks.findAll(user, priority)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/summary")
    public ResponseEntity<TaskSummaryResponse> summary() {
        User user = currentUser();
        return ResponseEntity.ok(tasks.getSummary(user));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest req) {
        User user = currentUser();
        Task t = new Task();
        t.setTitle(req.getTitle());
        t.setDescription(req.getDescription());
        t.setDueDate(req.getDueDate());
        t.setStatus(req.getStatus());
        t.setPriority(Priority.forPersistence(req.getPriority()));

        if (req.getAssignedToId() != null) {
            User assignedTo = userRepo.findById(req.getAssignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("user not found"));
            t.setAssignedTo(assignedTo);
        }

        if (req.getTeamId() != null) {
            var team = teamRepo.findById(req.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("team not found"));
            t.setTeam(team);
        }

        Task saved = tasks.create(user, t);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> get(@PathVariable Long id) {
        User user = currentUser();
        Task t = tasks.findByIdOrThrow(user, id);
        return ResponseEntity.ok(toResponse(t));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest req
    ) {
        User user = currentUser();
        Task updated = new Task();

        updated.setTitle(req.getTitle());
        updated.setDescription(req.getDescription());
        updated.setDueDate(req.getDueDate());
        updated.setStatus(req.getStatus());
        updated.setPriority(Priority.forPersistence(req.getPriority()));

        if (req.getAssignedToId() != null) {
            User assignedTo = userRepo.findById(req.getAssignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("user not found"));
            updated.setAssignedTo(assignedTo);
        }

        if (req.getTeamId() != null) {
            var team = teamRepo.findById(req.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("team not found"));
            updated.setTeam(team);
        } else {
            updated.setTeam(null);
        }

        Task saved = tasks.update(user, id, updated);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = currentUser();
        tasks.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
