package com.example.taskflow.controller;

import com.example.taskflow.domain.User;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TaskCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final TaskCommentService commentService;

    public CommentController(TaskCommentService commentService) {
        this.commentService = commentService;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        User user = currentUser();
        commentService.delete(commentId, user);
        return ResponseEntity.noContent().build();
    }
}