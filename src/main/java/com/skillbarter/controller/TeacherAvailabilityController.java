package com.skillbarter.controller;

import com.skillbarter.dto.TeacherAvailabilityDto;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.TeacherAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/availability")
@RequiredArgsConstructor
public class TeacherAvailabilityController {

    private final TeacherAvailabilityService availabilityService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String viewAvailability(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        var slots = availabilityService.getAvailabilityForTeacher(userId);
        model.addAttribute("slots", slots);
        return "availability/list";
    }

    @GetMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String addSlotForm(Model model) {
        model.addAttribute("slotDto", new TeacherAvailabilityDto());
        return "availability/add";
    }

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String addSlot(@Valid @ModelAttribute TeacherAvailabilityDto dto,
                         RedirectAttributes ra) {
        Long userId = securityUtils.getCurrentUserId();
        availabilityService.addAvailability(userId, dto);
        ra.addFlashAttribute("success", "Availability slot added");
        return "redirect:/availability";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteSlot(@PathVariable Long id,
                            RedirectAttributes ra) {
        Long userId = securityUtils.getCurrentUserId();
        availabilityService.deleteAvailability(id, userId);
        ra.addFlashAttribute("success", "Availability slot deleted");
        return "redirect:/availability";
    }
}
