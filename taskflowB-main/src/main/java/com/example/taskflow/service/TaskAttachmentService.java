package com.example.taskflow.service;

import com.example.taskflow.domain.Role;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.TaskAttachment;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.AttachmentListDTO;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.TaskAttachmentRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TaskAttachmentService {

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMembers;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "application/pdf", 
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain", "application/zip", "application/x-zip-compressed"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip"
    );
    private static final int MAX_ATTACHMENTS = 5;
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    public TaskAttachmentService(TaskAttachmentRepository attachmentRepository, 
                                 TaskRepository taskRepository,
                                 TeamMemberRepository teamMembers) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.teamMembers = teamMembers;
    }

    public AttachmentListDTO uploadAttachment(Long taskId, User actor, MultipartFile file) throws IOException {
        if (actor.getRole() == Role.VIEWER) {
            throw new ForbiddenException("Viewers cannot upload attachments");
        }

        Task task = findTaskVisibleToActor(taskId, actor);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds 5 MB limit.");
        }

        if (!isAllowedMimeType(file.getContentType()) || !isAllowedExtension(file.getOriginalFilename())) {
            throw new IllegalArgumentException("File type not allowed");
        }

        long currentCount = attachmentRepository.countByTaskId(taskId);
        if (currentCount >= MAX_ATTACHMENTS) {
            throw new IllegalArgumentException("Maximum 5 files reached");
        }

        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setUploader(actor);
        attachment.setOriginalName(sanitizeOriginalName(file.getOriginalFilename()));
        attachment.setMimeType(file.getContentType());
        attachment.setFileSizeBytes(file.getSize());
        attachment.setFileData(file.getBytes());

        TaskAttachment saved = attachmentRepository.save(attachment);

        return new AttachmentListDTO(
                saved.getId(),
                task.getId(),
                actor.getId(),
                actor.getFullName(),
                saved.getOriginalName(),
                saved.getMimeType(),
                saved.getFileSizeBytes(),
                saved.getUploadedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<AttachmentListDTO> getAttachmentsForTask(Long taskId, User actor) {
        findTaskVisibleToActor(taskId, actor);
        return attachmentRepository.findMetadataByTaskIdOrderByUploadedAtAsc(taskId);
    }

    @Transactional(readOnly = true)
    public TaskAttachment getAttachmentForDownload(Long attachmentId, User actor) {
        TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        if (!canView(actor, attachment.getTask())) {
            throw new ForbiddenException("You do not have access to this task");
        }
        return attachment;
    }

    public void deleteAttachment(Long attachmentId, User actor) {
        TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        if (!canView(actor, attachment.getTask())) {
            throw new ForbiddenException("You do not have access to this task");
        }

        if (!attachment.getUploader().getId().equals(actor.getId())
            && actor.getRole() != Role.ADMIN
            && actor.getRole() != Role.MANAGER) {
            throw new ForbiddenException("Not authorized to delete this attachment");
        }

        attachmentRepository.delete(attachment);
    }

    private Task findTaskVisibleToActor(Long taskId, User actor) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!canView(actor, task)) {
            throw new ForbiddenException("You do not have access to this task");
        }
        return task;
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

    private boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) return false;
        String normalized = mimeType.toLowerCase(Locale.ROOT);
        if (ALLOWED_MIME_TYPES.contains(normalized)) return true;
        return normalized.startsWith("application/vnd.openxmlformats-officedocument.");
    }

    private boolean isAllowedExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) return false;
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    private String sanitizeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "attachment";
        }
        return originalName.replaceAll("[\\r\\n]", "_").trim();
    }
}
