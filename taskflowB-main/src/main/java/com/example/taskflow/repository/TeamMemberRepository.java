package com.example.taskflow.repository;

import com.example.taskflow.domain.TeamMember;
import com.example.taskflow.domain.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
    boolean existsByIdTeamIdAndIdUserId(Long teamId, Long userId);

    List<TeamMember> findByIdTeamId(Long teamId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from TeamMember tm where tm.id.userId = :userId")
    int deleteByIdUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from TeamMember tm where tm.id.teamId = :teamId")
    int deleteByIdTeamId(@Param("teamId") Long teamId);

    long countByIdTeamId(Long teamId);

    @Query("select tm.id.teamId from TeamMember tm where tm.id.userId = :userId")
    List<Long> findTeamIdsByUserId(@Param("userId") Long userId);

    @Query("select tm.team from TeamMember tm where tm.user.id = :userId")
    List<com.example.taskflow.domain.Team> findTeamsByUserId(@Param("userId") Long userId);
}
