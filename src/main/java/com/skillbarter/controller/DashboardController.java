package com.skillbarter.controller;

import com.skillbarter.entity.User;
import com.skillbarter.enums.SessionStatus;
import com.skillbarter.pattern.UserProfileDecorator;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Dashboard Controller — user home page with at-a-glance stats.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final SecurityUtils securityUtils;
    private final UserService userService;
    private final SessionService sessionService;
    private final NotificationService notificationService;
    private final TransactionService transactionService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String dashboard(Model model) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            log.warn("Authenticated principal has no matching User row — forcing re-login");
            return "redirect:/auth/login?error=true";
        }
        Long userId = currentUser.getId();

        model.addAttribute("user", currentUser);
        UserProfileDecorator profile;
        try {
            profile = userService.buildProfileDecorator(userId);
        } catch (Exception ex) {
            log.error("Failed to build dashboard profile for user {}", userId, ex);
            profile = new UserProfileDecorator(currentUser, java.util.List.of(), null, 0L);
        }
        model.addAttribute("profile", profile);
        model.addAttribute("trustLabel", safeText(profile.getTrustLabel(), "Member"));
        model.addAttribute("streakDisplay", safeText(profile.getStreakDisplay(), "Start your streak!"));
        model.addAttribute("formattedRating", safeText(profile.getFormattedRating(), "No ratings yet"));
        model.addAttribute("badgeList", profile.getBadges() == null ? java.util.List.of() : profile.getBadges());

        // Session summaries
        var allSessions = sessionService.getSessionsForUser(userId);
        model.addAttribute("upcomingSessions", allSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.ACCEPTED
                          || s.getStatus() == SessionStatus.IN_PROGRESS)
                .limit(5).toList());
        model.addAttribute("pendingSessions", allSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.REQUESTED)
                .limit(5).toList());
        model.addAttribute("recentSessions", allSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .limit(5).toList());

        // Wallet stats
        model.addAttribute("totalEarned",  safeDecimal(transactionService.getTotalEarnings(userId)));
        model.addAttribute("totalSpent",   safeDecimal(transactionService.getTotalSpent(userId)));

        // Leaderboard previews
        model.addAttribute("topUsers",     userService.getLeaderboardByReputation().stream().limit(5).toList());
        model.addAttribute("streakLeaders",userService.getLeaderboardByStreak().stream().limit(5).toList());

        return "dashboard/home";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("topReputation", userService.getLeaderboardByReputation().stream().limit(10).toList());
        model.addAttribute("topStreak",     userService.getLeaderboardByStreak().stream().limit(10).toList());
        return "dashboard/leaderboard";
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private java.math.BigDecimal safeDecimal(java.math.BigDecimal value) {
        return value != null ? value : java.math.BigDecimal.ZERO;
    }
}
