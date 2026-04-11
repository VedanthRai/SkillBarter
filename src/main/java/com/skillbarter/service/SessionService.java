package com.skillbarter.service;

import com.skillbarter.dto.SessionRequestDto;
import com.skillbarter.entity.*;
import com.skillbarter.enums.SessionStatus;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.pattern.DomainEvents;
import com.skillbarter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Session Service — Major Feature 2 & 3.
 *
 * Manages the full session lifecycle:
 *   Request → Accept → Complete → (Review/Dispute)
 *
 * Integrates with TransactionService for escrow.
 * Fires domain events (Observer Pattern).
 *
 * SOLID – SRP: manages session state transitions only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMessageRepository sessionMessageRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final UserService userService;
    private final PdfReceiptService pdfReceiptService;
    private final MeetingLinkService meetingLinkService;
    private final ApplicationEventPublisher eventPublisher;

    // ── Book Session (Learner action) ────────────────────────────────────

    @Transactional
    public Session requestSession(Long learnerId, SessionRequestDto dto) {
        User learner = userService.findById(learnerId);
        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + dto.getSkillId()));

        User teacher = skill.getUser();

        if (teacher.getId().equals(learnerId)) {
            throw new BusinessRuleException("You cannot book your own skill.");
        }
        if (!teacher.isActive()) {
            throw new BusinessRuleException("Teacher account is not active.");
        }

        // Calculate credit cost
        BigDecimal cost = skill.getHourlyRate()
                .multiply(BigDecimal.valueOf(dto.getDurationMinutes() / 60.0));

        if (!learner.hasEnoughCredits(cost)) {
            throw new BusinessRuleException(
                    "Insufficient credits. Required: " + cost + ", Balance: " + learner.getCreditBalance());
        }

        Session session = Session.builder()
                .learner(learner)
                .teacher(teacher)
                .skill(skill)
                .scheduledAt(dto.getScheduledAt())
                .durationMinutes(dto.getDurationMinutes())
                .creditAmount(cost)
                .learnerNotes(dto.getNotes())
                .status(SessionStatus.REQUESTED)
                .build();

        session = sessionRepository.save(session);

        // Put credits into escrow immediately on request
        transactionService.createAndEscrow(session);

        eventPublisher.publishEvent(new DomainEvents.SessionRequestedEvent(this, session));
        log.info("Session {} requested by {} for skill '{}'", session.getId(), learnerId, skill.getName());
        return session;
    }

    // ── Accept Session (Teacher action) ──────────────────────────────────

    @Transactional
    public Session acceptSession(Long sessionId, Long teacherId) {
        Session session = getSession(sessionId);
        validateTeacher(session, teacherId);
        assertStatus(session, SessionStatus.REQUESTED);

        session.setStatus(SessionStatus.ACCEPTED);
        
        // Auto-generate meeting link if not already set
        if (session.getMeetingLink() == null || session.getMeetingLink().isEmpty()) {
            String autoLink = meetingLinkService.generateMeetingLink(session);
            session.setMeetingLink(autoLink);
            log.info("Auto-generated meeting link for session {}: {}", sessionId, autoLink);
        }
        
        session = sessionRepository.save(session);

        eventPublisher.publishEvent(new DomainEvents.SessionAcceptedEvent(this, session));
        log.info("Session {} accepted by teacher {}", sessionId, teacherId);
        return session;
    }

    // ── Cancel Session ────────────────────────────────────────────────────

    @Transactional
    public Session cancelSession(Long sessionId, Long userId) {
        Session session = getSession(sessionId);
        User canceller = userService.findById(userId);

        boolean isParticipant = session.getLearner().getId().equals(userId)
                              || session.getTeacher().getId().equals(userId);
        if (!isParticipant) throw new BusinessRuleException("You are not a participant in this session.");

        if (session.getStatus() == SessionStatus.IN_PROGRESS ||
            session.getStatus() == SessionStatus.COMPLETED) {
            throw new BusinessRuleException("Cannot cancel a session that is in progress or completed.");
        }

        session.setStatus(SessionStatus.CANCELLED);
        session = sessionRepository.save(session);

        // Refund credits from escrow
        transactionService.refund(session);

        eventPublisher.publishEvent(new DomainEvents.SessionCancelledEvent(this, session, canceller));
        return session;
    }

    // ── Start Session ─────────────────────────────────────────────────────

    @Transactional
    public Session startSession(Long sessionId, Long teacherId) {
        Session session = getSession(sessionId);
        validateTeacher(session, teacherId);
        assertStatus(session, SessionStatus.ACCEPTED);

        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setStartedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // ── Confirm Completion ────────────────────────────────────────────────

    @Transactional
    public Session confirmCompletion(Long sessionId, Long userId) {
        Session session = getSession(sessionId);
        assertStatus(session, SessionStatus.IN_PROGRESS);

        if (session.getLearner().getId().equals(userId)) {
            session.setLearnerConfirmed(true);
        } else if (session.getTeacher().getId().equals(userId)) {
            session.setTeacherConfirmed(true);
        } else {
            throw new BusinessRuleException("You are not a participant in this session.");
        }

        if (session.isBothConfirmed()) {
            completeSession(session);
        } else {
            sessionRepository.save(session);
        }
        return session;
    }

    private void completeSession(Session session) {
        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        session = sessionRepository.save(session);

        // Release escrowed credits to teacher
        transactionService.release(session);

        // Update skill stats
        Skill skill = session.getSkill();
        skill.setTotalSessions(skill.getTotalSessions() + 1);
        skillRepository.save(skill);

        // Update gamification
        User teacher = session.getTeacher();
        User learner = session.getLearner();
        teacher.incrementStreak();
        learner.incrementStreak();
        teacher.setReputationScore(teacher.getReputationScore() + 2);
        userRepository.save(teacher);
        userRepository.save(learner);

        userService.checkAndAwardStreakBadge(teacher);
        userService.checkAndAwardStreakBadge(learner);

        // Generate PDF receipt asynchronously
        try {
            String receiptPath = pdfReceiptService.generateReceipt(session);
            session.setReceiptPath(receiptPath);
            sessionRepository.save(session);
        } catch (Exception e) {
            log.warn("PDF generation failed for session {}: {}", session.getId(), e.getMessage());
        }

        eventPublisher.publishEvent(new DomainEvents.SessionCompletedEvent(this, session));
        log.info("Session {} completed. Credits released to teacher {}", session.getId(),
                session.getTeacher().getId());
    }

    // ── Scheduled: auto-expire sessions that passed without acceptance ────

    @Scheduled(fixedDelay = 3_600_000) // every hour
    @Transactional
    public void autoExpireOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(48);
        List<Session> expired = sessionRepository.findExpiredAcceptedSessions(cutoff);
        for (Session s : expired) {
            log.info("Auto-cancelling stale session {}", s.getId());
            s.setStatus(SessionStatus.CANCELLED);
            sessionRepository.save(s);
            transactionService.refund(s);
        }
    }

    // ── Queries ──────────────────────────────────────────────────────────

    public Session getSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + id));
    }

    public List<Session> getSessionsForUser(Long userId) {
        return sessionRepository.findAllByUserId(userId);
    }

    public List<Session> getSessionsByStatus(SessionStatus status) {
        return sessionRepository.findByStatus(status);
    }

    public List<SessionMessage> getMessagesForSession(Long sessionId, Long userId) {
        Session session = getSession(sessionId);
        validateParticipant(session, userId);
        return sessionMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public Session updateMeetingLink(Long sessionId, Long userId, String meetingLink) {
        Session session = getSession(sessionId);
        validateParticipant(session, userId);

        String trimmed = meetingLink == null ? "" : meetingLink.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessRuleException("Meeting link cannot be empty.");
        }
        if (!(trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
            throw new BusinessRuleException("Meeting link must start with http:// or https://");
        }

        session.setMeetingLink(trimmed);
        return sessionRepository.save(session);
    }

    @Transactional
    public SessionMessage addMessage(Long sessionId, Long userId, String messageText) {
        Session session = getSession(sessionId);
        validateParticipant(session, userId);

        String trimmed = messageText == null ? "" : messageText.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessRuleException("Message cannot be empty.");
        }

        SessionMessage message = SessionMessage.builder()
                .session(session)
                .sender(userService.findById(userId))
                .message(trimmed)
                .build();
        return sessionMessageRepository.save(message);
    }

    // ── Guards ────────────────────────────────────────────────────────────

    private void validateTeacher(Session session, Long teacherId) {
        if (!session.getTeacher().getId().equals(teacherId)) {
            throw new BusinessRuleException("Only the teacher can perform this action.");
        }
    }

    private void validateParticipant(Session session, Long userId) {
        boolean isParticipant = session.getLearner().getId().equals(userId)
                || session.getTeacher().getId().equals(userId);
        if (!isParticipant) {
            throw new BusinessRuleException("You are not a participant in this session.");
        }
    }

    private void assertStatus(Session session, SessionStatus expected) {
        if (session.getStatus() != expected) {
            throw new BusinessRuleException(
                "Session must be in status " + expected + " but is " + session.getStatus());
        }
    }
}
