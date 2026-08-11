package com.example.taskflow.service;

import com.example.taskflow.domain.Priority;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.TaskSummaryResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TaskService {

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    List<Task> findAll(User owner, Priority priority);

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    TaskSummaryResponse getSummary(User owner);

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    Task create(User owner, Task task);

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    Task findByIdOrThrow(User owner, Long id);

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    Task update(User owner, Long id, Task updated);

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    void delete(User owner, Long id);
}
