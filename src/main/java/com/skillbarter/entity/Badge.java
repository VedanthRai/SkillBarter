package com.skillbarter.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "badges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String badgeName;    // e.g., "Verified Skill: Java", "7-Day Streak", "Top Teacher"

    @Column(nullable = false)
    private String badgeType;    // SKILL_VERIFIED | STREAK | ACHIEVEMENT | TOP_RANKED

    @Column
    private String iconUrl;

    @Column(length = 500)
    private String description;

    /** Optional reference to the verified skill */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime awardedAt;
}
