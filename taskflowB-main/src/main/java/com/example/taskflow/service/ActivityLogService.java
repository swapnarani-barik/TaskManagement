package com.example.taskflow.service;

import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;

public interface ActivityLogService {
    void log(Task task, User actor, String actionCode, String message);
}