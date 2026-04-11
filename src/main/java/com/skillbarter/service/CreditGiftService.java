package com.skillbarter.service;

import com.skillbarter.entity.CreditGift;
import com.skillbarter.entity.CreditTransaction;
import com.skillbarter.entity.User;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.repository.CreditGiftRepository;
import com.skillbarter.repository.CreditTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditGiftService {

    private final CreditGiftRepository creditGiftRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserService userService;

    @Transactional
    public CreditGift giftCredits(Long senderId, Long recipientId, BigDecimal amount, String message) {
        if (senderId.equals(recipientId)) {
            throw new BusinessRuleException("Cannot gift credits to yourself.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Gift amount must be positive.");
        }

        User sender = userService.findById(senderId);
        User recipient = userService.findById(recipientId);

        if (!sender.hasEnoughCredits(amount)) {
            throw new BusinessRuleException("Insufficient credits to gift.");
        }

        // Deduct from sender
        sender.setCreditBalance(sender.getCreditBalance().subtract(amount));
        
        // Add to recipient
        recipient.setCreditBalance(recipient.getCreditBalance().add(amount));

        CreditGift gift = CreditGift.builder()
                .sender(sender)
                .recipient(recipient)
                .amount(amount)
                .message(message)
                .build();
        gift = creditGiftRepository.save(gift);

        // Record ledger entries
        creditTransactionRepository.save(CreditTransaction.builder()
                .user(sender)
                .type(CreditTransaction.CreditTxType.ADMIN_GRANT)
                .amount(amount.negate())
                .balanceAfter(sender.getCreditBalance())
                .description("Gift sent to " + recipient.getUsername())
                .build());

        creditTransactionRepository.save(CreditTransaction.builder()
                .user(recipient)
                .type(CreditTransaction.CreditTxType.ADMIN_GRANT)
                .amount(amount)
                .balanceAfter(recipient.getCreditBalance())
                .description("Gift received from " + sender.getUsername())
                .build());

        log.info("User {} gifted {} credits to {}", senderId, amount, recipientId);
        return gift;
    }

    public List<CreditGift> getGiftsForUser(Long userId) {
        return creditGiftRepository.findBySenderIdOrRecipientIdOrderByCreatedAtDesc(userId, userId);
    }

    @Transactional
    public CreditGift sendGift(Long senderId, com.skillbarter.dto.CreditGiftDto dto) {
        return giftCredits(senderId, dto.getRecipientId(), dto.getAmount(), dto.getMessage());
    }

    public List<CreditGift> getGiftHistory(Long userId) {
        return getGiftsForUser(userId);
    }
}
