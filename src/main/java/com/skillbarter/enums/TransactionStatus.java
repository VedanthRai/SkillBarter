package com.skillbarter.enums;

/**
 * Time-credit transaction lifecycle states.
 * Central to the Escrow State Machine (State Diagram 1).
 *
 * State transitions:
 * PENDING → ESCROWED → RELEASED  (normal flow)
 *                    → REFUNDED  (cancellation / dispute won by learner)
 *                    → DISPUTED  (conflict raised)
 * DISPUTED → RELEASED | REFUNDED (tribunal decision)
 */
public enum TransactionStatus {
    PENDING,    // Credits deducted from learner, not yet held
    ESCROWED,   // Credits locked; session not yet confirmed
    RELEASED,   // Credits transferred to teacher after confirmation
    REFUNDED,   // Credits returned to learner (cancel / dispute)
    DISPUTED    // Under tribunal review
}
