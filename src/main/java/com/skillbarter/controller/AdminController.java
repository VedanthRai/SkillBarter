package com.skillbarter.controller;
//Provides admin operations like user management and system control
import com.skillbarter.enums.Role;
import com.skillbarter.service.*;
import com.skillbarter.entity.User;
import com.skillbarter.repository.UserRepository;
import com.skillbarter.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final SkillService skillService;
    private final SessionService sessionService;
    private final DisputeService disputeService;
    private final NotificationService notificationService;
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String dashboard(Model model) {
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("totalUsers",    allUsers.size());
        model.addAttribute("totalSessions", sessionService.getSessionsByStatus(
                com.skillbarter.enums.SessionStatus.COMPLETED).size());
        model.addAttribute("openDisputes",  disputeService.getOpenDisputes().size());
        model.addAttribute("pendingVerifications",
                skillService.getPendingVerification().size());
        model.addAttribute("recentUsers", allUsers.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10).toList());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/ban")
    public String ban(@PathVariable Long id,
                      @RequestParam(defaultValue = "Policy violation") String reason,
                      RedirectAttributes ra) {
        userService.banUser(id, reason);
        notificationService.sendAdminMessage(userService.findById(id),
                "Your account has been banned. Reason: " + reason);
        ra.addFlashAttribute("successMsg", "User banned.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/suspend")
    public String suspend(@PathVariable Long id, RedirectAttributes ra) {
        userService.suspendUser(id);
        ra.addFlashAttribute("successMsg", "User suspended.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reinstate")
    public String reinstate(@PathVariable Long id, RedirectAttributes ra) {
        userService.reinstateUser(id);
        ra.addFlashAttribute("successMsg", "User reinstated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/make-verifier")
    public String makeVerifier(@PathVariable Long id, RedirectAttributes ra) {
        User user = userService.findById(id);
        user.setRole(Role.ROLE_VERIFIER);
        userRepository.save(user);
        ra.addFlashAttribute("successMsg", user.getUsername() + " promoted to Verifier.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/grant-credits")
    public String grantCredits(@PathVariable Long id,
                               @RequestParam BigDecimal amount,
                               @RequestParam(defaultValue = "Admin grant") String reason,
                               RedirectAttributes ra) {
        User admin = securityUtils.getCurrentUser();
        User recipient = userService.findById(id);
        walletService.adminGrantCredits(admin, recipient, amount, reason);
        notificationService.sendAdminMessage(recipient,
                String.format("%.2f credits have been added to your account. Reason: %s", amount, reason));
        ra.addFlashAttribute("successMsg",
                String.format("Granted %.2f credits to %s.", amount, recipient.getUsername()));
        return "redirect:/admin/users";
    }

    @GetMapping("/skills/pending")
    public String pendingVerifications(Model model) {
        model.addAttribute("skills", skillService.getPendingVerification());
        model.addAttribute("currentUserId", securityUtils.getCurrentUserId());
        return "admin/pending-skills";
    }

    @PostMapping("/skills/{id}/verify")
    public String verifySkill(@PathVariable Long id,
                              @RequestParam Long verifierId,
                              RedirectAttributes ra) {
        skillService.verifySkill(id, verifierId);
        ra.addFlashAttribute("successMsg", "Skill verified. Badge awarded to user.");
        return "redirect:/admin/skills/pending";
    }

    @GetMapping("/disputes")
    public String disputes(Model model) {
        var allDisputes = disputeService.getAllDisputes();
        model.addAttribute("disputes", allDisputes);
        model.addAttribute("verifiers", userRepository.findByRole(Role.ROLE_VERIFIER));
        // Pre-compute counts server-side (Thymeleaf doesn't support SpEL .?[] selection)
        model.addAttribute("openCount", allDisputes.stream()
                .filter(d -> d.getStatus().name().equals("OPEN")).count());
        model.addAttribute("reviewCount", allDisputes.stream()
                .filter(d -> d.getStatus().name().equals("UNDER_REVIEW")).count());
        model.addAttribute("resolvedCount", allDisputes.stream()
                .filter(d -> d.getStatus().name().startsWith("RESOLVED")).count());
        model.addAttribute("totalCount", allDisputes.size());
        return "admin/disputes";
    }
}
