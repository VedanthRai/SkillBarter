package com.skillbarter.controller;

import com.skillbarter.dto.SkillSwapDto;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.SkillSwapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/skill-swaps")
@RequiredArgsConstructor
public class SkillSwapController {

    private final SkillSwapService skillSwapService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String viewSwaps(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        var swaps = skillSwapService.getSwapsForUser(userId);
        model.addAttribute("swaps", swaps);
        return "skill-swaps/list";
    }

    @GetMapping("/propose")
    @PreAuthorize("isAuthenticated()")
    public String proposeForm(@RequestParam(required = false) Long skillId, Model model) {
        model.addAttribute("swapDto", new SkillSwapDto());
        model.addAttribute("preselectedSkillId", skillId);
        return "skill-swaps/propose";
    }

    @PostMapping("/propose")
    @PreAuthorize("isAuthenticated()")
    public String propose(@Valid @ModelAttribute SkillSwapDto dto,
                         RedirectAttributes ra) {
        Long userId = securityUtils.getCurrentUserId();
        skillSwapService.proposeSwap(userId, dto);
        ra.addFlashAttribute("success", "Skill swap proposal sent");
        return "redirect:/skill-swaps";
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public String accept(@PathVariable Long id,
                        RedirectAttributes ra) {
        Long userId = securityUtils.getCurrentUserId();
        skillSwapService.acceptSwap(id, userId);
        ra.addFlashAttribute("success", "Skill swap accepted! Sessions created.");
        return "redirect:/skill-swaps";
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public String reject(@PathVariable Long id,
                        RedirectAttributes ra) {
        Long userId = securityUtils.getCurrentUserId();
        skillSwapService.rejectSwap(id, userId);
        ra.addFlashAttribute("success", "Skill swap rejected");
        return "redirect:/skill-swaps";
    }
}
