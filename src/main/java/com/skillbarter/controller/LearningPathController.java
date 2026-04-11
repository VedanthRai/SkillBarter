package com.skillbarter.controller;

import com.skillbarter.dto.LearningPathDto;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String myPaths(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        var paths = learningPathService.getPathsForUser(userId);
        model.addAttribute("paths", paths);
        return "learning-paths/list";
    }

    @GetMapping("/{id}")
    public String viewPath(@PathVariable Long id, Model model) {
        var path = learningPathService.getPathWithProgress(id);
        model.addAttribute("path", path);
        return "learning-paths/view";
    }

    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String createForm(Model model) {
        model.addAttribute("pathDto", new LearningPathDto());
        return "learning-paths/create";
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String create(@Valid @ModelAttribute LearningPathDto dto,
                        RedirectAttributes ra) {
        Long userId = securityUtils.getCurrentUserId();
        learningPathService.createPath(userId, dto);
        ra.addFlashAttribute("success", "Learning path created successfully");
        return "redirect:/learning-paths";
    }

    @PostMapping("/{pathId}/steps/{stepId}/complete")
    @PreAuthorize("isAuthenticated()")
    public String completeStep(@PathVariable Long pathId,
                               @PathVariable Long stepId,
                               RedirectAttributes ra) {
        Long userId = securityUtils.getCurrentUserId();
        learningPathService.markStepComplete(stepId, userId);
        ra.addFlashAttribute("success", "Step marked as complete");
        return "redirect:/learning-paths/" + pathId;
    }
}
