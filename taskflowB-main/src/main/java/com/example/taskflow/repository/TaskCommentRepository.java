package com.example.taskflow.repository;

import com.example.taskflow.domain.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
	 
    // Existing simple query (kept for reference/other callers)
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
 
    Optional<TaskComment> findByIdAndAuthorId(Long commentId, Long authorId);
 
    // ✅ NEW: fetch-join author and task so they are initialized when mapping to DTO
    @Query("""
        select c
        from TaskComment c
        join fetch c.author a
        join fetch c.task t
        where t.id = :taskId
        order by c.createdAt asc
        """)
    List<TaskComment> findByTaskIdWithAuthor(@Param("taskId") Long taskId);
}