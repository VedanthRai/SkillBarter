package com.skillbarter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "wishlist_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    /** If true, user wants email/in-app alert when teacher adds availability */
    @Column(nullable = false)
    @Builder.Default
    private Boolean alertEnabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;
}
