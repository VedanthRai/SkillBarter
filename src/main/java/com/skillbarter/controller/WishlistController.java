package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Wishlist Controller — Minor Feature 1: Wishlist + Favorites.
 */
@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String viewWishlist(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        model.addAttribute("items", wishlistService.getWishlistForUser(userId));
        return "wishlist/index";
    }

    @PostMapping("/add/{skillId}")
    public String add(@PathVariable Long skillId,
                      @RequestParam(defaultValue = "true") boolean alertEnabled,
                      RedirectAttributes ra) {
        try {
            wishlistService.addToWishlist(securityUtils.getCurrentUserId(), skillId, alertEnabled);
            ra.addFlashAttribute("successMsg", "Added to your wishlist.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/skills/" + skillId;
    }

    @PostMapping("/remove/{skillId}")
    public String remove(@PathVariable Long skillId, RedirectAttributes ra) {
        try {
            wishlistService.removeFromWishlist(securityUtils.getCurrentUserId(), skillId);
            ra.addFlashAttribute("successMsg", "Removed from wishlist.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/wishlist";
    }
}
