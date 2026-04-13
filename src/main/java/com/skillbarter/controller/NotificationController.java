package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Manages user notification UI (view and mark as read)
@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String list(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        model.addAttribute("notifications",
                notificationService.getForUser(userId));
        notificationService.markAllRead(userId);
        return "dashboard/notifications";
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public String markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return "ok";
    }

    @PostMapping("/read-all")
    public String markAllRead(RedirectAttributes ra) {
        notificationService.markAllRead(securityUtils.getCurrentUserId());
        ra.addFlashAttribute("successMsg", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
