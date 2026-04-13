package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/referrals")
@RequiredArgsConstructor
public class ReferralController {
// Manages referral system (invite users and track rewards)
    private final ReferralService referralService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String viewReferrals(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        var code = referralService.getOrCreateReferralCode(userId);
        var referrals = referralService.getReferralStats(userId);
        model.addAttribute("referralCode", code);
        model.addAttribute("referrals", referrals);
        return "referrals/dashboard";
    }
}
