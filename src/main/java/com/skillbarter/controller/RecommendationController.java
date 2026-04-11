package com.skillbarter.controller;

import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/recommendations")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String recommendations(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        
        model.addAttribute("skillsToLearn", recommendationService.recommendSkillsToLearn(userId, 12));
        model.addAttribute("teachers", recommendationService.recommendTeachers(userId, 8));
        model.addAttribute("personalized", recommendationService.getPersonalizedRecommendations(userId));
        
        return "recommendations/index";
    }

    @GetMapping("/api/skills")
    @ResponseBody
    public Object getSkillRecommendationsApi(@RequestParam(defaultValue = "12") int limit) {
        Long userId = securityUtils.getCurrentUserId();
        return recommendationService.recommendSkillsToLearn(userId, limit);
    }

    @GetMapping("/api/teachers")
    @ResponseBody
    public Object getTeacherRecommendationsApi(@RequestParam(defaultValue = "8") int limit) {
        Long userId = securityUtils.getCurrentUserId();
        return recommendationService.recommendTeachers(userId, limit);
    }

    @GetMapping("/api/complementary/{skillId}")
    @ResponseBody
    public Object getComplementarySkillsApi(@PathVariable Long skillId, 
                                           @RequestParam(defaultValue = "6") int limit) {
        return recommendationService.recommendComplementarySkills(skillId, limit);
    }
}
