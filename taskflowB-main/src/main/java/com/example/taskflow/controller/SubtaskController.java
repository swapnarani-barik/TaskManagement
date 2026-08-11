package com.example.taskflow.controller;

import com.example.taskflow.domain.User;
import com.example.taskflow.dto.SubtaskCreateRequest;
import com.example.taskflow.dto.SubtaskResponse;
import com.example.taskflow.dto.SubtaskSummaryResponse;
import com.example.taskflow.dto.SubtaskUpdateRequest;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.SubtaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubtaskController {

    private final SubtaskService subtasks;

    public SubtaskController(SubtaskService subtasks) {
        this.subtasks = subtasks;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    @GetMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<List<SubtaskResponse>> list(@PathVariable Long taskId) {
        return ResponseEntity.ok(subtasks.list(currentUser(), taskId));
    }

    @PostMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<SubtaskResponse> create(
            @PathVariable Long taskId,
            @Valid @RequestBody SubtaskCreateRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subtasks.create(currentUser(), taskId, req));
    }

    @PatchMapping("/subtasks/{id}/toggle")
    public ResponseEntity<SubtaskResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(subtasks.toggle(currentUser(), id));
    }

    @PutMapping("/subtasks/{id}")
    public ResponseEntity<SubtaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SubtaskUpdateRequest req
    ) {
        return ResponseEntity.ok(subtasks.update(currentUser(), id, req));
    }

    @DeleteMapping("/subtasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subtasks.delete(currentUser(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/{taskId}/subtasks/summary")
    public ResponseEntity<SubtaskSummaryResponse> summary(@PathVariable Long taskId) {
        return ResponseEntity.ok(subtasks.summary(currentUser(), taskId));
    }
}

