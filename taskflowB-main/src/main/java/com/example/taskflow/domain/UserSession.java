package com.example.taskflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "user_sessions",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_sessions_jti", columnNames = "jti")
)
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jti", nullable = false, length = 100)
    private String jti;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_sessions_users"))
    private User user;

    @Column(name = "device_hint", length = 200)
    private String deviceHint;

    @Column(name = "login_time", nullable = false)
    private Instant loginTime = Instant.now();

    @Column(name = "last_active", nullable = false)
    private Instant lastActive = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public Long getId() { return id; }

    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDeviceHint() { return deviceHint; }
    public void setDeviceHint(String deviceHint) { this.deviceHint = deviceHint; }

    public Instant getLoginTime() { return loginTime; }
    public void setLoginTime(Instant loginTime) { this.loginTime = loginTime; }

    public Instant getLastActive() { return lastActive; }
    public void setLastActive(Instant lastActive) { this.lastActive = lastActive; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
