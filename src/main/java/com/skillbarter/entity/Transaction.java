package com.skillbarter.entity;

import com.skillbarter.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Time-credit transaction representing the escrow lifecycle.
 *
 * Design Pattern: State Pattern is applied here.
 * The TransactionStatus enum encodes valid states;
 * TransactionService enforces legal transitions.
 *
 * SOLID – SRP: manages only financial movement of credits.
 */
@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;   // learner

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private User payee;   // teacher

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(length = 500)
    private String note;

    /** Timestamp when credits entered escrow */
    @Column
    private LocalDateTime escrowedAt;

    /** Timestamp when credits were released or refunded */
    @Column
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── State Transition Guards ──────────────────────────────
    public boolean canEscrow() {
        return this.status == TransactionStatus.PENDING;
    }

    public boolean canRelease() {
        return this.status == TransactionStatus.ESCROWED;
    }

    public boolean canRefund() {
        return this.status == TransactionStatus.ESCROWED
               || this.status == TransactionStatus.DISPUTED;
    }

    public boolean canDispute() {
        return this.status == TransactionStatus.ESCROWED;
    }

    public void transitionTo(TransactionStatus newStatus) {
        boolean legal = switch (newStatus) {
            case ESCROWED  -> canEscrow();
            case RELEASED  -> canRelease();
            case REFUNDED  -> canRefund();
            case DISPUTED  -> canDispute();
            default -> false;
        };
        if (!legal) {
            throw new IllegalStateException(
                "Cannot transition transaction from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
        if (newStatus == TransactionStatus.ESCROWED) {
            this.escrowedAt = LocalDateTime.now();
        }
        if (newStatus == TransactionStatus.RELEASED || newStatus == TransactionStatus.REFUNDED) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
}
