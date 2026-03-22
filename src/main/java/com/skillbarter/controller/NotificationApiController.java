package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API controller for notification badge polling (used by app.js).
 * Returns unread count as JSON — consumed by the navbar badge refresher.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationApiController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) return ResponseEntity.ok(Map.of("count", 0L));
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
