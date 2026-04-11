package com.skillbarter.entity;

import com.skillbarter.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Skill Request Board — learners post what they want to learn.
 * Teachers can browse and offer to teach.
 */
@Entity
@Table(name = "skill_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class SkillRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @Column(nullable = false)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
