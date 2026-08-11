package com.example.taskflow.controller;

import com.example.taskflow.domain.TaskAttachment;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.AttachmentListDTO;
import com.example.taskflow.security.UserPrincipal;
import com.example.taskflow.service.TaskAttachmentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskAttachmentController {

    private final TaskAttachmentService attachmentService;

    public TaskAttachmentController(TaskAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    private User currentUser() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<AttachmentListDTO> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) throws IOException {
        AttachmentListDTO dto = attachmentService.uploadAttachment(taskId, currentUser(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentListDTO>> getAttachments(@PathVariable Long taskId) {
        List<AttachmentListDTO> attachments = attachmentService.getAttachmentsForTask(taskId, currentUser());
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId) {
        TaskAttachment attachment = attachmentService.getAttachmentForDownload(attachmentId, currentUser());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(attachment.getOriginalName()).build().toString())
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .contentLength(attachment.getFileSizeBytes())
                .body(attachment.getFileData());
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId, currentUser());
        return ResponseEntity.noContent().build();
    }
}
