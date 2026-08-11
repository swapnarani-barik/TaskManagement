package com.example.taskflow.service;

import com.example.taskflow.domain.ThemePreference;
import com.example.taskflow.domain.TokenBlocklist;
import com.example.taskflow.domain.User;
import com.example.taskflow.domain.UserPreference;
import com.example.taskflow.domain.UserSession;
import com.example.taskflow.dto.ChangePasswordRequest;
import com.example.taskflow.dto.DeleteAccountRequest;
import com.example.taskflow.dto.MeProfileResponse;
import com.example.taskflow.dto.PreferencesResponse;
import com.example.taskflow.dto.UpdatePreferencesRequest;
import com.example.taskflow.dto.UpdateProfileRequest;
import com.example.taskflow.dto.UserSessionResponse;
import com.example.taskflow.repository.TokenBlocklistRepository;
import com.example.taskflow.repository.UserPreferenceRepository;
import com.example.taskflow.repository.UserRepository;
import com.example.taskflow.repository.UserSessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class UserSettingsService {

    private final UserRepository users;
    private final UserPreferenceRepository preferences;
    private final UserSessionRepository sessions;
    private final TokenBlocklistRepository blocklist;
    private final PasswordEncoder passwordEncoder;

    public UserSettingsService(
            UserRepository users,
            UserPreferenceRepository preferences,
            UserSessionRepository sessions,
            TokenBlocklistRepository blocklist,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.preferences = preferences;
        this.sessions = sessions;
        this.blocklist = blocklist;
        this.passwordEncoder = passwordEncoder;
    }

    public MeProfileResponse getMe(User actor) {
        UserPreference pref = ensurePreference(actor);
        return toProfile(actor, pref);
    }

    @Transactional
    public MeProfileResponse updateProfile(User actor, UpdateProfileRequest req) {
        String newName = req.getFullName().trim();
        String newEmail = req.getEmail().trim().toLowerCase(Locale.ROOT);

        if (!newEmail.equalsIgnoreCase(actor.getEmail())) {
            if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("Current password is required to change email");
            }
            if (!passwordEncoder.matches(req.getCurrentPassword(), actor.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            if (users.existsByEmailAndIdNot(newEmail, actor.getId())) {
                throw new IllegalArgumentException("Email already registered");
            }
            actor.setEmail(newEmail);
        }

        actor.setFullName(newName);
        users.save(actor);

        UserPreference pref = ensurePreference(actor);
        if (req.getAvatarColour() != null && !req.getAvatarColour().isBlank()) {
            pref.setAvatarColour(req.getAvatarColour().trim());
        }
        pref.setBio(req.getBio());
        preferences.save(pref);

        return toProfile(actor, pref);
    }

    @Transactional
    public void changePassword(User actor, ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), actor.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Confirm password does not match");
        }
        actor.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        users.save(actor);
    }

    @Transactional
    public PreferencesResponse updatePreferences(User actor, UpdatePreferencesRequest req) {
        UserPreference pref = ensurePreference(actor);

        if (req.getTheme() != null && !req.getTheme().isBlank()) {
            pref.setTheme(ThemePreference.valueOf(req.getTheme().trim().toUpperCase(Locale.ROOT)));
        }
        if (req.getNotifyAssigned() != null) pref.setNotifyAssigned(req.getNotifyAssigned());
        if (req.getNotifyComment() != null) pref.setNotifyComment(req.getNotifyComment());
        if (req.getNotifySubtask() != null) pref.setNotifySubtask(req.getNotifySubtask());
        if (req.getNotifyOverdue() != null) pref.setNotifyOverdue(req.getNotifyOverdue());
        if (req.getNotifyTeam() != null) pref.setNotifyTeam(req.getNotifyTeam());

        UserPreference saved = preferences.save(pref);
        return toPreferences(saved);
    }

    public PreferencesResponse getPreferences(User actor) {
        return toPreferences(ensurePreference(actor));
    }

    @Transactional
    public void deleteOwnAccount(User actor, DeleteAccountRequest req, String currentJti) {
        if (!actor.getEmail().equalsIgnoreCase(req.getConfirmEmail().trim())) {
            throw new IllegalArgumentException("Confirm email does not match your account email");
        }
        if (currentJti != null && !currentJti.isBlank()) {
            revokeSession(actor, currentJti);
        }
        sessions.deleteByUserId(actor.getId());
        users.delete(actor);
    }

    public List<UserSessionResponse> listSessions(User actor, String currentJti) {
        return sessions.findByUserIdOrderByLoginTimeDesc(actor.getId())
                .stream()
                .map(s -> new UserSessionResponse(
                        s.getJti(),
                        s.getDeviceHint(),
                        s.getLoginTime(),
                        s.getLastActive(),
                        s.getJti().equals(currentJti)
                ))
                .toList();
    }

    @Transactional
    public void revokeSession(User actor, String jti) {
        UserSession session = sessions.findByJti(jti)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getUser().getId().equals(actor.getId())) {
            throw new IllegalArgumentException("Session does not belong to current user");
        }
        blockToken(session);
        sessions.delete(session);
    }

    @Transactional
    public void revokeAllOtherSessions(User actor, String currentJti) {
        List<UserSession> all = sessions.findByUserIdOrderByLoginTimeDesc(actor.getId());
        for (UserSession session : all) {
            if (session.getJti().equals(currentJti)) {
                continue;
            }
            blockToken(session);
        }
        sessions.deleteByUserIdAndJtiNot(actor.getId(), currentJti);
    }

    @Transactional
    public void recordLogin(User user, String jti, String deviceHint, Instant expiresAt) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setJti(jti);
        session.setDeviceHint(deviceHint);
        session.setLoginTime(Instant.now());
        session.setLastActive(Instant.now());
        session.setExpiresAt(expiresAt);
        sessions.save(session);
    }

    private void blockToken(UserSession session) {
        if (blocklist.existsById(session.getJti())) {
            return;
        }
        TokenBlocklist row = new TokenBlocklist();
        row.setJti(session.getJti());
        row.setUser(session.getUser());
        row.setRevokedAt(Instant.now());
        row.setExpiresAt(session.getExpiresAt());
        blocklist.save(row);
    }

    private UserPreference ensurePreference(User user) {
        return preferences.findById(user.getId())
                .orElseGet(() -> {
                    UserPreference pref = new UserPreference();
                    pref.setUserId(user.getId());
                    return preferences.save(pref);
                });
    }

    private MeProfileResponse toProfile(User user, UserPreference pref) {
        return new MeProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                pref.getAvatarColour(),
                pref.getBio()
        );
    }

    private PreferencesResponse toPreferences(UserPreference pref) {
        return new PreferencesResponse(
                pref.getTheme().name(),
                pref.isNotifyAssigned(),
                pref.isNotifyComment(),
                pref.isNotifySubtask(),
                pref.isNotifyOverdue(),
                pref.isNotifyTeam(),
                pref.getAvatarColour(),
                pref.getBio()
        );
    }
}
