package com.example.taskflow.service;

import com.example.taskflow.domain.TaskComment;
import com.example.taskflow.domain.User;

import java.util.List;

public interface TaskCommentService {

    List<TaskComment> findByTaskId(Long taskId);

    TaskComment create(Long taskId, User author, String body);

    void delete(Long commentId, User requester);
}