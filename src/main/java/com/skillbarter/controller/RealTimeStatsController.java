package com.skillbarter.controller;

import com.skillbarter.service.RealTimeStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
// Provides real-time system statistics and updates
@Controller
@RequestMapping("/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RealTimeStatsController {

    private final RealTimeStatsService realTimeStatsService;

    @GetMapping
    public String realTimeStats(Model model) {
        model.addAttribute("platformStats", realTimeStatsService.getPlatformStats());
        model.addAttribute("activityStats", realTimeStatsService.getUserActivityStats());
        model.addAttribute("trendingStats", realTimeStatsService.getTrendingStats());
        model.addAttribute("healthMetrics", realTimeStatsService.getHealthMetrics());
        
        return "admin/realtime-stats";
    }

    @GetMapping("/api/platform")
    @ResponseBody
    public Map<String, Object> getPlatformStatsApi() {
        return realTimeStatsService.getPlatformStats();
    }

    @GetMapping("/api/activity")
    @ResponseBody
    public Map<String, Object> getActivityStatsApi() {
        return realTimeStatsService.getUserActivityStats();
    }

    @GetMapping("/api/trending")
    @ResponseBody
    public Map<String, Object> getTrendingStatsApi() {
        return realTimeStatsService.getTrendingStats();
    }

    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> getHealthMetricsApi() {
        return realTimeStatsService.getHealthMetrics();
    }

    @GetMapping("/api/all")
    @ResponseBody
    public Map<String, Object> getAllStatsApi() {
        Map<String, Object> allStats = new HashMap<>();
        allStats.put("platform", realTimeStatsService.getPlatformStats());
        allStats.put("activity", realTimeStatsService.getUserActivityStats());
        allStats.put("trending", realTimeStatsService.getTrendingStats());
        allStats.put("health", realTimeStatsService.getHealthMetrics());
        return allStats;
    }
}
