package com.skillbarter.controller;

import com.skillbarter.dto.SkillDto;
import com.skillbarter.enums.SkillCategory;
import com.skillbarter.matching.MatchingService;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.EndorsementService;
import com.skillbarter.service.SkillService;
import com.skillbarter.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Skill Controller — Major Feature 1 (profiles), Minor Feature 1 (search).
 * MVC: handles HTTP, delegates to SkillService & MatchingService.
 */
@Controller
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final MatchingService matchingService;
    private final SecurityUtils securityUtils;
    private final EndorsementService endorsementService;
    private final WishlistService wishlistService;

    // ── Browse/Search (public) ────────────────────────────────────────────

    @GetMapping("/browse")
    public String browse(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) SkillCategory category,
                         Model model) {
        if (category != null) {
            model.addAttribute("skills", skillService.getByCategory(category));
        } else {
            model.addAttribute("skills", skillService.searchOfferedSkills(keyword));
        }
        model.addAttribute("categories", SkillCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        return "skills/browse";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false, defaultValue = "") String q,
                         @RequestParam(defaultValue = "ratingBasedStrategy") String strategy,
                         Model model) {
        String trimmedQuery = q == null ? "" : q.trim();
        var currentUser = securityUtils.getCurrentUser();
        List<com.skillbarter.entity.Skill> matches;
        if (currentUser != null) {
            matches = matchingService.findAllMatches(currentUser, trimmedQuery, strategy);
        } else {
            matches = skillService.searchOfferedSkills(trimmedQuery);
        }
        model.addAttribute("matches", matches);
        model.addAttribute("query", trimmedQuery);
        model.addAttribute("strategy", strategy);
        model.addAttribute("strategies", matchingService.getAvailableStrategies().entrySet());
        model.addAttribute("isDiscoveryMode", trimmedQuery.isBlank());
        return "skills/search";
    }

    // ── My Skills ─────────────────────────────────────────────────────────

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public String mySkills(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        model.addAttribute("skills", skillService.getSkillsForUser(userId));
        model.addAttribute("skillDto", new SkillDto());
        model.addAttribute("categories", SkillCategory.values());
        return "skills/my-skills";
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String addSkill(@Valid @ModelAttribute SkillDto skillDto,
                           BindingResult result,
                           @RequestParam(required = false) MultipartFile certificate,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", SkillCategory.values());
            model.addAttribute("skills", skillService.getSkillsForUser(securityUtils.getCurrentUserId()));
            return "skills/my-skills";
        }
        try {
            skillService.addSkill(securityUtils.getCurrentUserId(), skillDto, certificate);
            redirectAttributes.addFlashAttribute("successMsg", "Skill added successfully!");
        } catch (java.io.IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Skill saved but certificate upload failed: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/skills/my";
    }

    @GetMapping("/{id}")
    public String viewSkill(@PathVariable Long id, Model model) {
        model.addAttribute("skill", skillService.findById(id));
        Long userId = securityUtils.getCurrentUserId();
        model.addAttribute("endorsements", endorsementService.getEndorsementsForSkill(id));
        model.addAttribute("hasEndorsed", userId != null && endorsementService.hasEndorsed(userId, id));
        model.addAttribute("isWishlisted", userId != null && wishlistService.isWishlisted(userId, id));
        return "skills/view";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteSkill(@PathVariable Long id, RedirectAttributes ra) {
        skillService.deleteSkill(id, securityUtils.getCurrentUserId());
        ra.addFlashAttribute("successMsg", "Skill deleted.");
        return "redirect:/skills/my";
    }
}
