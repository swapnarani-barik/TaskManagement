package com.example.taskflow.controller;

import com.example.taskflow.domain.TaskComment;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.TaskCommentRequest;
import com.example.taskflow.dto.TaskCommentResponse;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TaskCommentService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class TaskCommentController {

    private final TaskCommentService commentService;

    public TaskCommentController(TaskCommentService commentService) {
        this.commentService = commentService;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    @GetMapping
    public ResponseEntity<List<TaskCommentResponse>> getComments(@PathVariable Long taskId) {
        List<TaskComment> comments = commentService.findByTaskId(taskId);

        List<TaskCommentResponse> result = comments.stream()
                .map(c -> new TaskCommentResponse(
                        c.getId(),
                        c.getTask().getId(),
                        c.getAuthor().getId(),
                        c.getAuthor().getFullName(),
                        c.getBody(),
                        c.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<TaskCommentResponse> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentRequest req
    ) {

        User user = currentUser();
        TaskComment saved = commentService.create(taskId, user, req.getBody());

        TaskCommentResponse body = new TaskCommentResponse(
                saved.getId(),
                saved.getTask().getId(),
                saved.getAuthor().getId(),
                saved.getAuthor().getFullName(),
                saved.getBody(),
                saved.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}