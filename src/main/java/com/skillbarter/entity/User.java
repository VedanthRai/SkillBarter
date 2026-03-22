package com.skillbarter.entity;

import com.skillbarter.enums.Role;
import com.skillbarter.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core User entity.
 *
 * SOLID – SRP: manages only identity and credit balance.
 * SOLID – OCP: extended by badge/gamification relationships,
 *              not by modifying this class.
 *
 * Participates in:
 *  - User Account State Machine (UserStatus)
 *  - Strategy Pattern (matching engine reads skills offered/wanted)
 *  - Observer Pattern (source of Notification events)
 */
@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "username")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 500)
    private String bio;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // ── Time Credits ────────────────────────────────────────
    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    @Builder.Default
    private BigDecimal creditBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    @Builder.Default
    private BigDecimal escrowBalance = BigDecimal.ZERO; // credits held pending

    // ── Gamification ────────────────────────────────────────
    @Column(nullable = false)
    @Builder.Default
    private Integer streakCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSessionsCompleted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reputationScore = 0;

    @Column
    private LocalDateTime lastSessionDate;

    // ── Preferences ─────────────────────────────────────────
    /** IANA timezone ID, e.g. "Asia/Kolkata", "America/New_York" */
    @Column(length = 60)
    @Builder.Default
    private String timezone = "UTC";

    // ── Verification ────────────────────────────────────────
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column
    private String emailVerificationToken;

    // ── Account State ────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    // ── Relationships ────────────────────────────────────────
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "learner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Session> sessionsAsLearner = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Session> sessionsAsTeacher = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Badge> badges = new ArrayList<>();

    // ── Audit ────────────────────────────────────────────────
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Domain Logic ─────────────────────────────────────────
    public boolean hasEnoughCredits(BigDecimal amount) {
        return creditBalance.compareTo(amount) >= 0;
    }

    public void deductCredits(BigDecimal amount) {
        if (!hasEnoughCredits(amount)) {
            throw new IllegalStateException("Insufficient credits: balance=" + creditBalance + " required=" + amount);
        }
        this.creditBalance = this.creditBalance.subtract(amount);
        this.escrowBalance = this.escrowBalance.add(amount);
    }

    public void releaseEscrow(BigDecimal amount) {
        this.escrowBalance = this.escrowBalance.subtract(amount);
    }

    public void addCredits(BigDecimal amount) {
        this.creditBalance = this.creditBalance.add(amount);
    }

    public void refundFromEscrow(BigDecimal amount) {
        this.escrowBalance = this.escrowBalance.subtract(amount);
        this.creditBalance = this.creditBalance.add(amount);
    }

    public void incrementStreak() {
        this.streakCount++;
        this.totalSessionsCompleted++;
        this.lastSessionDate = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Integer getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(Integer streakCount) {
        this.streakCount = streakCount;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
