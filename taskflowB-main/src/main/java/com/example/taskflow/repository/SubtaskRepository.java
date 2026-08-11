package com.example.taskflow.repository;

import com.example.taskflow.domain.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    List<Subtask> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    int countByTaskId(Long taskId);

    int countByTaskIdAndCompleteTrue(Long taskId);
}

