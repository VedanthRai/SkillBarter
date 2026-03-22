package com.skillbarter.service;

import com.skillbarter.entity.Notification;
import com.skillbarter.entity.User;
import com.skillbarter.enums.NotificationType;
import com.skillbarter.pattern.DomainEvents;
import com.skillbarter.pattern.NotificationBuilder;
import com.skillbarter.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Notification Service — implements Observer Pattern via Spring Events.
 *
 * Listens for domain events and persists Notification records using
 * the NotificationBuilder (Builder Pattern).
 *
 * SOLID – SRP: handles only notification persistence and retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ── Observer Listeners (reacting to domain events) ───────────────────

    @Async
    @EventListener
    @Transactional
    public void onSessionRequested(DomainEvents.SessionRequestedEvent event) {
        var session = event.getSession();
        send(NotificationBuilder.create()
                .forUser(session.getTeacher())
                .ofType(NotificationType.SESSION_REQUEST)
                .withMessage(session.getLearner().getUsername() +
                             " requested a session for: " + session.getSkill().getName())
                .withActionUrl("/sessions/" + session.getId())
                .build());
    }

    @Async
    @EventListener
    @Transactional
    public void onSessionAccepted(DomainEvents.SessionAcceptedEvent event) {
        var session = event.getSession();
        send(NotificationBuilder.create()
                .forUser(session.getLearner())
                .ofType(NotificationType.SESSION_ACCEPTED)
                .withMessage(session.getTeacher().getUsername() +
                             " accepted your session request for: " + session.getSkill().getName())
                .withActionUrl("/sessions/" + session.getId())
                .build());
    }

    @Async
    @EventListener
    @Transactional
    public void onSessionCancelled(DomainEvents.SessionCancelledEvent event) {
        var session = event.getSession();
        User otherParty = event.getCancelledBy().getId().equals(session.getLearner().getId())
                ? session.getTeacher() : session.getLearner();
        send(NotificationBuilder.create()
                .forUser(otherParty)
                .ofType(NotificationType.SESSION_CANCELLED)
                .withMessage("Session for '" + session.getSkill().getName() + "' was cancelled.")
                .withActionUrl("/sessions")
                .build());
    }

    @Async
    @EventListener
    @Transactional
    public void onSessionCompleted(DomainEvents.SessionCompletedEvent event) {
        var session = event.getSession();
        // notify teacher their credits are incoming
        send(NotificationBuilder.create()
                .forUser(session.getTeacher())
                .ofType(NotificationType.PAYMENT_RELEASED)
                .withMessage(String.format("Session completed! %.2f credits released to your balance.",
                             session.getCreditAmount().doubleValue()))
                .withActionUrl("/wallet")
                .build());
        // prompt learner to leave a review
        send(NotificationBuilder.create()
                .forUser(session.getLearner())
                .ofType(NotificationType.SESSION_ACCEPTED)
                .withMessage("Your session is complete! Please leave a review for " +
                             session.getTeacher().getUsername())
                .withActionUrl("/sessions/" + session.getId() + "/review")
                .build());
    }

    @Async
    @EventListener
    @Transactional
    public void onPaymentEscrowed(DomainEvents.PaymentEscrowedEvent event) {
        var session = event.getSession();
        send(NotificationBuilder.create()
                .forUser(session.getLearner())
                .ofType(NotificationType.PAYMENT_ESCROWED)
                .withMessage(String.format("%.2f credits held in escrow for your upcoming session.",
                             session.getCreditAmount().doubleValue()))
                .withActionUrl("/wallet")
                .build());
    }

    @Async
    @EventListener
    @Transactional
    public void onPaymentRefunded(DomainEvents.PaymentRefundedEvent event) {
        var session = event.getSession();
        send(NotificationBuilder.create()
                .forUser(session.getLearner())
                .ofType(NotificationType.PAYMENT_REFUNDED)
                .withMessage(String.format("%.2f credits refunded to your balance.",
                             session.getCreditAmount().doubleValue()))
                .withActionUrl("/wallet")
                .build());
    }

    @Async
    @EventListener
    @Transactional
    public void onDisputeOpened(DomainEvents.DisputeOpenedEvent event) {
        var dispute = event.getDispute();
        send(NotificationBuilder.create()
                .forUser(dispute.getSession().getTeacher())
                .ofType(NotificationType.DISPUTE_OPENED)
                .withMessage("A dispute has been raised for your session. A verifier will review it.")
                .withActionUrl("/disputes/" + dispute.getId())
                .build());
    }

    @Async
    @EventListener
    @Transactional
    public void onDisputeResolved(DomainEvents.DisputeResolvedEvent event) {
        var dispute = event.getDispute();
        var session = dispute.getSession();
        String outcome = dispute.getStatus().name().contains("TEACHER") ? "teacher" : "learner";
        send(NotificationBuilder.create()
                .forUser(session.getLearner())
                .ofType(NotificationType.DISPUTE_RESOLVED)
                .withMessage("Dispute resolved in favour of " + outcome + ". See ruling details.")
                .withActionUrl("/disputes/" + dispute.getId())
                .build());
        send(NotificationBuilder.create()
                .forUser(session.getTeacher())
                .ofType(NotificationType.DISPUTE_RESOLVED)
                .withMessage("Dispute resolved in favour of " + outcome + ". See ruling details.")
                .withActionUrl("/disputes/" + dispute.getId())
                .build());
    }

    @Async
    @EventListener
    @Transactional
    public void onBadgeAwarded(DomainEvents.BadgeAwardedEvent event) {
        var badge = event.getBadge();
        send(NotificationBuilder.create()
                .forUser(badge.getUser())
                .ofType(NotificationType.BADGE_AWARDED)
                .withMessage("🏅 You earned a new badge: " + badge.getBadgeName())
                .withActionUrl("/profile")
                .build());
    }

    // ── Direct notification ───────────────────────────────────────────────

    @Transactional
    public void sendAdminMessage(User recipient, String message) {
        send(NotificationBuilder.create()
                .forUser(recipient)
                .ofType(NotificationType.ADMIN_MESSAGE)
                .withMessage(message)
                .build());
    }

    // ── Queries ───────────────────────────────────────────────────────────

    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    @Transactional
    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void send(Notification notification) {
        notificationRepository.save(notification);
        log.debug("Notification sent to {}: {}", notification.getRecipient().getUsername(),
                  notification.getMessage());
    }
}
