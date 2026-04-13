package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.GeminiAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
// Handles AI chatbot interactions and responses
/**
 * AI Chat Controller — Gemini-powered SkillBot assistant.
 *
 * Provides both a full chat page and a REST endpoint for AJAX calls.
 * Strategy Pattern: GeminiAiService can be swapped for any AI provider.
 *
 * SOLID – SRP: only handles AI chat HTTP layer.
 */
@Controller
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final GeminiAiService geminiAiService;
    private final SecurityUtils securityUtils;

    /** Full chat page */
    @GetMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public String chatPage(Model model) {
        model.addAttribute("user", securityUtils.getCurrentUser());
        return "ai/chat";
    }

    /** AJAX endpoint — returns plain text response */
    @PostMapping("/chat/ask")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "").trim();
        if (message.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("reply", "Please type a message."));
        }
        String reply = geminiAiService.chat(message);
        return ResponseEntity.ok(Map.of("reply", reply));
    }

    /** Skill description improvement — called from skill add form */
    @PostMapping("/improve-description")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, String>> improveDescription(
            @RequestBody Map<String, String> body) {
        String skillName = body.getOrDefault("skillName", "");
        String description = body.getOrDefault("description", "");
        String improved = geminiAiService.improveSkillDescription(skillName, description);
        return ResponseEntity.ok(Map.of("improved", improved));
    }

    /** Skill suggestions — called from dashboard */
    @PostMapping("/suggest-skills")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, String>> suggestSkills(
            @RequestBody Map<String, String> body) {
        String interests = body.getOrDefault("interests", "");
        String currentSkills = body.getOrDefault("currentSkills", "");
        String suggestions = geminiAiService.suggestSkills(interests, currentSkills);
        return ResponseEntity.ok(Map.of("suggestions", suggestions));
    }
}
