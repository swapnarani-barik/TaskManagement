package com.example.taskflow.service.impl;

import com.example.taskflow.domain.ActivityLog;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import com.example.taskflow.repository.ActivityLogRepository;
import com.example.taskflow.service.ActivityLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {
    private static final Logger log = LoggerFactory.getLogger(ActivityLogServiceImpl.class);
    private final ActivityLogRepository activityLogs;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogs) {
        this.activityLogs = activityLogs;
    }

    @Override
    public void log(Task task, User actor, String actionCode, String message) {
        try {
            activityLogs.save(new ActivityLog(task, actor, actionCode, message));
        } catch (Exception ex) {
            // Activity log write must not break core operations (task/comment CRUD).
            log.warn("Activity log write skipped. actionCode={}, actorId={}, taskId={}, reason={}",
                    actionCode,
                    actor == null ? null : actor.getId(),
                    task == null ? null : task.getId(),
                    ex.getMessage());
        }
    }
}
