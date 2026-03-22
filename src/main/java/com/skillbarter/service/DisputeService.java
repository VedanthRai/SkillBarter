package com.skillbarter.service;

import com.skillbarter.dto.DisputeRequestDto;
import com.skillbarter.entity.Dispute;
import com.skillbarter.entity.Session;
import com.skillbarter.entity.User;
import com.skillbarter.enums.DisputeStatus;
import com.skillbarter.enums.SessionStatus;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.pattern.DomainEvents;
import com.skillbarter.repository.DisputeRepository;
import com.skillbarter.repository.SessionRepository;
import com.skillbarter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dispute Service — Major Feature 4: Dispute Tribunal.
 *
 * Implements the Dispute State Machine (State Diagram 4):
 *   OPEN → UNDER_REVIEW → RESOLVED_TEACHER | RESOLVED_LEARNER → CLOSED
 *
 * SOLID – SRP: handles only dispute lifecycle.
 * SOLID – OCP: resolution strategies could be plugged in (future Strategy).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ── Raise Dispute (Learner or Teacher) ───────────────────────────────

    @Transactional
    public Dispute raiseDispute(Long sessionId, Long userId, DisputeRequestDto dto) {
        Session session = sessionService.getSession(sessionId);

        boolean isParticipant = session.getLearner().getId().equals(userId)
                             || session.getTeacher().getId().equals(userId);
        if (!isParticipant) {
            throw new BusinessRuleException("Only session participants can raise a dispute.");
        }
        if (disputeRepository.existsBySessionId(sessionId)) {
            throw new BusinessRuleException("A dispute already exists for this session.");
        }
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot dispute a cancelled session.");
        }

        // Move session and transaction to DISPUTED state
        session.setStatus(SessionStatus.DISPUTED);
        sessionRepository.save(session);

        User raisedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Dispute dispute = Dispute.builder()
                .session(session)
                .raisedBy(raisedBy)
                .description(dto.getDescription())
                .evidence(dto.getEvidence())
                .status(DisputeStatus.OPEN)
                .build();

        dispute = disputeRepository.save(dispute);

        // Mark the financial transaction as disputed (credits stay in escrow)
        transactionService.markDisputed(session);

        eventPublisher.publishEvent(new DomainEvents.DisputeOpenedEvent(this, dispute));
        log.info("Dispute {} raised for session {} by user {}", dispute.getId(), sessionId, userId);
        return dispute;
    }

    // ── Assign Verifier (Admin action) ────────────────────────────────────

    @Transactional
    public Dispute assignVerifier(Long disputeId, Long verifierId) {
        Dispute dispute = getDispute(disputeId);
        assertDisputeStatus(dispute, DisputeStatus.OPEN);

        User verifier = userRepository.findById(verifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Verifier not found: " + verifierId));

        dispute.setAssignedVerifier(verifier);
        dispute.setStatus(DisputeStatus.UNDER_REVIEW);
        dispute.setAssignedAt(LocalDateTime.now());

        log.info("Dispute {} assigned to verifier {}", disputeId, verifierId);
        return disputeRepository.save(dispute);
    }

    // ── Resolve Dispute (Verifier action) ─────────────────────────────────

    @Transactional
    public Dispute resolveInFavourOfTeacher(Long disputeId, Long verifierId, String resolution) {
        Dispute dispute = getAndValidateVerifier(disputeId, verifierId);

        dispute.setStatus(DisputeStatus.RESOLVED_TEACHER);
        dispute.setResolution(resolution);
        dispute.setResolvedAt(LocalDateTime.now());
        dispute = disputeRepository.save(dispute);

        // Release escrowed credits to teacher
        transactionService.release(dispute.getSession());
        dispute.getSession().setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(dispute.getSession());

        eventPublisher.publishEvent(new DomainEvents.DisputeResolvedEvent(this, dispute));
        log.info("Dispute {} resolved in TEACHER's favour by verifier {}", disputeId, verifierId);
        return dispute;
    }

    @Transactional
    public Dispute resolveInFavourOfLearner(Long disputeId, Long verifierId, String resolution) {
        Dispute dispute = getAndValidateVerifier(disputeId, verifierId);

        dispute.setStatus(DisputeStatus.RESOLVED_LEARNER);
        dispute.setResolution(resolution);
        dispute.setResolvedAt(LocalDateTime.now());
        dispute = disputeRepository.save(dispute);

        // Refund escrowed credits to learner
        transactionService.refund(dispute.getSession());
        dispute.getSession().setStatus(SessionStatus.CANCELLED);
        sessionRepository.save(dispute.getSession());

        eventPublisher.publishEvent(new DomainEvents.DisputeResolvedEvent(this, dispute));
        log.info("Dispute {} resolved in LEARNER's favour by verifier {}", disputeId, verifierId);
        return dispute;
    }

    // ── Queries ──────────────────────────────────────────────────────────

    public Dispute getDispute(Long id) {
        return disputeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found: " + id));
    }

    public List<Dispute> getOpenDisputes() {
        return disputeRepository.findByStatus(DisputeStatus.OPEN);
    }

    public List<Dispute> getDisputesForVerifier(Long verifierId) {
        return disputeRepository.findByAssignedVerifierId(verifierId);
    }

    public List<Dispute> getDisputesRaisedBy(Long userId) {
        return disputeRepository.findByRaisedById(userId);
    }

    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Dispute getAndValidateVerifier(Long disputeId, Long verifierId) {
        Dispute dispute = getDispute(disputeId);
        assertDisputeStatus(dispute, DisputeStatus.UNDER_REVIEW);
        if (dispute.getAssignedVerifier() == null ||
            !dispute.getAssignedVerifier().getId().equals(verifierId)) {
            throw new BusinessRuleException("You are not the assigned verifier for this dispute.");
        }
        return dispute;
    }

    private void assertDisputeStatus(Dispute dispute, DisputeStatus expected) {
        if (dispute.getStatus() != expected) {
            throw new BusinessRuleException(
                "Dispute must be in status " + expected + " but is " + dispute.getStatus());
        }
    }
}
