package com.example.taskflow.repository;

import com.example.taskflow.domain.TaskAttachment;
import com.example.taskflow.dto.AttachmentListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    @Query("""
        select new com.example.taskflow.dto.AttachmentListDTO(
            a.id,
            a.task.id,
            a.uploader.id,
            a.uploader.fullName,
            a.originalName,
            a.mimeType,
            a.fileSizeBytes,
            a.uploadedAt
        )
        from TaskAttachment a
        where a.task.id = :taskId
        order by a.uploadedAt asc
        """)
    List<AttachmentListDTO> findMetadataByTaskIdOrderByUploadedAtAsc(@Param("taskId") Long taskId);

    long countByTaskId(Long taskId);
}
