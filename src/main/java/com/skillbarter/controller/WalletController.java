package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.TransactionService;
import com.skillbarter.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Wallet Controller — dedicated credit ledger view.
 *
 * Provides a full transaction history for the current user.
 * SOLID – SRP: only handles wallet/ledger display.
 */
@Controller
@RequestMapping("/wallet")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String wallet(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        var user = securityUtils.getCurrentUser();

        model.addAttribute("user", user);
        model.addAttribute("ledger", walletService.getLedgerForUser(userId));
        model.addAttribute("totalEarned", walletService.getTotalEarned(userId));
        model.addAttribute("totalSpent", walletService.getTotalSpent(userId));
        model.addAttribute("escrowBalance", user.getEscrowBalance());
        model.addAttribute("availableBalance", user.getCreditBalance());
        return "wallet/index";
    }
}
