package com.example.taskflow.repository;

import com.example.taskflow.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByJti(String jti);
    List<UserSession> findByUserIdOrderByLoginTimeDesc(Long userId);

    @Modifying
    @Transactional
    @Query("update UserSession s set s.lastActive = :lastActive where s.jti = :jti")
    int updateLastActiveByJti(@Param("jti") String jti, @Param("lastActive") Instant lastActive);

    @Modifying
    @Transactional
    @Query("delete from UserSession s where s.user.id = :userId and s.jti <> :keepJti")
    int deleteByUserIdAndJtiNot(@Param("userId") Long userId, @Param("keepJti") String keepJti);

    @Modifying
    @Transactional
    @Query("delete from UserSession s where s.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
