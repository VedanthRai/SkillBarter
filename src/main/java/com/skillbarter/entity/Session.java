package com.skillbarter.entity;

import com.skillbarter.enums.SessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A learning session between a learner and a teacher for a specific skill.
 *
 * Session lifecycle drives the Escrow flow:
 *   REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED
 *                                      → DISPUTED
 *                        → CANCELLED
 *
 * SOLID – SRP: manages only scheduling and session state.
 */
@Entity
@Table(name = "sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer durationMinutes = 60;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.REQUESTED;

    /** Credits locked for this session */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal creditAmount;

    /** Additional notes from learner */
    @Column(length = 500)
    private String learnerNotes;

    /** Feedback from teacher after session */
    @Column(length = 500)
    private String teacherNotes;

    /** Optional Zoom / Google Meet / Teams URL shared for the live class */
    @Column(length = 500)
    private String meetingLink;

    /** True if learner confirmed session completed */
    @Column(nullable = false)
    @Builder.Default
    private Boolean learnerConfirmed = false;

    /** True if teacher confirmed session completed */
    @Column(nullable = false)
    @Builder.Default
    private Boolean teacherConfirmed = false;

    /** Path to generated PDF receipt */
    @Column
    private String receiptPath;

    /** The linked transaction (1-to-1) */
    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private Transaction transaction;

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private Review review;

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private Dispute dispute;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<SessionMessage> messages = new ArrayList<>();

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isBothConfirmed() {
        return Boolean.TRUE.equals(learnerConfirmed) && Boolean.TRUE.equals(teacherConfirmed);
    }

    public BigDecimal computeCreditAmount() {
        // 1 credit per hour; fraction for partial hours
        double hours = durationMinutes / 60.0;
        return skill.getHourlyRate().multiply(BigDecimal.valueOf(hours));
    }
}
