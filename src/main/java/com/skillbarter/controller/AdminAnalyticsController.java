package com.skillbarter.controller;

import com.skillbarter.service.ChartDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAnalyticsController {
// Displays admin-level analytics and system metrics
    private final ChartDataService chartDataService;

    @GetMapping
    public String adminAnalytics(@RequestParam(defaultValue = "30") int days, Model model) {
        model.addAttribute("userGrowthData", chartDataService.getUserGrowthData(days));
        
        // Sessions by category
        model.addAttribute("sessionsCategoryData", chartDataService.getSessionsByCategoryData());
        
        // Credit flow
        model.addAttribute("creditFlowData", chartDataService.getCreditFlowData(days));
        
        // Peak usage hours
        model.addAttribute("peakUsageData", chartDataService.getPeakUsageHoursData());
        
        // Top skills by demand
        model.addAttribute("topSkillsData", chartDataService.getTopSkillsByDemandData());
        
        // Completion rate trend
        model.addAttribute("completionRateData", chartDataService.getCompletionRateTrendData(days));
        
        model.addAttribute("selectedDays", days);
        
        return "admin/analytics";
    }

    @GetMapping("/api/user-growth")
    @ResponseBody
    public Object getUserGrowthApi(@RequestParam(defaultValue = "30") int days) {
        return chartDataService.getUserGrowthData(days);
    }

    @GetMapping("/api/sessions-category")
    @ResponseBody
    public Object getSessionsCategoryApi() {
        return chartDataService.getSessionsByCategoryData();
    }

    @GetMapping("/api/credit-flow")
    @ResponseBody
    public Object getCreditFlowApi(@RequestParam(defaultValue = "30") int days) {
        return chartDataService.getCreditFlowData(days);
    }
}
