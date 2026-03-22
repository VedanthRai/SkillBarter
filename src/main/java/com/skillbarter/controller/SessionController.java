package com.skillbarter.controller;

import com.skillbarter.dto.ReviewDto;
import com.skillbarter.dto.SessionRequestDto;
import com.skillbarter.entity.Session;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;

/**
 * Session Controller — Major Features 2, 3.
 * Full lifecycle: request → accept → start → complete → review.
 */
@Controller
@RequestMapping("/sessions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SessionController {

    private final SessionService sessionService;
    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String mySessions(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        model.addAttribute("sessions", sessionService.getSessionsForUser(userId));
        model.addAttribute("userId", userId);
        return "sessions/list";
    }

    @GetMapping("/{id}")
    public String viewSession(@PathVariable Long id, Model model) {
        Session session = sessionService.getSession(id);
        Long userId = securityUtils.getCurrentUserId();

        boolean isParticipant = session.getLearner().getId().equals(userId)
                             || session.getTeacher().getId().equals(userId);
        if (!isParticipant && !securityUtils.isAdmin()) {
            return "redirect:/sessions";
        }

        model.addAttribute("session", session);
        model.addAttribute("userId", userId);
        model.addAttribute("isTeacher", session.getTeacher().getId().equals(userId));
        model.addAttribute("isLearner", session.getLearner().getId().equals(userId));
        model.addAttribute("messages", sessionService.getMessagesForSession(id, userId));
        model.addAttribute("reviewDto", new ReviewDto());
        return "sessions/detail";
    }

    @GetMapping("/request/{skillId}")
    public String requestForm(@PathVariable Long skillId, Model model) {
        model.addAttribute("sessionRequest", new SessionRequestDto());
        model.addAttribute("skillId", skillId);
        return "sessions/request";
    }

    @PostMapping("/request")
    public String requestSession(@Valid @ModelAttribute SessionRequestDto dto,
                                 BindingResult result,
                                 RedirectAttributes ra,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("skillId", dto.getSkillId());
            return "sessions/request";
        }
        if (!dto.isScheduledInFuture()) {
            model.addAttribute("errorMsg", "Scheduled time must be in the future.");
            model.addAttribute("skillId", dto.getSkillId());
            return "sessions/request";
        }
        try {
            Session session = sessionService.requestSession(securityUtils.getCurrentUserId(), dto);
            ra.addFlashAttribute("successMsg",
                    "Session requested! Credits held in escrow pending teacher acceptance.");
            return "redirect:/sessions/" + session.getId();
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("skillId", dto.getSkillId());
            return "sessions/request";
        }
    }

    @PostMapping("/{id}/accept")
    public String acceptSession(@PathVariable Long id, RedirectAttributes ra) {
        sessionService.acceptSession(id, securityUtils.getCurrentUserId());
        ra.addFlashAttribute("successMsg", "Session accepted! Learner has been notified.");
        return "redirect:/sessions/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelSession(@PathVariable Long id, RedirectAttributes ra) {
        sessionService.cancelSession(id, securityUtils.getCurrentUserId());
        ra.addFlashAttribute("infoMsg", "Session cancelled. Credits refunded.");
        return "redirect:/sessions";
    }

    @PostMapping("/{id}/start")
    public String startSession(@PathVariable Long id, RedirectAttributes ra) {
        sessionService.startSession(id, securityUtils.getCurrentUserId());
        ra.addFlashAttribute("successMsg", "Session started! Timer is running.");
        return "redirect:/sessions/" + id;
    }

    @PostMapping("/{id}/confirm")
    public String confirmCompletion(@PathVariable Long id, RedirectAttributes ra) {
        sessionService.confirmCompletion(id, securityUtils.getCurrentUserId());
        ra.addFlashAttribute("successMsg", "Completion confirmed. Awaiting other party's confirmation.");
        return "redirect:/sessions/" + id;
    }

    @PostMapping("/{id}/meeting-link")
    public String updateMeetingLink(@PathVariable Long id,
                                    @RequestParam String meetingLink,
                                    RedirectAttributes ra) {
        try {
            sessionService.updateMeetingLink(id, securityUtils.getCurrentUserId(), meetingLink);
            ra.addFlashAttribute("successMsg", "Meeting link saved.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/sessions/" + id;
    }

    @PostMapping("/{id}/messages")
    public String addMessage(@PathVariable Long id,
                             @RequestParam String messageText,
                             RedirectAttributes ra) {
        try {
            sessionService.addMessage(id, securityUtils.getCurrentUserId(), messageText);
            ra.addFlashAttribute("successMsg", "Message sent.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/sessions/" + id;
    }

    @PostMapping("/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @Valid @ModelAttribute ReviewDto reviewDto,
                               BindingResult result,
                               RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("reviewError", "Please provide a valid rating (1–5).");
            return "redirect:/sessions/" + id;
        }
        reviewService.submitReview(id, securityUtils.getCurrentUserId(), reviewDto);
        ra.addFlashAttribute("successMsg", "Review submitted! Thank you.");
        return "redirect:/sessions/" + id;
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<Resource> downloadReceipt(@PathVariable Long id) {
        Session session = sessionService.getSession(id);
        if (session.getReceiptPath() == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File("./receipts/" + session.getReceiptPath());
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + session.getReceiptPath() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
