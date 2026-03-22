package com.skillbarter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed credit ledger entry for the Wallet page.
 *
 * Every credit movement (top-up, escrow, release, refund, bonus)
 * is recorded here for full auditability.
 *
 * SOLID – SRP: pure ledger record, no business logic.
 */
@Entity
@Table(name = "credit_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class CreditTransaction {

    public enum CreditTxType {
        SIGNUP_BONUS, ADMIN_GRANT, ESCROW_HOLD, ESCROW_RELEASE,
        ESCROW_REFUND, SESSION_EARNED, STREAK_BONUS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditTxType type;

    /** Positive = credit added, Negative = credit deducted */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Balance after this transaction */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String description;

    /** Optional reference to the session that caused this movement */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
