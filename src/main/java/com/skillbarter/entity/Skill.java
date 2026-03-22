package com.skillbarter.entity;

import com.skillbarter.enums.SkillCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A skill that a user can teach or has requested to learn.
 *
 * SOLID – SRP: purely describes a teachable competency.
 * Used by the Smart Matching Engine (Strategy Pattern).
 */
@Entity
@Table(name = "skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category;

    /** Credits charged per hour for this skill */
    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal hourlyRate = BigDecimal.ONE;

    /** Proficiency level: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT */
    @Column
    private String proficiencyLevel;

    /** Path to uploaded certificate PDF/image */
    @Column
    private String certificatePath;

    /** Set to true by admin/verifier after certificate review */
    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isOffering = true; // true = teaching this skill, false = wanting to learn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Session> sessions = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSessions = 0;

    /** Number of peer endorsements received */
    @Column(nullable = false)
    @Builder.Default
    private Integer endorsementCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateRating(int newRating) {
        double total = this.averageRating * this.totalRatings + newRating;
        this.totalRatings++;
        this.averageRating = total / this.totalRatings;
        this.totalSessions++;
    }
}
