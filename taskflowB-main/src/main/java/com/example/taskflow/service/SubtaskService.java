package com.example.taskflow.service;

import com.example.taskflow.domain.Role;
import com.example.taskflow.domain.Subtask;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.SubtaskCreateRequest;
import com.example.taskflow.dto.SubtaskResponse;
import com.example.taskflow.dto.SubtaskSummaryResponse;
import com.example.taskflow.dto.SubtaskUpdateRequest;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.SubtaskRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.TeamMemberRepository;
import com.example.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class SubtaskService {

    private final SubtaskRepository subtasks;
    private final TaskRepository tasks;
    private final UserRepository users;
    private final TeamMemberRepository teamMembers;

    public SubtaskService(
            SubtaskRepository subtasks,
            TaskRepository tasks,
            UserRepository users,
            TeamMemberRepository teamMembers
    ) {
        this.subtasks = subtasks;
        this.tasks = tasks;
        this.users = users;
        this.teamMembers = teamMembers;
    }

    @Transactional(readOnly = true)
    public List<SubtaskResponse> list(User actor, Long taskId) {
        Task task = findTaskVisibleToActor(taskId, actor);
        return subtasks.findByTaskIdOrderByCreatedAtAsc(task.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SubtaskResponse create(User actor, Long taskId, SubtaskCreateRequest req) {
        if (actor.getRole() == Role.VIEWER) {
            throw new ForbiddenException("Viewers cannot create subtasks");
        }
        Task task = findTaskVisibleToActor(taskId, actor);

        Subtask st = new Subtask();
        st.setTask(task);
        st.setTitle(req.getTitle().trim());
        st.setCreatedBy(actor);
        st.setComplete(false);
        st.setCompletedAt(null);

        if (req.getAssignedTo() != null) {
            User assignee = users.findById(req.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            st.setAssignedTo(assignee);
        }

        return toResponse(subtasks.save(st));
    }

    public SubtaskResponse toggle(User actor, Long subtaskId) {
        if (actor.getRole() == Role.VIEWER) {
            throw new ForbiddenException("Viewers cannot update subtasks");
        }
        Subtask st = subtasks.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));
        ensureTaskVisible(actor, st.getTask());

        boolean next = !st.isComplete();
        st.setComplete(next);
        st.setCompletedAt(next ? Instant.now() : null);
        return toResponse(subtasks.save(st));
    }

    public SubtaskResponse update(User actor, Long subtaskId, SubtaskUpdateRequest req) {
        if (actor.getRole() == Role.VIEWER) {
            throw new ForbiddenException("Viewers cannot update subtasks");
        }
        Subtask st = subtasks.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));
        ensureTaskVisible(actor, st.getTask());

        st.setTitle(req.getTitle().trim());
        if (req.getAssignedTo() == null) {
            st.setAssignedTo(null);
        } else {
            User assignee = users.findById(req.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            st.setAssignedTo(assignee);
        }

        return toResponse(subtasks.save(st));
    }

    public void delete(User actor, Long subtaskId) {
        Subtask st = subtasks.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));
        ensureTaskVisible(actor, st.getTask());

        boolean allowed = actor.getRole() == Role.ADMIN
                || actor.getRole() == Role.MANAGER
                || st.getCreatedBy().getId().equals(actor.getId());
        if (!allowed) {
            throw new ForbiddenException("Not authorized to delete this subtask");
        }

        subtasks.delete(st);
    }

    @Transactional(readOnly = true)
    public SubtaskSummaryResponse summary(User actor, Long taskId) {
        Task task = findTaskVisibleToActor(taskId, actor);
        int total = subtasks.countByTaskId(task.getId());
        int completed = subtasks.countByTaskIdAndCompleteTrue(task.getId());
        return new SubtaskSummaryResponse(total, completed);
    }

    private SubtaskResponse toResponse(Subtask st) {
        return new SubtaskResponse(
                st.getId(),
                st.getTask().getId(),
                st.getTitle(),
                st.isComplete(),
                st.getAssignedTo() != null ? st.getAssignedTo().getId() : null,
                st.getAssignedTo() != null ? st.getAssignedTo().getFullName() : null,
                st.getCreatedBy().getId(),
                st.getCreatedBy().getFullName(),
                st.getCreatedAt(),
                st.getCompletedAt()
        );
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

