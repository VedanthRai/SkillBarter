// Vijay: Handles skill endorsements between users
// Improves credibility and influences matching results
package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.EndorsementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Endorsement Controller — Peer skill endorsements.
 */
@Controller
@RequestMapping("/endorsements")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EndorsementController {

    private final EndorsementService endorsementService;
    private final SecurityUtils securityUtils;

    @PostMapping("/endorse/{skillId}")
    public String endorse(@PathVariable Long skillId,
                          @RequestParam(required = false, defaultValue = "") String note,
                          RedirectAttributes ra) {
        try {
            endorsementService.endorse(securityUtils.getCurrentUserId(), skillId, note);
            ra.addFlashAttribute("successMsg", "Skill endorsed successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/skills/" + skillId;
    }

    @PostMapping("/remove/{skillId}")
    public String remove(@PathVariable Long skillId, RedirectAttributes ra) {
        try {
            endorsementService.removeEndorsement(securityUtils.getCurrentUserId(), skillId);
            ra.addFlashAttribute("successMsg", "Endorsement removed.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/skills/" + skillId;
    }
}
