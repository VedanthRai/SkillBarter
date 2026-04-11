package com.skillbarter.controller;

import com.skillbarter.dto.SkillRequestDto;
import com.skillbarter.entity.User;
import com.skillbarter.service.SkillRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/skill-requests")
@RequiredArgsConstructor
public class SkillRequestController {

    private final SkillRequestService skillRequestService;

    @GetMapping
    public String viewBoard(Model model) {
        var requests = skillRequestService.getAllActiveRequests();
        model.addAttribute("requests", requests);
        return "skill-requests/board";
    }

    @GetMapping("/my-requests")
    public String myRequests(@AuthenticationPrincipal User user, Model model) {
        var requests = skillRequestService.getRequestsByUser(user.getId());
        model.addAttribute("requests", requests);
        return "skill-requests/my-requests";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("requestDto", new SkillRequestDto());
        return "skill-requests/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute SkillRequestDto dto,
                        @AuthenticationPrincipal User user,
                        RedirectAttributes ra) {
        skillRequestService.createRequest(user.getId(), dto);
        ra.addFlashAttribute("success", "Skill request posted successfully");
        return "redirect:/skill-requests";
    }

    @PostMapping("/{id}/offer")
    public String offerToTeach(@PathVariable Long id,
                              @AuthenticationPrincipal User user,
                              RedirectAttributes ra) {
        skillRequestService.offerToTeach(id, user.getId());
        ra.addFlashAttribute("success", "Offer sent to requester");
        return "redirect:/skill-requests";
    }

    @PostMapping("/{id}/close")
    public String closeRequest(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes ra) {
        skillRequestService.closeRequest(id, user.getId());
        ra.addFlashAttribute("success", "Request closed");
        return "redirect:/skill-requests/my-requests";
    }
}
