package com.skillbarter.config;

import com.skillbarter.entity.User;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * GlobalModelAdvice — injects common model attributes into EVERY controller response.
 *
 * This solves the problem of every controller needing to manually add:
 *   - unreadCount  (notification badge in navbar)
 *   - currentUser  (for conditional rendering anywhere)
 *
 * SOLID – SRP: keeps cross-cutting view concerns out of individual controllers.
 * SOLID – DRY: no duplication of these model attributes across 8+ controllers.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

    /**
     * Adds unreadCount to every model so the navbar notification badge always works.
     * Returns 0 safely for unauthenticated users.
     */
    @ModelAttribute("unreadCount")
    public long unreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return 0L;
        }
        try {
            Long userId = securityUtils.getCurrentUserId();
            if (userId == null) return 0L;
            return notificationService.getUnreadCount(userId);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Adds the current authenticated User entity to every model.
     * Null for unauthenticated requests — templates must use th:if="${currentUser != null}".
     */
    @ModelAttribute("currentUser")
    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            return securityUtils.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
