package com.skillbarter.enums;

/**
 * User account lifecycle states.
 * Used in the User Account State Machine (State Diagram 3).
 */
public enum UserStatus {
    PENDING_VERIFICATION,   // Registered, email not confirmed
    ACTIVE,                 // Normal operating state
    SUSPENDED,              // Temporarily restricted by admin
    BANNED                  // Permanently restricted by admin
}
