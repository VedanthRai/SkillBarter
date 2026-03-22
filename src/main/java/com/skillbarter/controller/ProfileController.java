package com.skillbarter.controller;

import com.skillbarter.entity.User;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Profile Controller — Major Feature 1.
 * Displays user profiles with the Decorator pattern.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final SecurityUtils securityUtils;
    private final ReviewService reviewService;
    private final SkillService skillService;
    private final TransactionService transactionService;

    @Autowired
    public ProfileController(UserService userService,
                             SecurityUtils securityUtils,
                             ReviewService reviewService,
                             SkillService skillService,
                             TransactionService transactionService) {
        this.userService = userService;
        this.securityUtils = securityUtils;
        this.reviewService = reviewService;
        this.skillService = skillService;
        this.transactionService = transactionService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String ownProfile(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        return "redirect:/profile/" + userId;
    }

    @GetMapping("/{userId}")
    public String viewProfile(@PathVariable Long userId, Model model) {
        model.addAttribute("profile", userService.buildProfileDecorator(userId));
        model.addAttribute("skills",  skillService.getSkillsForUser(userId));
        model.addAttribute("reviews", reviewService.getReviewsForUser(userId));

        Long currentId = securityUtils.getCurrentUserId();
        boolean isOwner = currentId != null && currentId.equals(userId);
        model.addAttribute("isOwner", isOwner);
        // Always pass wallet data — template checks isOwner before displaying
        model.addAttribute("totalEarned", transactionService.getTotalEarnings(userId));
        model.addAttribute("totalSpent",  transactionService.getTotalSpent(userId));
        return "profile/view";
    }

    @GetMapping("/edit")
    @PreAuthorize("isAuthenticated()")
    public String editForm(Model model) {
        model.addAttribute("user", securityUtils.getCurrentUser());
        return "profile/edit";
    }

    @PostMapping("/edit")
    @PreAuthorize("isAuthenticated()")
    public String saveEdit(@RequestParam(required = false) String bio,
                           @RequestParam(required = false) String timezone,
                           @RequestParam(required = false) MultipartFile avatar,
                           RedirectAttributes ra) {
        try {
            userService.updateProfile(securityUtils.getCurrentUserId(), bio, timezone, avatar);
            ra.addFlashAttribute("successMsg", "Profile updated.");
        } catch (java.io.IOException e) {
            ra.addFlashAttribute("errorMsg", "Profile saved but avatar upload failed.");
        }
        return "redirect:/profile";
    }
}
