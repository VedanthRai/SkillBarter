package com.skillbarter.entity;

import com.skillbarter.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Dispute Tribunal entity (Major Feature 4).
 *
 * Dispute State Machine (State Diagram 4):
 *   OPEN → UNDER_REVIEW → RESOLVED_TEACHER | RESOLVED_LEARNER → CLOSED
 *
 * SOLID – SRP: models only dispute metadata and decision.
 */
@Entity
@Table(name = "disputes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by_id", nullable = false)
    private User raisedBy;

    /** Verifier (ROLE_VERIFIER) assigned to adjudicate */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_verifier_id")
    private User assignedVerifier;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 2000)
    private String evidence;  // URLs or paths to uploaded evidence

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(length = 2000)
    private String resolution;  // Verifier's written ruling

    @Column
    private LocalDateTime assignedAt;

    @Column
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
