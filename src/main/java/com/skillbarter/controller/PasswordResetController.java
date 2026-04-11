package com.skillbarter.controller;

import com.skillbarter.dto.PasswordResetDto;
import com.skillbarter.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping
    public String resetForm(Model model) {
        model.addAttribute("resetDto", new PasswordResetDto());
        return "auth/password-reset";
    }

    @PostMapping
    public String sendResetLink(@Valid @ModelAttribute PasswordResetDto dto,
                               RedirectAttributes ra) {
        passwordResetService.sendResetLink(dto.getEmail());
        ra.addFlashAttribute("success", "Password reset link sent to your email");
        return "redirect:/login";
    }

    @GetMapping("/confirm")
    public String confirmReset(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/password-reset-confirm";
    }

    @PostMapping("/confirm")
    public String resetPassword(@RequestParam String token,
                               @RequestParam String newPassword,
                               RedirectAttributes ra) {
        passwordResetService.resetPassword(token, newPassword);
        ra.addFlashAttribute("success", "Password reset successfully");
        return "redirect:/login";
    }
}
