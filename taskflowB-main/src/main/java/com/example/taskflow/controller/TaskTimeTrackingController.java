package com.example.taskflow.controller;

import com.example.taskflow.domain.User;
import com.example.taskflow.dto.ActiveTimerResponse;
import com.example.taskflow.dto.ManualTimeLogRequest;
import com.example.taskflow.dto.TaskTimeLogResponse;
import com.example.taskflow.dto.TimeTotalResponse;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TaskTimeTrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskTimeTrackingController {

    private final TaskTimeTrackingService timeTracking;

    public TaskTimeTrackingController(TaskTimeTrackingService timeTracking) {
        this.timeTracking = timeTracking;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    @PostMapping("/tasks/{taskId}/timer/start")
    public ResponseEntity<ActiveTimerResponse> start(@PathVariable Long taskId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeTracking.startTimer(currentUser(), taskId));
    }

    @PostMapping("/tasks/{taskId}/timer/stop")
    public ResponseEntity<TaskTimeLogResponse> stop(@PathVariable Long taskId) {
        return ResponseEntity.ok(timeTracking.stopTimer(currentUser(), taskId));
    }

    @GetMapping("/tasks/{taskId}/timer/active")
    public ResponseEntity<ActiveTimerResponse> active(@PathVariable Long taskId) {
        return ResponseEntity.ok(timeTracking.activeTimer(currentUser(), taskId));
    }

    @PostMapping("/tasks/{taskId}/time-logs")
    public ResponseEntity<TaskTimeLogResponse> createManual(
            @PathVariable Long taskId,
            @Valid @RequestBody ManualTimeLogRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeTracking.createManualLog(currentUser(), taskId, req));
    }

    @GetMapping("/tasks/{taskId}/time-logs")
    public ResponseEntity<List<TaskTimeLogResponse>> list(@PathVariable Long taskId) {
        return ResponseEntity.ok(timeTracking.listLogs(currentUser(), taskId));
    }

    @GetMapping("/tasks/{taskId}/time-logs/total")
    public ResponseEntity<TimeTotalResponse> total(@PathVariable Long taskId) {
        return ResponseEntity.ok(timeTracking.total(currentUser(), taskId));
    }

    @DeleteMapping("/time-logs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeTracking.deleteLog(currentUser(), id);
        return ResponseEntity.noContent().build();
    }
}

