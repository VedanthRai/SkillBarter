package com.skillbarter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Individual step in a learning path.
 */
@Entity
@Table(name = "learning_path_steps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class LearningPathStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @Column(nullable = false)
    private Integer stepOrder;

    @Column(nullable = false)
    private String skillName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;  // Optional link to actual skill

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;
}
