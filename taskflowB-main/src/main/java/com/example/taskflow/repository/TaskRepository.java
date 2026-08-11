package com.example.taskflow.repository;

import com.example.taskflow.domain.Priority;
import com.example.taskflow.domain.Task;
import com.example.taskflow.domain.TaskStatus;
import com.example.taskflow.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwner(User owner);
    List<Task> findByOwnerAndPriority(User owner, Priority priority);
    List<Task> findByOwnerAndPriorityIn(User owner, List<Priority> priorities);
    Optional<Task> findByIdAndOwner(Long id, User owner);

    long countByOwner(User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.status = com.example.taskflow.domain.TaskStatus.TODO")
    long countTodoByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.status = com.example.taskflow.domain.TaskStatus.IN_PROGRESS")
    long countInProgressByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.status = com.example.taskflow.domain.TaskStatus.DONE")
    long countDoneByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.priority = com.example.taskflow.domain.Priority.HIGH")
    long countHighByOwner(@Param("owner") User owner);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE user_id = :ownerId AND priority IN ('MED', 'MEDIUM')", nativeQuery = true)
    long countMediumByOwner(@Param("ownerId") Long ownerId);

    @Query("select count(t) from Task t where t.owner = :owner and t.priority = com.example.taskflow.domain.Priority.LOW")
    long countLowByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.dueDate < CURRENT_DATE and t.status <> com.example.taskflow.domain.TaskStatus.DONE")
    long overdueCountByOwner(@Param("owner") User owner);

    @Query("select count(t) from Task t where t.owner = :owner and t.dueDate = CURRENT_DATE and t.status <> com.example.taskflow.domain.TaskStatus.DONE")
    long dueTodayCountByOwner(@Param("owner") User owner);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE user_id = :ownerId AND DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)", 
           nativeQuery = true)
    long countTasksThisWeekByOwnerId(@Param("ownerId") Long ownerId);

    @Query(value = """
        SELECT COALESCE(
            ROUND(
                (SUM(CASE WHEN status='DONE' THEN 1 ELSE 0 END) * 100.0) 
                / NULLIF(COUNT(*), 0), 1
            ),
        0)
        FROM tasks 
        WHERE user_id = :ownerId
        """, 
        nativeQuery = true)
    double completionRateByOwnerId(@Param("ownerId") Long ownerId);

    @Query(value = """
        SELECT DISTINCT t.*
        FROM tasks t
        LEFT JOIN team_members tm ON tm.team_id = t.team_id
        WHERE t.user_id = :userId
           OR t.assigned_to = :userId
           OR tm.user_id = :userId
        """, nativeQuery = true)
    List<Task> findVisibleTasks(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE team_id = :teamId AND status <> 'DONE'", nativeQuery = true)
    long countActiveByTeamId(@Param("teamId") Long teamId);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE team_id = :teamId AND assigned_to = :userId", nativeQuery = true)
    long countAssignedToUserInTeam(@Param("teamId") Long teamId, @Param("userId") Long userId);
}
