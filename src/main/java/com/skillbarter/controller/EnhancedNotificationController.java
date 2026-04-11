package com.skillbarter.controller;

import com.skillbarter.entity.Notification;
import com.skillbarter.enums.NotificationType;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notifications")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class EnhancedNotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping("/center")
    public String notificationCenter(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        
        List<Notification> allNotifications = notificationService.getForUser(userId);
        
        // Group by type
        Map<NotificationType, List<Notification>> grouped = allNotifications.stream()
            .collect(Collectors.groupingBy(Notification::getType));
        
        // Separate read and unread
        List<Notification> unread = allNotifications.stream()
            .filter(n -> !n.getIsRead())
            .collect(Collectors.toList());
        
        List<Notification> read = allNotifications.stream()
            .filter(Notification::getIsRead)
            .limit(20)
            .collect(Collectors.toList());
        
        model.addAttribute("allNotifications", allNotifications);
        model.addAttribute("groupedNotifications", grouped);
        model.addAttribute("unreadNotifications", unread);
        model.addAttribute("readNotifications", read);
        model.addAttribute("unreadCount", unread.size());
        model.addAttribute("types", NotificationType.values());
        
        return "notifications/center";
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public Map<String, Object> markAllAsRead() {
        Long userId = securityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Map.of("success", true, "message", "All notifications marked as read");
    }

    @PostMapping("/archive/{id}")
    @ResponseBody
    public Map<String, Object> archiveNotification(@PathVariable Long id) {
        // Archive functionality (can be implemented later)
        return Map.of("success", true, "message", "Notification archived");
    }

    @GetMapping("/api/unread-count")
    @ResponseBody
    public Map<String, Object> getUnreadCount() {
        Long userId = securityUtils.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return Map.of("count", count);
    }

    @GetMapping("/api/filter")
    @ResponseBody
    public List<Notification> filterNotifications(@RequestParam String type) {
        Long userId = securityUtils.getCurrentUserId();
        List<Notification> all = notificationService.getForUser(userId);
        
        if ("all".equals(type)) {
            return all;
        }
        
        try {
            NotificationType notifType = NotificationType.valueOf(type);
            return all.stream()
                .filter(n -> n.getType() == notifType)
                .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return all;
        }
    }
}
