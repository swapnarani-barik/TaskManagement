package com.example.taskflow.service;

import com.example.taskflow.domain.Team;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.TeamCreateRequest;
import com.example.taskflow.dto.TeamUpdateRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TeamService {

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    Team createTeam(User actor, TeamCreateRequest req);

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    Team updateTeam(User actor, Long teamId, TeamUpdateRequest req);

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    List<Team> listTeams(User actor);

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER','VIEWER')")
    Team getTeam(User actor, Long teamId);

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    void addMember(User actor, Long teamId, Long userId);

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    void removeMember(User actor, Long teamId, Long userId);

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    void deleteTeam(User actor, Long teamId);
}
