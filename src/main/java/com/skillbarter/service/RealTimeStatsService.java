package com.skillbarter.service;

import com.skillbarter.enums.SessionStatus;
import com.skillbarter.enums.UserStatus;
import com.skillbarter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Real-Time Statistics Service - Provides live platform statistics
 * 
 * SOLID - SRP: Only handles real-time statistics
 * SOLID - OCP: Easy to add new statistics
 */
@Service
@RequiredArgsConstructor
public class RealTimeStatsService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SkillRepository skillRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    /**
     * Get platform-wide real-time statistics
     */
    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);

        // Session statistics
        long totalSessions = sessionRepository.count();
        long completedSessions = sessionRepository.findByStatus(SessionStatus.COMPLETED).size();
        long activeSessions = sessionRepository.findByStatus(SessionStatus.IN_PROGRESS).size();
        long pendingSessions = sessionRepository.findByStatus(SessionStatus.REQUESTED).size();
        
        stats.put("totalSessions", totalSessions);
        stats.put("completedSessions", completedSessions);
        stats.put("activeSessions", activeSessions);
        stats.put("pendingSessions", pendingSessions);

        // Skill statistics
        long totalSkills = skillRepository.count();
        long offeredSkills = skillRepository.findByIsOfferingTrue().size();
        long verifiedSkills = skillRepository.findByVerifiedTrue().size();
        
        stats.put("totalSkills", totalSkills);
        stats.put("offeredSkills", offeredSkills);
        stats.put("verifiedSkills", verifiedSkills);

        // Credit statistics
        BigDecimal totalCreditsInCirculation = userRepository.findAll().stream()
            .map(u -> u.getCreditBalance() != null ? u.getCreditBalance() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCreditsInEscrow = userRepository.findAll().stream()
            .map(u -> u.getEscrowBalance() != null ? u.getEscrowBalance() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalCreditsInCirculation", totalCreditsInCirculation);
        stats.put("totalCreditsInEscrow", totalCreditsInEscrow);

        // Completion rate
        double completionRate = totalSessions > 0 
            ? (double) completedSessions / totalSessions * 100.0 
            : 0.0;
        stats.put("completionRate", String.format("%.1f%%", completionRate));

        return stats;
    }

    /**
     * Get user activity statistics
     */
    public Map<String, Object> getUserActivityStats() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24h = now.minusHours(24);
        LocalDateTime last7d = now.minusDays(7);
        LocalDateTime last30d = now.minusDays(30);

        // Active users by time period
        long activeToday = userRepository.findAll().stream()
            .filter(u -> u.getLastActiveAt() != null && u.getLastActiveAt().isAfter(last24h))
            .count();

        long activeThisWeek = userRepository.findAll().stream()
            .filter(u -> u.getLastActiveAt() != null && u.getLastActiveAt().isAfter(last7d))
            .count();

        long activeThisMonth = userRepository.findAll().stream()
            .filter(u -> u.getLastActiveAt() != null && u.getLastActiveAt().isAfter(last30d))
            .count();

        stats.put("activeToday", activeToday);
        stats.put("activeThisWeek", activeThisWeek);
        stats.put("activeThisMonth", activeThisMonth);

        return stats;
    }

    /**
     * Get trending statistics
     */
    public Map<String, Object> getTrendingStats() {
        Map<String, Object> stats = new HashMap<>();

        // Most popular categories
        var categoryData = sessionRepository.countSessionsByCategory();
        if (!categoryData.isEmpty()) {
            Object[] topCategory = categoryData.get(0);
            stats.put("topCategory", topCategory[0]);
            stats.put("topCategoryCount", topCategory[1]);
        }

        // Peak usage hour
        var hourData = sessionRepository.getSessionsByHourOfDay();
        if (!hourData.isEmpty()) {
            Object[] peakHour = hourData.stream()
                .max((a, b) -> Long.compare((Long) a[1], (Long) b[1]))
                .orElse(new Object[]{0, 0L});
            stats.put("peakHour", peakHour[0] + ":00");
            stats.put("peakHourSessions", peakHour[1]);
        }

        return stats;
    }

    /**
     * Get health metrics
     */
    public Map<String, String> getHealthMetrics() {
        Map<String, String> health = new HashMap<>();

        long totalSessions = sessionRepository.count();
        long completedSessions = sessionRepository.findByStatus(SessionStatus.COMPLETED).size();
        
        double completionRate = totalSessions > 0 
            ? (double) completedSessions / totalSessions * 100.0 
            : 0.0;

        // Platform health indicators
        health.put("completionRate", completionRate >= 80 ? "Excellent" : 
                                     completionRate >= 60 ? "Good" : 
                                     completionRate >= 40 ? "Fair" : "Needs Improvement");

        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long totalUsers = userRepository.count();
        double activeRate = totalUsers > 0 ? (double) activeUsers / totalUsers * 100.0 : 0.0;

        health.put("userEngagement", activeRate >= 80 ? "High" : 
                                     activeRate >= 50 ? "Medium" : "Low");

        long verifiedSkills = skillRepository.findByVerifiedTrue().size();
        long totalSkills = skillRepository.count();
        double verificationRate = totalSkills > 0 ? (double) verifiedSkills / totalSkills * 100.0 : 0.0;

        health.put("verificationRate", verificationRate >= 30 ? "Good" : 
                                       verificationRate >= 15 ? "Fair" : "Low");

        return health;
    }
}
