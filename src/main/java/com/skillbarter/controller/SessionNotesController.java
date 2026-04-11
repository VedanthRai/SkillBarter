package com.skillbarter.controller;

import com.skillbarter.dto.SessionNotesDto;
import com.skillbarter.entity.User;
import com.skillbarter.service.SessionNotesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sessions/{sessionId}/notes")
@RequiredArgsConstructor
public class SessionNotesController {

    private final SessionNotesService sessionNotesService;

    @GetMapping
    public String viewNotes(@PathVariable Long sessionId, Model model, @AuthenticationPrincipal User user) {
        var notes = sessionNotesService.getNotesForSession(sessionId);
        model.addAttribute("notes", notes);
        model.addAttribute("sessionId", sessionId);
        return "sessions/notes";
    }

    @GetMapping("/add")
    public String addNotesForm(@PathVariable Long sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("notesDto", new SessionNotesDto());
        return "sessions/add-notes";
    }

    @PostMapping("/add")
    public String addNotes(@PathVariable Long sessionId,
                          @Valid @ModelAttribute SessionNotesDto dto,
                          @AuthenticationPrincipal User user,
                          RedirectAttributes ra) {
        sessionNotesService.addNotes(sessionId, user.getId(), dto);
        ra.addFlashAttribute("success", "Session notes added successfully");
        return "redirect:/sessions/" + sessionId;
    }
}
