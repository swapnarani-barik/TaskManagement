package com.example.taskflow.dto;

import java.time.Instant;

public class AttachmentListDTO {

    private Long id;
    private Long taskId;
    private Long uploaderId;
    private String uploaderName;
    private String originalName;
    private String mimeType;
    private Long fileSizeBytes;
    private Instant uploadedAt;

    public AttachmentListDTO(Long id, Long taskId, Long uploaderId, String uploaderName, String originalName, String mimeType, Long fileSizeBytes, Instant uploadedAt) {
        this.id = id;
        this.taskId = taskId;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.uploadedAt = uploadedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
