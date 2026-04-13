package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.ActivityFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/activity")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ActivityFeedController {
// Displays user activity feed and recent actions
    private final ActivityFeedService activityFeedService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String activityFeed(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        
        model.addAttribute("myActivities", activityFeedService.getUserActivityFeed(userId, 20));
        model.addAttribute("platformActivities", activityFeedService.getPlatformActivityFeed(20));
        model.addAttribute("trending", activityFeedService.getTrendingActivities(10));
        
        return "activity/feed";
    }

    @GetMapping("/api/my-feed")
    @ResponseBody
    public Object getMyFeedApi(@RequestParam(defaultValue = "20") int limit) {
        Long userId = securityUtils.getCurrentUserId();
        return activityFeedService.getUserActivityFeed(userId, limit);
    }

    @GetMapping("/api/platform-feed")
    @ResponseBody
    public Object getPlatformFeedApi(@RequestParam(defaultValue = "20") int limit) {
        return activityFeedService.getPlatformActivityFeed(limit);
    }

    @GetMapping("/api/trending")
    @ResponseBody
    public Object getTrendingApi(@RequestParam(defaultValue = "10") int limit) {
        return activityFeedService.getTrendingActivities(limit);
    }
}
