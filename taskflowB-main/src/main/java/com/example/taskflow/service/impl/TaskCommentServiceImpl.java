package com.example.taskflow.service.impl;

import com.example.taskflow.domain.Task;
import org.springframework.transaction.annotation.Transactional;
import com.example.taskflow.domain.TaskComment;
import com.example.taskflow.domain.User;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.repository.TaskCommentRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.service.ActivityLogService;
import com.example.taskflow.service.TaskCommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskCommentServiceImpl implements TaskCommentService {
 
    private final TaskCommentRepository commentRepo;
    private final TaskRepository taskRepo;
    private final ActivityLogService activityLogs;
 
    public TaskCommentServiceImpl(TaskCommentRepository commentRepo,
                                  TaskRepository taskRepo,
                                  ActivityLogService activityLogs) {
        this.commentRepo = commentRepo;
        this.taskRepo = taskRepo;
        this.activityLogs = activityLogs;
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<TaskComment> findByTaskId(Long taskId) {
        // ✅ Use fetch-join to avoid LazyInitializationException in controller mapping
        return commentRepo.findByTaskIdWithAuthor(taskId);
    }
 
    @Override
    @Transactional
    public TaskComment create(Long taskId, User author, String body) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
 
        TaskComment comment = new TaskComment(task, author, body);
        TaskComment saved = commentRepo.save(comment);
 
        // Activity: comment added
        activityLogs.log(
                task,
                author,
                "COMMENT_ADDED",
                author.getFullName() + " commented on \"" + task.getTitle() + "\""
        );
 
        return saved;
    }
 
    @Override
    @Transactional
    public void delete(Long commentId, User requester) {
        TaskComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
 
        // Only author can delete their comment
        if (!comment.getAuthor().getId().equals(requester.getId())) {
            throw new ForbiddenException("You can only delete your own comments");
        }
 
        commentRepo.delete(comment);
    }
}