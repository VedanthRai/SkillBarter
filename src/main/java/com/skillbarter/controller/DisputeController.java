package com.skillbarter.controller;

import com.skillbarter.dto.DisputeRequestDto;
import com.skillbarter.entity.Dispute;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Dispute Controller — Major Feature 4: Dispute Tribunal.
 * Handles raise → assign → resolve lifecycle.
 */
@Controller
@RequestMapping("/disputes")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DisputeController {

    private final DisputeService disputeService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String myDisputes(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        model.addAttribute("disputes", disputeService.getDisputesRaisedBy(userId));
        if (securityUtils.isVerifier()) {
            model.addAttribute("assignedDisputes",
                    disputeService.getDisputesForVerifier(userId));
            model.addAttribute("openDisputes", disputeService.getOpenDisputes());
        }
        return "disputes/list";
    }

    @GetMapping("/{id}")
    public String viewDispute(@PathVariable Long id, Model model) {
        Dispute dispute = disputeService.getDispute(id);
        model.addAttribute("dispute", dispute);
        model.addAttribute("userId", securityUtils.getCurrentUserId());
        model.addAttribute("isVerifier", securityUtils.isVerifier());
        return "disputes/detail";
    }

    @GetMapping("/raise/{sessionId}")
    public String raiseDisputeForm(@PathVariable Long sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("disputeDto", new DisputeRequestDto());
        return "disputes/raise";
    }

    @PostMapping("/raise/{sessionId}")
    public String raiseDispute(@PathVariable Long sessionId,
                               @Valid @ModelAttribute DisputeRequestDto dto,
                               BindingResult result,
                               RedirectAttributes ra,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("sessionId", sessionId);
            return "disputes/raise";
        }
        Dispute dispute = disputeService.raiseDispute(
                sessionId, securityUtils.getCurrentUserId(), dto);
        ra.addFlashAttribute("successMsg",
                "Dispute raised. A verifier will be assigned shortly.");
        return "redirect:/disputes/" + dispute.getId();
    }

    @PostMapping("/assign/{disputeId}")
    @PreAuthorize("hasAnyRole('VERIFIER','ADMIN')")
    public String assignVerifier(@PathVariable Long disputeId,
                                 @RequestParam Long verifierId,
                                 RedirectAttributes ra) {
        disputeService.assignVerifier(disputeId, verifierId);
        ra.addFlashAttribute("successMsg", "Verifier assigned.");
        return "redirect:/disputes/" + disputeId;
    }

    @PostMapping("/resolve/{disputeId}/teacher")
    @PreAuthorize("hasAnyRole('VERIFIER','ADMIN')")
    public String resolveForTeacher(@PathVariable Long disputeId,
                                    @RequestParam String resolution,
                                    RedirectAttributes ra) {
        disputeService.resolveInFavourOfTeacher(
                disputeId, securityUtils.getCurrentUserId(), resolution);
        ra.addFlashAttribute("successMsg",
                "Dispute resolved in favour of teacher. Credits released.");
        return "redirect:/disputes/" + disputeId;
    }

    @PostMapping("/resolve/{disputeId}/learner")
    @PreAuthorize("hasAnyRole('VERIFIER','ADMIN')")
    public String resolveForLearner(@PathVariable Long disputeId,
                                    @RequestParam String resolution,
                                    RedirectAttributes ra) {
        disputeService.resolveInFavourOfLearner(
                disputeId, securityUtils.getCurrentUserId(), resolution);
        ra.addFlashAttribute("successMsg",
                "Dispute resolved in favour of learner. Credits refunded.");
        return "redirect:/disputes/" + disputeId;
    }
}
