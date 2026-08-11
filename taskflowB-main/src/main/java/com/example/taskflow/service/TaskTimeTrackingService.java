package com.example.taskflow.service;

import com.example.taskflow.domain.ActiveTimer;
import com.example.taskflow.domain.Role;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.TaskTimeLog;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.ActiveTimerResponse;
import com.example.taskflow.dto.ManualTimeLogRequest;
import com.example.taskflow.dto.TaskTimeLogResponse;
import com.example.taskflow.dto.TimeTotalResponse;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.ActiveTimerRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.TaskTimeLogRepository;
import com.example.taskflow.repository.TeamMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TaskTimeTrackingService {

    private final TaskRepository tasks;
    private final TaskTimeLogRepository logs;
    private final ActiveTimerRepository timers;
    private final TeamMemberRepository teamMembers;

    public TaskTimeTrackingService(
            TaskRepository tasks,
            TaskTimeLogRepository logs,
            ActiveTimerRepository timers,
            TeamMemberRepository teamMembers
    ) {
        this.tasks = tasks;
        this.logs = logs;
        this.timers = timers;
        this.teamMembers = teamMembers;
    }

    public ActiveTimerResponse startTimer(User actor, Long taskId) {
        ensureCanTrack(actor);
        Task task = findTaskVisibleToActor(taskId, actor);

        if (timers.existsByTaskIdAndUserId(taskId, actor.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A timer is already running");
        }

        ActiveTimer timer = new ActiveTimer();
        timer.setTask(task);
        timer.setUser(actor);
        timer.setStartTime(Instant.now());
        ActiveTimer saved = timers.save(timer);
        return new ActiveTimerResponse(true, saved.getStartTime());
    }

    public TaskTimeLogResponse stopTimer(User actor, Long taskId) {
        ensureCanTrack(actor);
        findTaskVisibleToActor(taskId, actor);

        ActiveTimer timer = timers.findByTaskIdAndUserId(taskId, actor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No running timer found"));

        long minutes = Duration.between(timer.getStartTime(), Instant.now()).toMinutes();
        int durationMinutes = (int) Math.max(1, minutes);

        TaskTimeLog log = new TaskTimeLog();
        log.setTask(timer.getTask());
        log.setLoggedBy(actor);
        log.setDurationMinutes(durationMinutes);
        log.setLogDate(LocalDate.now());
        log.setManual(false);
        log.setNote(null);

        TaskTimeLog saved = logs.save(log);
        timers.delete(timer);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ActiveTimerResponse activeTimer(User actor, Long taskId) {
        findTaskVisibleToActor(taskId, actor);
        return timers.findByTaskIdAndUserId(taskId, actor.getId())
                .map(t -> new ActiveTimerResponse(true, t.getStartTime()))
                .orElseGet(() -> new ActiveTimerResponse(false, null));
    }

    public TaskTimeLogResponse createManualLog(User actor, Long taskId, ManualTimeLogRequest req) {
        ensureCanTrack(actor);
        Task task = findTaskVisibleToActor(taskId, actor);

        TaskTimeLog log = new TaskTimeLog();
        log.setTask(task);
        log.setLoggedBy(actor);
        log.setDurationMinutes(req.getDurationMinutes());
        log.setLogDate(req.getLogDate());
        log.setNote(req.getNote());
        log.setManual(true);

        return toResponse(logs.save(log));
    }

    @Transactional(readOnly = true)
    public List<TaskTimeLogResponse> listLogs(User actor, Long taskId) {
        findTaskVisibleToActor(taskId, actor);
        return logs.findByTaskIdOrderByLogDateDescCreatedAtDesc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TimeTotalResponse total(User actor, Long taskId) {
        findTaskVisibleToActor(taskId, actor);
        Integer totalMinutes = logs.totalMinutesByTaskId(taskId);
        return new TimeTotalResponse(totalMinutes != null ? totalMinutes : 0);
    }

    public void deleteLog(User actor, Long logId) {
        TaskTimeLog log = logs.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Time log not found"));
        ensureTaskVisible(actor, log.getTask());

        if (!log.isManual()) {
            throw new ForbiddenException("Timer-generated entries cannot be deleted");
        }

        boolean allowed = actor.getRole() == Role.ADMIN
                || actor.getRole() == Role.MANAGER
                || log.getLoggedBy().getId().equals(actor.getId());
        if (!allowed) {
            throw new ForbiddenException("Not authorized to delete this time log");
        }
        logs.delete(log);
    }

    private TaskTimeLogResponse toResponse(TaskTimeLog log) {
        return new TaskTimeLogResponse(
                log.getId(),
                log.getTask().getId(),
                log.getLoggedBy().getId(),
                log.getLoggedBy().getFullName(),
                log.getDurationMinutes(),
                log.getLogDate(),
                log.getNote(),
                log.isManual(),
                log.getCreatedAt()
        );
    }

    private void ensureCanTrack(User actor) {
        if (actor.getRole() == Role.VIEWER) {
            throw new ForbiddenException("Viewers cannot log time");
        }
    }

    private Task findTaskVisibleToActor(Long taskId, User actor) {
        Task task = tasks.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        ensureTaskVisible(actor, task);
        return task;
    }

    private void ensureTaskVisible(User actor, Task task) {
        if (canView(actor, task)) return;
        throw new ForbiddenException("You do not have access to this task");
    }

    private boolean canView(User actor, Task task) {
        if (actor.getRole() == Role.ADMIN) return true;
        if (task.getOwner() != null && task.getOwner().getId().equals(actor.getId())) return true;
        if (task.getAssignedTo() != null && task.getAssignedTo().getId().equals(actor.getId())) return true;

        if (task.getTeam() != null) {
            if (actor.getRole() == Role.MANAGER
                    && task.getTeam().getManager() != null
                    && task.getTeam().getManager().getId().equals(actor.getId())) {
                return true;
            }
            return teamMembers.existsByIdTeamIdAndIdUserId(task.getTeam().getId(), actor.getId());
        }
        return false;
    }
}

