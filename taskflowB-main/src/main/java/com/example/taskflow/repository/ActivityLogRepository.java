package com.example.taskflow.repository;

import com.example.taskflow.domain.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

 
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
 
    @Query("""
        select a
        from ActivityLog a
        left join a.task t
        where (
            t.owner.id = :userId
            or t.assignedTo.id = :userId
            or (a.task is null and a.actor.id = :userId)
        )
        and a.actor is not null
        order by a.createdAt desc
        """)
    List<ActivityLog> findRecentForUser(@Param("userId") Long userId, Pageable pageable);
 
    /**
     * Unlink all activity logs from the given task to avoid FK violations when deleting the task.
     * This assumes activity_log.task_id is nullable in the DB. If your column is NOT NULL,
     * either alter the column to allow null OR change this to a delete-by-task instead.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ActivityLog a set a.task = null where a.task.id = :taskId")
    int unlinkTask(@Param("taskId") Long taskId);
}
 