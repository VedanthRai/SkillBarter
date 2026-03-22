package com.skillbarter.controller;

import com.skillbarter.dto.RegisterRequest;
import com.skillbarter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Authentication Controller — MVC enforced.
 * Handles registration, login, logout.
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null)  model.addAttribute("loginError", "Invalid email or password.");
        if (logout != null) model.addAttribute("logoutMsg",  "You have been logged out.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (!request.passwordsMatch()) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Account created! You received 5 free credits. Please log in.");
            return "redirect:/auth/login";
        } catch (Exception ex) {
            model.addAttribute("registerError", ex.getMessage());
            return "auth/register";
        }
    }
}
