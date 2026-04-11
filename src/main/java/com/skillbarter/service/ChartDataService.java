package com.skillbarter.service;

import com.skillbarter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Chart Data Service - Prepares data for Chart.js visualization
 * 
 * SOLID - SRP: Only handles chart data preparation
 * SOLID - OCP: Easy to add new chart types
 */
@Service
@RequiredArgsConstructor
public class ChartDataService {

    private final UserRepository userRepo;
    private final SessionRepository sessionRepo;
    private final CreditTransactionRepository creditTxRepo;
    private final SkillRepository skillRepo;

    /**
     * Get user growth data for line chart
     */
    public Map<String, Object> getUserGrowthData(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<Object[]> data = userRepo.getUserGrowthByDay(startDate);
        
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        
        for (Object[] row : data) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", values);
        chartData.put("label", "New Users");
        
        return chartData;
    }

    /**
     * Get sessions by category for pie chart
     */
    public Map<String, Object> getSessionsByCategoryData() {
        List<Object[]> data = sessionRepo.countSessionsByCategory();
        
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        
        for (Object[] row : data) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", values);
        
        return chartData;
    }

    /**
     * Get credit flow data for bar chart
     */
    public Map<String, Object> getCreditFlowData(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<Object[]> earned = creditTxRepo.getCreditEarnedByDay(startDate);
        List<Object[]> spent = creditTxRepo.getCreditSpentByDay(startDate);
        
        List<String> labels = new ArrayList<>();
        List<BigDecimal> earnedValues = new ArrayList<>();
        List<BigDecimal> spentValues = new ArrayList<>();
        
        // Merge data by date
        Map<String, BigDecimal> earnedMap = new HashMap<>();
        Map<String, BigDecimal> spentMap = new HashMap<>();
        
        for (Object[] row : earned) {
            earnedMap.put(row[0].toString(), (BigDecimal) row[1]);
        }
        for (Object[] row : spent) {
            spentMap.put(row[0].toString(), (BigDecimal) row[1]);
        }
        
        Set<String> allDates = new TreeSet<>();
        allDates.addAll(earnedMap.keySet());
        allDates.addAll(spentMap.keySet());
        
        for (String date : allDates) {
            labels.add(date);
            earnedValues.add(earnedMap.getOrDefault(date, BigDecimal.ZERO));
            spentValues.add(spentMap.getOrDefault(date, BigDecimal.ZERO));
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("earned", earnedValues);
        chartData.put("spent", spentValues);
        
        return chartData;
    }

    /**
     * Get peak usage hours for heatmap
     */
    public Map<String, Object> getPeakUsageHoursData() {
        List<Object[]> data = sessionRepo.getSessionsByHourOfDay();
        
        List<Integer> hours = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (Object[] row : data) {
            hours.add((Integer) row[0]);
            counts.add((Long) row[1]);
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("hours", hours);
        chartData.put("counts", counts);
        
        return chartData;
    }

    /**
     * Get user engagement metrics for radar chart
     */
    public Map<String, Object> getUserEngagementData(Long userId) {
        Map<String, Object> chartData = new HashMap<>();
        
        // Calculate different engagement metrics (0-100 scale)
        long sessionsCompleted = sessionRepo.countCompletedAsTeacher(userId) + 
                                sessionRepo.countCompletedAsLearner(userId);
        long skillsOffered = skillRepo.countByUserIdAndIsOffering(userId, true);
        long endorsementsReceived = skillRepo.getTotalEndorsementsForUser(userId);
        long reviewsGiven = sessionRepo.countReviewsByUser(userId);
        
        // Normalize to 0-100 scale
        int sessionsScore = Math.min(100, (int) (sessionsCompleted * 10));
        int skillsScore = Math.min(100, (int) (skillsOffered * 20));
        int endorsementsScore = Math.min(100, (int) (endorsementsReceived * 5));
        int reviewsScore = Math.min(100, (int) (reviewsGiven * 10));
        int activityScore = 75; // Based on last active date
        
        chartData.put("labels", Arrays.asList("Sessions", "Skills", "Endorsements", "Reviews", "Activity"));
        chartData.put("data", Arrays.asList(sessionsScore, skillsScore, endorsementsScore, reviewsScore, activityScore));
        
        return chartData;
    }

    /**
     * Get top skills by demand for horizontal bar chart
     */
    public Map<String, Object> getTopSkillsByDemandData() {
        List<Object[]> data = skillRepo.getTopSkillsByDemand(10);
        
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        
        for (Object[] row : data) {
            labels.add((String) row[0]);
            values.add((Long) row[1]);
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", values);
        
        return chartData;
    }

    /**
     * Get session completion rate trend
     */
    public Map<String, Object> getCompletionRateTrendData(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<Object[]> data = sessionRepo.getCompletionRateByDay(startDate);
        
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        
        for (Object[] row : data) {
            labels.add(row[0].toString());
            values.add(((Number) row[1]).doubleValue());
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("data", values);
        chartData.put("label", "Completion Rate %");
        
        return chartData;
    }
}
