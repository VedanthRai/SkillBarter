// Vijay: Implements advanced filtering (category, rating, etc.)
// Enhances user experience by refining search results
package com.skillbarter.controller;

import com.skillbarter.dto.AdvancedSearchDto;
import com.skillbarter.entity.Skill;
import com.skillbarter.enums.SkillCategory;
import com.skillbarter.enums.VerificationLevel;
import com.skillbarter.service.AdvancedSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/skills")
@RequiredArgsConstructor
public class AdvancedSearchController {

    private final AdvancedSearchService advancedSearchService;

    @GetMapping("/advanced-search")
    public String advancedSearchPage(Model model) {
        model.addAttribute("searchDto", new AdvancedSearchDto());
        model.addAttribute("categories", SkillCategory.values());
        model.addAttribute("verificationLevels", VerificationLevel.values());
        model.addAttribute("popularSearches", advancedSearchService.getPopularSearches());
        return "skills/advanced-search";
    }

    @PostMapping("/advanced-search")
    public String performAdvancedSearch(@ModelAttribute AdvancedSearchDto searchDto, Model model) {
        List<Skill> results = advancedSearchService.advancedSearch(searchDto);
        model.addAttribute("results", results);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("categories", SkillCategory.values());
        model.addAttribute("verificationLevels", VerificationLevel.values());
        model.addAttribute("resultCount", results.size());
        return "skills/advanced-search";
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public List<String> autocomplete(@RequestParam String query) {
        return advancedSearchService.getSearchSuggestions(query);
    }

    @GetMapping("/trending")
    public String trendingSkills(Model model) {
        model.addAttribute("skills", advancedSearchService.getTrendingSkills());
        return "skills/trending";
    }
}
