package com.skillbarter.controller;

import com.skillbarter.entity.Session;
import com.skillbarter.enums.SessionStatus;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analytics Controller — Skill analytics for teachers.
 *
 * Shows sessions taught, average rating, credits earned,
 * and per-skill breakdown.
 *
 * SOLID – SRP: only handles analytics display.
 */
@Controller
@RequestMapping("/analytics")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AnalyticsController {

    private final SecurityUtils securityUtils;
    private final SessionService sessionService;
    private final SkillService skillService;
    private final ReviewService reviewService;
    private final WalletService walletService;

    @GetMapping
    public String analytics(Model model) {
        Long userId = securityUtils.getCurrentUserId();

        List<Session> allSessions = sessionService.getSessionsForUser(userId);

        // Teaching stats
        List<Session> taughtSessions = allSessions.stream()
                .filter(s -> s.getTeacher().getId().equals(userId)
                          && s.getStatus() == SessionStatus.COMPLETED)
                .toList();

        // Learning stats
        List<Session> learnedSessions = allSessions.stream()
                .filter(s -> s.getLearner().getId().equals(userId)
                          && s.getStatus() == SessionStatus.COMPLETED)
                .toList();

        // Credits earned per skill
        Map<String, BigDecimal> earningsBySkill = taughtSessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getSkill().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Session::getCreditAmount, BigDecimal::add)
                ));

        // Sessions per skill
        Map<String, Long> sessionsBySkill = taughtSessions.stream()
                .collect(Collectors.groupingBy(s -> s.getSkill().getName(), Collectors.counting()));

        // Total minutes taught
        int totalMinutesTaught = taughtSessions.stream()
                .mapToInt(Session::getDurationMinutes).sum();

        // Total minutes learned
        int totalMinutesLearned = learnedSessions.stream()
                .mapToInt(Session::getDurationMinutes).sum();

        model.addAttribute("mySkills", skillService.getSkillsForUser(userId));
        model.addAttribute("taughtSessions", taughtSessions);
        model.addAttribute("learnedSessions", learnedSessions);
        model.addAttribute("earningsBySkill", earningsBySkill);
        model.addAttribute("sessionsBySkill", sessionsBySkill);
        model.addAttribute("totalMinutesTaught", totalMinutesTaught);
        model.addAttribute("totalMinutesLearned", totalMinutesLearned);
        model.addAttribute("totalEarned", walletService.getTotalEarned(userId));
        model.addAttribute("totalSpent", walletService.getTotalSpent(userId));
        model.addAttribute("reviews", reviewService.getReviewsForUser(userId));
        model.addAttribute("avgRating", reviewService.getAverageRating(userId));
        model.addAttribute("pendingSessions", allSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.REQUESTED
                          || s.getStatus() == SessionStatus.ACCEPTED).count());

        return "analytics/index";
    }
}
