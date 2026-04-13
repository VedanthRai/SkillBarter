package com.skillbarter.enums;


public enum UserStatus {
    PENDING_VERIFICATION,   // Registered, email not confirmed
    ACTIVE,                 // Normal operating state
    SUSPENDED,              // Temporarily restricted by admin
    BANNED                  // Permanently restricted by admin
}
