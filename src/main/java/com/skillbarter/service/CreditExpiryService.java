package com.skillbarter.service;

import com.skillbarter.entity.CreditTransaction;
import com.skillbarter.entity.User;
import com.skillbarter.repository.CreditTransactionRepository;
import com.skillbarter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to handle credit expiry for inactive users.
 * 
 * Credits expire if unused for 6 months to encourage active participation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditExpiryService {

    private final UserRepository userRepo;
    private final CreditTransactionRepository creditTxRepo;
    private final NotificationService notificationService;

    private static final int INACTIVITY_MONTHS = 6;
    private static final int WARNING_DAYS_BEFORE = 7;

    /**
     * Run daily at 2 AM to check for expired credits
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireInactiveCredits() {
        log.info("Starting credit expiry check...");
        
        LocalDateTime expiryThreshold = LocalDateTime.now().minusMonths(INACTIVITY_MONTHS);
        List<User> inactiveUsers = userRepo.findUsersInactiveSince(expiryThreshold);
        
        for (User user : inactiveUsers) {
            if (user.getCreditBalance().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal expiredAmount = user.getCreditBalance();
                user.setCreditBalance(BigDecimal.ZERO);
                userRepo.save(user);
                
                // Record expiry transaction
                creditTxRepo.save(CreditTransaction.builder()
                        .user(user)
                        .type(CreditTransaction.CreditTxType.CREDIT_EXPIRY)
                        .amount(expiredAmount.negate())
                        .balanceAfter(BigDecimal.ZERO)
                        .description("Credits expired due to 6 months inactivity")
                        .build());
                
                // Notify user
                notificationService.notifyCreditExpiry(user, expiredAmount);
                
                log.info("Expired {} credits for inactive user: {}", expiredAmount, user.getUsername());
            }
        }
        
        log.info("Credit expiry check completed. Processed {} users", inactiveUsers.size());
    }

    /**
     * Run daily at 9 AM to warn users about upcoming expiry
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void warnUpcomingExpiry() {
        log.info("Checking for users with upcoming credit expiry...");
        
        LocalDateTime warningThreshold = LocalDateTime.now()
                .minusMonths(INACTIVITY_MONTHS)
                .plusDays(WARNING_DAYS_BEFORE);
        
        List<User> usersNearExpiry = userRepo.findUsersInactiveSince(warningThreshold);
        
        for (User user : usersNearExpiry) {
            if (user.getCreditBalance().compareTo(BigDecimal.ZERO) > 0) {
                notificationService.notifyUpcomingCreditExpiry(user, WARNING_DAYS_BEFORE);
                log.info("Sent expiry warning to user: {}", user.getUsername());
            }
        }
        
        log.info("Expiry warning check completed. Warned {} users", usersNearExpiry.size());
    }
}
