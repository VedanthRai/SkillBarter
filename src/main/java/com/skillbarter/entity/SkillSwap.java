package com.skillbarter.entity;

import com.skillbarter.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Skill Swap — direct barter without credits.
 * User A teaches User B skill X, User B teaches User A skill Y.
 */
@Entity
@Table(name = "skill_swaps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class SkillSwap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_a_id", nullable = false)
    private Skill skillA;  // A teaches this to B

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_b_id", nullable = false)
    private Skill skillB;  // B teaches this to A

    @Column(nullable = false)
    private LocalDateTime sessionAScheduledAt;

    @Column(nullable = false)
    private LocalDateTime sessionBScheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.REQUESTED;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sessionACompleted = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sessionBCompleted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
