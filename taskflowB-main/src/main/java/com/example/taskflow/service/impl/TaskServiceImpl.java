package com.example.taskflow.service.impl;

import com.example.taskflow.domain.Priority;
import com.example.taskflow.domain.Role;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.TaskSummaryResponse;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.ActivityLogRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.TeamMemberRepository;
import com.example.taskflow.service.ActivityLogService;
import com.example.taskflow.service.TaskService;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
 
    private final TaskRepository tasks;
    private final ActivityLogService activityLogs;
    private final ActivityLogRepository activityLogRepo; // <-- NEW
    private final TeamMemberRepository teamMembers;
 
    public TaskServiceImpl(TaskRepository tasks,
                           ActivityLogService activityLogs,
                           ActivityLogRepository activityLogRepo,
                           TeamMemberRepository teamMembers) {
        this.tasks = tasks;
        this.activityLogs = activityLogs;
        this.activityLogRepo = activityLogRepo;
        this.teamMembers = teamMembers;
    }
 
    @Override
    public List<Task> findAll(User owner, Priority priority) {
        List<Task> visible = tasks.findVisibleTasks(owner.getId());
        if (priority == null) return visible;

        if (priority == Priority.MEDIUM || priority == Priority.MED) {
            return visible.stream()
                    .filter(t -> t.getPriority() == Priority.MED || t.getPriority() == Priority.MEDIUM)
                    .toList();
        }
        return visible.stream()
                .filter(t -> t.getPriority() == priority)
                .toList();
    }
 
    @Override
    public TaskSummaryResponse getSummary(User owner) {
        int total = (int) tasks.countByOwner(owner);
 
        TaskSummaryResponse.ByStatus byStatus = new TaskSummaryResponse.ByStatus(
                (int) tasks.countTodoByOwner(owner),
                (int) tasks.countInProgressByOwner(owner),
                (int) tasks.countDoneByOwner(owner)
        );
 
        TaskSummaryResponse.ByPriority byPriority = new TaskSummaryResponse.ByPriority(
                (int) tasks.countHighByOwner(owner),
                (int) tasks.countMediumByOwner(owner.getId()),
                (int) tasks.countLowByOwner(owner)
        );
 
        return new TaskSummaryResponse(
                total,
                byStatus,
                byPriority,
                tasks.completionRateByOwnerId(owner.getId()),
                (int) tasks.overdueCountByOwner(owner),
                (int) tasks.countTasksThisWeekByOwnerId(owner.getId()),
                (int) tasks.dueTodayCountByOwner(owner)
        );
    }
 
    @Override
    @Transactional
    public Task create(User owner, Task task) {
        if (owner.getRole() == Role.VIEWER) {
            throw new ForbiddenException("Viewers cannot create tasks");
        }
        if (owner.getRole() == Role.MEMBER) {
            if (task.getAssignedTo() != null && !task.getAssignedTo().getId().equals(owner.getId())) {
                throw new ForbiddenException("Members can only assign tasks to themselves");
            }
        }
        ensureCanUseTeam(owner, task);
        task.setOwner(owner);
        Task saved = tasks.save(task);
 
        activityLogs.log(
                saved,
                owner,
                "TASK_CREATED",
                owner.getFullName() + " created task \"" + saved.getTitle() + "\""
        );
 
        if (saved.getAssignedTo() != null) {
            String assigneeName = saved.getAssignedTo().getFullName();
            activityLogs.log(
                    saved,
                    owner,
                    "TASK_ASSIGNED",
                    owner.getFullName() + " assigned \"" + saved.getTitle() + "\" to " + assigneeName
            );
        }
 
        return saved;
    }
 
    @Override
    public Task findByIdOrThrow(User owner, Long id) {
        Task t = tasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!canView(owner, t)) {
            throw new ForbiddenException("You do not have access to this task");
        }
        return t;
    }
 
    @Override
    @Transactional
    public Task update(User owner, Long id, Task updated) {
        Task existing = tasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!canEdit(owner, existing)) {
            throw new ForbiddenException("You cannot edit this task");
        }
        ensureCanUseTeam(owner, updated);
 
        var oldStatus = existing.getStatus();
        var oldPriority = existing.getPriority();
        var oldAssignedToId = existing.getAssignedTo() != null ? existing.getAssignedTo().getId() : null;
 
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDueDate(updated.getDueDate());
        existing.setStatus(updated.getStatus());
        existing.setPriority(updated.getPriority());
        existing.setAssignedTo(updated.getAssignedTo());
        existing.setTeam(updated.getTeam());
 
        Task saved = tasks.save(existing);
 
        if (oldStatus != saved.getStatus()) {
            activityLogs.log(
                    saved,
                    owner,
                    "TASK_STATUS_CHANGED",
                    owner.getFullName() + " changed status of \"" + saved.getTitle() +
                            "\" to " + formatStatus(saved.getStatus().name())
            );
        }
 
        if (oldPriority != saved.getPriority()) {
            activityLogs.log(
                    saved,
                    owner,
                    "TASK_PRIORITY_CHANGED",
                    owner.getFullName() + " changed priority of \"" + saved.getTitle() +
                            "\" to " + formatPriority(saved.getPriority().name())
            );
        }
 
        Long newAssignedToId = saved.getAssignedTo() != null ? saved.getAssignedTo().getId() : null;
        if ((oldAssignedToId == null && newAssignedToId != null)
                || (oldAssignedToId != null && !oldAssignedToId.equals(newAssignedToId))) {
 
            String assigneeName = saved.getAssignedTo() != null
                    ? saved.getAssignedTo().getFullName()
                    : "Unassigned";
 
            activityLogs.log(
                    saved,
                    owner,
                    "TASK_ASSIGNED",
                    owner.getFullName() + " assigned \"" + saved.getTitle() + "\" to " + assigneeName
            );
        }
 
        return saved;
    }
 
    @Override
    @Transactional
    public void delete(User owner, Long id) {
        Task existing = tasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!canEdit(owner, existing)) {
            throw new ForbiddenException("You cannot delete this task");
        }
        String title = existing.getTitle();
 
        // Log the deletion event BEFORE unlinking/deleting.
        activityLogs.log(
                null,
                owner,
                "TASK_DELETED",
                owner.getFullName() + " deleted task \"" + title + "\""
        );
 
        // ✅ Unlink all activity logs that point to this task to avoid FK violation
        activityLogRepo.unlinkTask(id);
 
        // Now it is safe to delete the task row.
        tasks.delete(existing);
    }
 
    private String formatStatus(String status) {
        if ("IN_PROGRESS".equals(status)) return "In Progress";
        if ("TODO".equals(status)) return "To-Do";
        return "Done";
    }
 
    private String formatPriority(String priority) {
        if ("HIGH".equals(priority)) return "High";
        if ("LOW".equals(priority)) return "Low";
        return "Medium";
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

    private boolean canEdit(User actor, Task task) {
        if (actor.getRole() == Role.ADMIN) return true;
        if (task.getOwner() != null && task.getOwner().getId().equals(actor.getId())) return true;

        if (actor.getRole() == Role.MANAGER && task.getTeam() != null) {
            return task.getTeam().getManager() != null && task.getTeam().getManager().getId().equals(actor.getId());
        }

        if (actor.getRole() == Role.MEMBER) {
            return task.getAssignedTo() != null && task.getAssignedTo().getId().equals(actor.getId());
        }

        return false;
    }

    private void ensureCanUseTeam(User actor, Task task) {
        if (task.getTeam() == null) return;

        if (actor.getRole() == Role.ADMIN) return;
        if (actor.getRole() == Role.MANAGER) {
            if (task.getTeam().getManager() != null && task.getTeam().getManager().getId().equals(actor.getId())) {
                return;
            }
            throw new ForbiddenException("Managers can only assign tasks to their own teams");
        }

        if (actor.getRole() == Role.MEMBER) {
            if (teamMembers.existsByIdTeamIdAndIdUserId(task.getTeam().getId(), actor.getId())) {
                return;
            }
            throw new ForbiddenException("You are not a member of this team");
        }

        throw new ForbiddenException("You cannot assign tasks to a team");
    }
}
