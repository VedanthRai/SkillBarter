package com.skillbarter.pattern;

import com.skillbarter.entity.Session;
import com.skillbarter.entity.User;
import org.springframework.context.ApplicationEvent;

/**
 * Observer Pattern (Behavioral) — Domain Event classes.
 *
 * Spring's ApplicationEvent/ApplicationEventPublisher mechanism
 * implements the Observer pattern natively.
 *
 * Publishers (Services) fire events; Listeners (NotificationService)
 * react without tight coupling.
 *
 * SOLID – OCP: new listeners can be added without touching publishers.
 * SOLID – DIP: publishers depend on ApplicationEventPublisher abstraction.
 */
public class DomainEvents {

    // ── Session Events ───────────────────────────────────────────────────

    public static class SessionRequestedEvent extends ApplicationEvent {
        private final Session session;
        public SessionRequestedEvent(Object source, Session session) {
            super(source);
            this.session = session;
        }
        public Session getSession() { return session; }
    }

    public static class SessionAcceptedEvent extends ApplicationEvent {
        private final Session session;
        public SessionAcceptedEvent(Object source, Session session) {
            super(source);
            this.session = session;
        }
        public Session getSession() { return session; }
    }

    public static class SessionCancelledEvent extends ApplicationEvent {
        private final Session session;
        private final User cancelledBy;
        public SessionCancelledEvent(Object source, Session session, User cancelledBy) {
            super(source);
            this.session = session;
            this.cancelledBy = cancelledBy;
        }
        public Session getSession() { return session; }
        public User getCancelledBy() { return cancelledBy; }
    }

    public static class SessionCompletedEvent extends ApplicationEvent {
        private final Session session;
        public SessionCompletedEvent(Object source, Session session) {
            super(source);
            this.session = session;
        }
        public Session getSession() { return session; }
    }

    // ── Transaction Events ────────────────────────────────────────────────

    public static class PaymentEscrowedEvent extends ApplicationEvent {
        private final Session session;
        public PaymentEscrowedEvent(Object source, Session session) {
            super(source);
            this.session = session;
        }
        public Session getSession() { return session; }
    }

    public static class PaymentReleasedEvent extends ApplicationEvent {
        private final Session session;
        public PaymentReleasedEvent(Object source, Session session) {
            super(source);
            this.session = session;
        }
        public Session getSession() { return session; }
    }

    public static class PaymentRefundedEvent extends ApplicationEvent {
        private final Session session;
        public PaymentRefundedEvent(Object source, Session session) {
            super(source);
            this.session = session;
        }
        public Session getSession() { return session; }
    }

    // ── Dispute Events ────────────────────────────────────────────────────

    public static class DisputeOpenedEvent extends ApplicationEvent {
        private final com.skillbarter.entity.Dispute dispute;
        public DisputeOpenedEvent(Object source, com.skillbarter.entity.Dispute dispute) {
            super(source);
            this.dispute = dispute;
        }
        public com.skillbarter.entity.Dispute getDispute() { return dispute; }
    }

    public static class DisputeResolvedEvent extends ApplicationEvent {
        private final com.skillbarter.entity.Dispute dispute;
        public DisputeResolvedEvent(Object source, com.skillbarter.entity.Dispute dispute) {
            super(source);
            this.dispute = dispute;
        }
        public com.skillbarter.entity.Dispute getDispute() { return dispute; }
    }

    // ── Badge Events ──────────────────────────────────────────────────────

    public static class BadgeAwardedEvent extends ApplicationEvent {
        private final com.skillbarter.entity.Badge badge;
        public BadgeAwardedEvent(Object source, com.skillbarter.entity.Badge badge) {
            super(source);
            this.badge = badge;
        }
        public com.skillbarter.entity.Badge getBadge() { return badge; }
    }

    // ── Endorsement Events ────────────────────────────────────────────────

    public static class EndorsementReceivedEvent extends ApplicationEvent {
        private final com.skillbarter.entity.SkillEndorsement endorsement;
        public EndorsementReceivedEvent(Object source, com.skillbarter.entity.SkillEndorsement endorsement) {
            super(source);
            this.endorsement = endorsement;
        }
        public com.skillbarter.entity.SkillEndorsement getEndorsement() { return endorsement; }
    }
}
