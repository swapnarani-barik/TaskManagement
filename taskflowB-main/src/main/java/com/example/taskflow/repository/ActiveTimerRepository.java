package com.example.taskflow.repository;

import com.example.taskflow.domain.ActiveTimer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActiveTimerRepository extends JpaRepository<ActiveTimer, Long> {
    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    Optional<ActiveTimer> findByTaskIdAndUserId(Long taskId, Long userId);
}

