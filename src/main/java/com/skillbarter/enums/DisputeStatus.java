package com.skillbarter.enums;
// Defines dispute lifecycle states
/**
 * Dispute lifecycle states (State Diagram 4).
 */
public enum DisputeStatus {
    OPEN,           // Raised, awaiting verifier assignment
    UNDER_REVIEW,   // Verifier assigned, reviewing evidence
    RESOLVED_TEACHER, // Tribunal ruled in teacher's favour
    RESOLVED_LEARNER, // Tribunal ruled in learner's favour
    CLOSED          // Acknowledged / withdrawn
}
