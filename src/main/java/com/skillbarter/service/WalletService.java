package com.skillbarter.service;

import com.skillbarter.entity.CreditTransaction;
import com.skillbarter.entity.Session;
import com.skillbarter.entity.User;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.repository.CreditTransactionRepository;
import com.skillbarter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wallet Service — manages the credit ledger (CreditTransaction).
 *
 * Every credit movement is recorded here for full auditability.
 * The Wallet page reads from this ledger.
 *
 * SOLID – SRP: only manages credit ledger entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final CreditTransactionRepository creditTxRepo;
    private final UserRepository userRepository;

    // ── Record ledger entries ─────────────────────────────────────────────

    @Transactional
    public void recordSignupBonus(User user, BigDecimal amount) {
        save(CreditTransaction.builder()
                .user(user)
                .type(CreditTransaction.CreditTxType.SIGNUP_BONUS)
                .amount(amount)
                .balanceAfter(user.getCreditBalance())
                .description("Welcome bonus credits")
                .build());
    }

    @Transactional
    public void recordEscrowHold(User user, Session session, BigDecimal amount) {
        save(CreditTransaction.builder()
                .user(user)
                .type(CreditTransaction.CreditTxType.ESCROW_HOLD)
                .amount(amount.negate())
                .balanceAfter(user.getCreditBalance())
                .description("Escrow hold for session: " + session.getSkill().getName())
                .session(session)
                .build());
    }

    @Transactional
    public void recordEscrowRelease(User teacher, Session session, BigDecimal amount) {
        save(CreditTransaction.builder()
                .user(teacher)
                .type(CreditTransaction.CreditTxType.SESSION_EARNED)
                .amount(amount)
                .balanceAfter(teacher.getCreditBalance())
                .description("Credits earned for teaching: " + session.getSkill().getName())
                .session(session)
                .build());
    }

    @Transactional
    public void recordRefund(User learner, Session session, BigDecimal amount) {
        save(CreditTransaction.builder()
                .user(learner)
                .type(CreditTransaction.CreditTxType.ESCROW_REFUND)
                .amount(amount)
                .balanceAfter(learner.getCreditBalance())
                .description("Refund for cancelled/disputed session: " + session.getSkill().getName())
                .session(session)
                .build());
    }

    @Transactional
    public void recordPartialRefund(User learner, User teacher, Session session, BigDecimal halfAmount) {
        recordPartialRefund(learner, teacher, session, halfAmount, halfAmount);
    }

    @Transactional
    public void recordPartialRefund(User learner, User teacher, Session session, BigDecimal learnerAmount, BigDecimal teacherAmount) {
        // Record partial refund to learner
        save(CreditTransaction.builder()
                .user(learner)
                .type(CreditTransaction.CreditTxType.ESCROW_REFUND)
                .amount(learnerAmount)
                .balanceAfter(learner.getCreditBalance())
                .description("Partial refund (50%) for disputed session: " + session.getSkill().getName())
                .session(session)
                .build());
        
        // Record partial payment to teacher
        save(CreditTransaction.builder()
                .user(teacher)
                .type(CreditTransaction.CreditTxType.SESSION_EARNED)
                .amount(teacherAmount)
                .balanceAfter(teacher.getCreditBalance())
                .description("Partial payment (50%) for disputed session: " + session.getSkill().getName())
                .session(session)
                .build());
    }

    @Transactional
    public void recordGift(User sender, User recipient, BigDecimal amount, String message) {
        // Deduct from sender
        save(CreditTransaction.builder()
                .user(sender)
                .type(CreditTransaction.CreditTxType.GIFT_SENT)
                .amount(amount.negate())
                .balanceAfter(sender.getCreditBalance())
                .description("Gift sent to " + recipient.getUsername() + ": " + message)
                .build());
        
        // Add to recipient
        save(CreditTransaction.builder()
                .user(recipient)
                .type(CreditTransaction.CreditTxType.GIFT_RECEIVED)
                .amount(amount)
                .balanceAfter(recipient.getCreditBalance())
                .description("Gift received from " + sender.getUsername() + ": " + message)
                .build());
    }

    @Transactional
    public void recordReferralBonus(User referrer, User newUser) {
        // Bonus to referrer
        save(CreditTransaction.builder()
                .user(referrer)
                .type(CreditTransaction.CreditTxType.REFERRAL_BONUS)
                .amount(BigDecimal.ONE)
                .balanceAfter(referrer.getCreditBalance())
                .description("Referral bonus for inviting " + newUser.getUsername())
                .build());
        
        // Bonus to new user
        save(CreditTransaction.builder()
                .user(newUser)
                .type(CreditTransaction.CreditTxType.REFERRAL_BONUS)
                .amount(BigDecimal.ONE)
                .balanceAfter(newUser.getCreditBalance())
                .description("Referral bonus for joining via " + referrer.getUsername())
                .build());
    }

    /**
     * Admin grants credits to a user.
     * SOLID – OCP: new grant types can be added without changing this method.
     */
    @Transactional
    public void adminGrantCredits(User admin, User recipient, BigDecimal amount, String reason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Grant amount must be positive.");
        }
        recipient.addCredits(amount);
        userRepository.save(recipient);

        save(CreditTransaction.builder()
                .user(recipient)
                .type(CreditTransaction.CreditTxType.ADMIN_GRANT)
                .amount(amount)
                .balanceAfter(recipient.getCreditBalance())
                .description("Admin grant by " + admin.getUsername() + ": " + reason)
                .build());

        log.info("Admin {} granted {} credits to user {} ({})", admin.getUsername(),
                amount, recipient.getUsername(), reason);
    }

    // ── Queries ───────────────────────────────────────────────────────────

    public List<CreditTransaction> getLedgerForUser(Long userId) {
        return creditTxRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public BigDecimal getTotalEarned(Long userId) {
        return creditTxRepo.sumCreditsEarned(userId);
    }

    public BigDecimal getTotalSpent(Long userId) {
        BigDecimal spent = creditTxRepo.sumCreditsSpent(userId);
        return spent.abs();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void save(CreditTransaction tx) {
        creditTxRepo.save(tx);
    }
}
