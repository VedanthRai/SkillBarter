package com.skillbarter.service;

import com.skillbarter.entity.CreditTransaction;
import com.skillbarter.entity.ReferralCode;
import com.skillbarter.entity.User;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.repository.CreditTransactionRepository;
import com.skillbarter.repository.ReferralCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
// Manages referral logic and reward distribution
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private final ReferralCodeRepository referralCodeRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserService userService;

    @Value("${app.referral.bonus.credits:1}")
    private int referralBonusInt;

    private BigDecimal getReferralBonus() {
        return BigDecimal.valueOf(referralBonusInt);
    }

    @Transactional
    public ReferralCode generateReferralCode(Long userId) {
        User user = userService.findById(userId);
        
        return referralCodeRepository.findByReferrerId(userId)
                .orElseGet(() -> {
                    String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    ReferralCode referralCode = ReferralCode.builder()
                            .referrer(user)
                            .code(code)
                            .build();
                    return referralCodeRepository.save(referralCode);
                });
    }

    @Transactional
    public void applyReferralCode(Long newUserId, String code) {
        ReferralCode referralCode = referralCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid referral code"));

        User referrer = referralCode.getReferrer();
        User newUser = userService.findById(newUserId);

        if (referrer.getId().equals(newUserId)) {
            throw new IllegalArgumentException("Cannot use your own referral code");
        }

        // Award bonus to both
        BigDecimal bonus = getReferralBonus();
        
        referrer.setCreditBalance(referrer.getCreditBalance().add(bonus));
        newUser.setCreditBalance(newUser.getCreditBalance().add(bonus));

        referralCode.setUsedCount(referralCode.getUsedCount() + 1);
        referralCodeRepository.save(referralCode);

        // Record transactions
        creditTransactionRepository.save(CreditTransaction.builder()
                .user(referrer)
                .type(CreditTransaction.CreditTxType.ADMIN_GRANT)
                .amount(bonus)
                .balanceAfter(referrer.getCreditBalance())
                .description("Referral bonus for inviting " + newUser.getUsername())
                .build());

        creditTransactionRepository.save(CreditTransaction.builder()
                .user(newUser)
                .type(CreditTransaction.CreditTxType.SIGNUP_BONUS)
                .amount(bonus)
                .balanceAfter(newUser.getCreditBalance())
                .description("Referral bonus from " + referrer.getUsername())
                .build());

        log.info("Referral bonus awarded: {} invited {}", referrer.getId(), newUserId);
    }

    @Transactional
    public void processReferral(String code, Long newUserId) {
        try {
            applyReferralCode(newUserId, code);
        } catch (Exception e) {
            log.warn("Failed to process referral code {} for user {}: {}", code, newUserId, e.getMessage());
        }
    }

    public ReferralCode getOrCreateReferralCode(Long userId) {
        return referralCodeRepository.findByReferrerId(userId)
                .orElseGet(() -> generateReferralCode(userId));
    }

    public ReferralStats getReferralStats(Long userId) {
        ReferralCode code = getOrCreateReferralCode(userId);
        return new ReferralStats(code.getCode(), code.getUsedCount());
    }

    public static class ReferralStats {
        public final String code;
        public final Integer usedCount;
        
        public ReferralStats(String code, Integer usedCount) {
            this.code = code;
            this.usedCount = usedCount;
        }
        
        public String getCode() { return code; }
        public Integer getUsedCount() { return usedCount; }
    }

    public ReferralCode getReferralCodeForUser(Long userId) {
        return referralCodeRepository.findByReferrerId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No referral code found"));
    }
}
