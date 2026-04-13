package com.skillbarter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
// Represents referral codes and tracking
/**
 * Referral system — user invites friends.
 */
@Entity
@Table(name = "referral_codes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class ReferralCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", nullable = false)
    private User referrer;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
