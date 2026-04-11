package com.skillbarter.service;

import com.skillbarter.entity.Session;
import com.skillbarter.entity.Transaction;
import com.skillbarter.entity.User;
import com.skillbarter.enums.TransactionStatus;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.pattern.DomainEvents;
import com.skillbarter.repository.TransactionRepository;
import com.skillbarter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
/**
 * Transaction Service — Major Feature 3: Escrow & Payment.
 *
 * Implements the Escrow State Machine using Transaction.transitionTo().
 * All credit movements are atomic within a @Transactional boundary.
 *
 * State Transitions (State Diagram 1):
 *   PENDING → ESCROWED → RELEASED
 *                      → REFUNDED
 *                      → DISPUTED → RELEASED | REFUNDED
 *
 * SOLID – SRP: manages only credit movement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletService walletService;

    /**
     * Create a transaction and immediately move credits to escrow.
     * Called when a session is requested.
     */
    @Transactional
    public Transaction createAndEscrow(Session session) {
        User learner = session.getLearner();
        BigDecimal amount = session.getCreditAmount();

        // Deduct from learner's available balance, add to escrow balance
        learner.deductCredits(amount);
        userRepository.save(learner);

        Transaction tx = Transaction.builder()
                .payer(learner)
                .payee(session.getTeacher())
                .amount(amount)
                .session(session)
                .note("Escrow for session: " + session.getSkill().getName())
                .status(TransactionStatus.PENDING)
                .build();

        tx.transitionTo(TransactionStatus.ESCROWED);
        tx = transactionRepository.save(tx);

        walletService.recordEscrowHold(learner, session, amount);
        eventPublisher.publishEvent(new DomainEvents.PaymentEscrowedEvent(this, session));
        log.info("Escrowed {} credits for session {} (payer={})", amount, session.getId(), learner.getId());
        return tx;
    }

    /**
     * Release escrowed credits to teacher.
     * Called when both parties confirm session completion.
     */
    @Transactional
    public Transaction release(Session session) {
        Transaction tx = getTransactionForSession(session.getId());
        tx.transitionTo(TransactionStatus.RELEASED);

        User learner = session.getLearner();
        User teacher = session.getTeacher();

        // Remove from escrow balance, add to teacher's available balance
        learner.releaseEscrow(tx.getAmount());
        teacher.addCredits(tx.getAmount());

        userRepository.save(learner);
        userRepository.save(teacher);
        tx = transactionRepository.save(tx);

        walletService.recordEscrowRelease(teacher, session, tx.getAmount());
        eventPublisher.publishEvent(new DomainEvents.PaymentReleasedEvent(this, session));
        log.info("Released {} credits to teacher {} for session {}",
                tx.getAmount(), teacher.getId(), session.getId());
        return tx;
    }

    /**
     * Refund escrowed credits to learner.
     * Called on cancellation or dispute ruling for learner.
     */
    @Transactional
    public Transaction refund(Session session) {
        Transaction tx = getTransactionForSession(session.getId());

        // Only refund if credits are still in escrow
        if (tx.getStatus() == TransactionStatus.RELEASED) {
            throw new BusinessRuleException("Credits already released — cannot refund.");
        }
        if (tx.getStatus() == TransactionStatus.REFUNDED) {
            log.warn("Transaction {} already refunded, skipping.", tx.getId());
            return tx;
        }

        tx.transitionTo(TransactionStatus.REFUNDED);

        User learner = session.getLearner();
        learner.refundFromEscrow(tx.getAmount());
        userRepository.save(learner);
        tx = transactionRepository.save(tx);

        walletService.recordRefund(learner, session, tx.getAmount());
        eventPublisher.publishEvent(new DomainEvents.PaymentRefundedEvent(this, session));
        log.info("Refunded {} credits to learner {} for session {}",
                tx.getAmount(), learner.getId(), session.getId());
        return tx;
    }

    /**
     * Partial refund (50/50 split) for dispute resolution.
     */
    @Transactional
    public Transaction partialRefund(Session session) {
        Transaction tx = getTransactionForSession(session.getId());

        if (tx.getStatus() == TransactionStatus.RELEASED) {
            throw new BusinessRuleException("Credits already released — cannot refund.");
        }

        BigDecimal halfAmount = tx.getAmount().divide(BigDecimal.valueOf(2));

        User learner = session.getLearner();
        User teacher = session.getTeacher();

        // Return half to learner, give half to teacher
        learner.releaseEscrow(halfAmount);
        learner.addCredits(halfAmount);
        
        learner.releaseEscrow(halfAmount);
        teacher.addCredits(halfAmount);

        userRepository.save(learner);
        userRepository.save(teacher);

        tx.transitionTo(TransactionStatus.RELEASED);
        tx.setNote(tx.getNote() + " [50/50 SPLIT]");
        tx = transactionRepository.save(tx);

        walletService.recordPartialRefund(learner, teacher, session, halfAmount);
        log.info("Partial refund (50/50): {} to learner, {} to teacher for session {}",
                halfAmount, halfAmount, session.getId());
        return tx;
    }

    /**
     * Mark transaction as disputed (called when dispute is raised).
     */
    @Transactional
    public Transaction markDisputed(Session session) {
        Transaction tx = getTransactionForSession(session.getId());
        tx.transitionTo(TransactionStatus.DISPUTED);
        return transactionRepository.save(tx);
    }

    // ── Queries ──────────────────────────────────────────────────────────

    public List<Transaction> getTransactionsForUser(Long userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    public Optional<Transaction> findBySessionId(Long sessionId) {
        return transactionRepository.findBySessionId(sessionId);
    }

    public BigDecimal getTotalEarnings(Long userId) {
        BigDecimal earned = transactionRepository.sumReleasedEarningsForUser(userId);
        return earned != null ? earned : BigDecimal.ZERO;
    }

    public BigDecimal getTotalSpent(Long userId) {
        BigDecimal spent = transactionRepository.sumSpentCreditsForUser(userId);
        return spent != null ? spent : BigDecimal.ZERO;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Transaction getTransactionForSession(Long sessionId) {
        return transactionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessRuleException(
                        "No transaction found for session: " + sessionId));
    }
}
