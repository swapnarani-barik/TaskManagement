package com.example.taskflow.service.impl;

import com.example.taskflow.domain.Role;
import com.example.taskflow.domain.Team;
import com.example.taskflow.domain.TeamMember;
import com.example.taskflow.domain.TeamMemberId;
import com.example.taskflow.domain.User;
import com.example.taskflow.dto.TeamCreateRequest;
import com.example.taskflow.dto.TeamUpdateRequest;
import com.example.taskflow.exception.ForbiddenException;
import com.example.taskflow.exception.ResourceNotFoundException;
import com.example.taskflow.repository.TeamMemberRepository;
import com.example.taskflow.repository.TeamRepository;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.service.TeamService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teams;
    private final TeamMemberRepository teamMembers;
    private final UserRepository users;

    public TeamServiceImpl(TeamRepository teams, TeamMemberRepository teamMembers, UserRepository users) {
        this.teams = teams;
        this.teamMembers = teamMembers;
        this.users = users;
    }

    @Override
    @Transactional
    public Team createTeam(User actor, TeamCreateRequest req) {
        Team t = new Team();
        t.setName(req.getName());
        t.setDescription(req.getDescription());

        User manager;
        if (actor.getRole() == Role.ADMIN && req.getManagerId() != null) {
            manager = users.findById(req.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("manager not found"));
        } else {
            manager = actor;
        }
        t.setManager(manager);

        Team saved = teams.save(t);

        ensureMember(saved, manager);
        if (req.getMemberIds() != null) {
            for (Long memberId : req.getMemberIds()) {
                if (memberId == null) continue;
                User u = users.findById(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("user not found"));
                ensureMember(saved, u);
            }
        }

        return saved;
    }

    @Override
    @Transactional
    public Team updateTeam(User actor, Long teamId, TeamUpdateRequest req) {
        Team team = teams.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (!canManage(actor, team)) {
            throw new ForbiddenException("You cannot manage this team");
        }

        team.setName(req.getName().trim());
        team.setDescription(req.getDescription());

        if (req.getManagerId() != null && !req.getManagerId().equals(team.getManager().getId())) {
            User newManager = users.findById(req.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("manager not found"));
            team.setManager(newManager);
            ensureMember(team, newManager);
        }

        return teams.save(team);
    }

    @Override
    public List<Team> listTeams(User actor) {
        if (actor.getRole() == Role.ADMIN) {
            return teams.findAll();
        }
        if (actor.getRole() == Role.MANAGER) {
            return teams.findByManagerId(actor.getId());
        }
        List<Long> ids = teamMembers.findTeamIdsByUserId(actor.getId());
        if (ids.isEmpty()) return List.of();
        return teams.findAllById(ids);
    }

    @Override
    public Team getTeam(User actor, Long teamId) {
        Team t = teams.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (!canView(actor, t)) {
            throw new ForbiddenException("You do not have access to this team");
        }
        return t;
    }

    @Override
    @Transactional
    public void addMember(User actor, Long teamId, Long userId) {
        Team t = teams.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (!canManage(actor, t)) {
            throw new ForbiddenException("You cannot manage this team");
        }

        User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        ensureMember(t, u);
    }

    @Override
    @Transactional
    public void removeMember(User actor, Long teamId, Long userId) {
        Team t = teams.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (!canManage(actor, t)) {
            throw new ForbiddenException("You cannot manage this team");
        }

        if (t.getManager() != null && t.getManager().getId().equals(userId)) {
            throw new ForbiddenException("Cannot remove the team manager");
        }

        teamMembers.deleteById(new TeamMemberId(teamId, userId));
    }

    @Override
    @Transactional
    public void deleteTeam(User actor, Long teamId) {
        Team t = teams.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (!canManage(actor, t)) {
            throw new ForbiddenException("You cannot delete this team");
        }
        teamMembers.deleteByIdTeamId(teamId);
        teamMembers.flush();
        teams.delete(t);
    }

    private boolean canView(User actor, Team team) {
        if (actor.getRole() == Role.ADMIN) return true;
        if (team.getManager() != null && team.getManager().getId().equals(actor.getId())) return true;
        return teamMembers.existsByIdTeamIdAndIdUserId(team.getId(), actor.getId());
    }

    private boolean canManage(User actor, Team team) {
        if (actor.getRole() == Role.ADMIN) return true;
        return actor.getRole() == Role.MANAGER
                && team.getManager() != null
                && team.getManager().getId().equals(actor.getId());
    }

    private void ensureMember(Team team, User user) {
        if (teamMembers.existsByIdTeamIdAndIdUserId(team.getId(), user.getId())) return;
        teamMembers.save(new TeamMember(team, user));
    }
}
