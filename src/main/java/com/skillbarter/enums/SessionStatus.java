package com.skillbarter.enums;

/**
 * Session lifecycle states (State Diagram 2).
 */
public enum SessionStatus {
    REQUESTED,   // Learner sent request
    ACCEPTED,    // Teacher confirmed
    REJECTED,    // Teacher declined request
    IN_PROGRESS, // Session underway (timer started)
    COMPLETED,   // Both parties confirmed end
    CANCELLED,   // Either party cancelled before start
    DISPUTED     // Conflict raised post-session
}
