package com.skillbarter.pattern;

import com.skillbarter.entity.Notification;
import com.skillbarter.entity.User;
import com.skillbarter.enums.NotificationType;

/**
 * Builder Pattern (Creational) — Notification construction.
 *
 * Constructs complex Notification objects step-by-step,
 * ensuring all required fields are set before saving.
 *
 * SOLID – SRP: handles only notification assembly.
 * Design Pattern: Builder Pattern.
 *
 * Usage:
 *   Notification n = NotificationBuilder.create()
 *       .for(user)
 *       .ofType(SESSION_ACCEPTED)
 *       .withMessage("Your session has been accepted!")
 *       .withActionUrl("/sessions/42")
 *       .build();
 */
public class NotificationBuilder {

    private User recipient;
    private NotificationType type;
    private String message;
    private String actionUrl;

    private NotificationBuilder() {}

    public static NotificationBuilder create() {
        return new NotificationBuilder();
    }

    public NotificationBuilder forUser(User recipient) {
        this.recipient = recipient;
        return this;
    }

    public NotificationBuilder ofType(NotificationType type) {
        this.type = type;
        return this;
    }

    public NotificationBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public NotificationBuilder withActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
        return this;
    }

    public Notification build() {
        if (recipient == null) throw new IllegalStateException("Notification recipient is required");
        if (type == null)      throw new IllegalStateException("Notification type is required");
        if (message == null || message.isBlank())
                               throw new IllegalStateException("Notification message is required");

        return Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .actionUrl(actionUrl)
                .isRead(false)
                .build();
    }
}
