package com.skillbarter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Peer endorsement for a skill.
 *
 * Design Pattern: Observer — endorsement events can trigger notifications.
 * SOLID – SRP: models only the endorsement relationship.
 */
@Entity
@Table(name = "skill_endorsements",
       uniqueConstraints = @UniqueConstraint(columnNames = {"endorser_id", "skill_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class SkillEndorsement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user giving the endorsement */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endorser_id", nullable = false)
    private User endorser;

    /** The skill being endorsed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    /** Optional short note from endorser */
    @Column(length = 300)
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
