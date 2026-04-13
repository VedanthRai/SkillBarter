package com.skillbarter.controller;

import com.skillbarter.entity.User;
import com.skillbarter.security.SecurityUtils;
import com.skillbarter.service.ProfileCompletenessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ProfileCompletenessController {

    private final SecurityUtils securityUtils;
    private final ProfileCompletenessService profileCompletenessService;

    @GetMapping("/completeness")
    public String profileCompleteness(Model model) {
        User user = securityUtils.getCurrentUser();
        
        int completeness = profileCompletenessService.calculateCompleteness(user);
        String level = profileCompletenessService.getCompletionLevel(completeness);
        Map<String, String> missingItems = profileCompletenessService.getMissingItems(user);

        model.addAttribute("user", user);
        model.addAttribute("completeness", completeness);
        model.addAttribute("level", level);
        model.addAttribute("missingItems", missingItems);

        return "profile/completeness";
    }

    @GetMapping("/completeness/api")
    @ResponseBody
    public Map<String, Object> getCompletenessApi() {
        User user = securityUtils.getCurrentUser();
        
        Map<String, Object> response = new HashMap<>();
        response.put("percentage", profileCompletenessService.calculateCompleteness(user));
        response.put("level", profileCompletenessService.getCompletionLevel(
            profileCompletenessService.calculateCompleteness(user)));
        response.put("missingItems", profileCompletenessService.getMissingItems(user));
        return response;
    }
}
