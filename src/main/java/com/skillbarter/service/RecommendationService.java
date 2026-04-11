package com.skillbarter.service;

import com.skillbarter.entity.*;
import com.skillbarter.enums.SkillCategory;
import com.skillbarter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommendation Service - AI-powered skill and teacher recommendations
 * 
 * SOLID - SRP: Only handles recommendations
 * SOLID - OCP: Easy to add new recommendation algorithms
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SkillRepository skillRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    /**
     * Recommend skills to learn based on user's interests
     */
    public List<Skill> recommendSkillsToLearn(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();

        // Get user's current skills
        List<Skill> userSkills = skillRepository.findByUserId(userId);
        Set<SkillCategory> userCategories = userSkills.stream()
            .map(Skill::getCategory)
            .collect(Collectors.toSet());

        // Get user's session history
        List<Session> userSessions = sessionRepository.findAllByUserId(userId);
        Set<SkillCategory> sessionCategories = userSessions.stream()
            .map(s -> s.getSkill().getCategory())
            .collect(Collectors.toSet());

        // Combine categories
        Set<SkillCategory> interestedCategories = new HashSet<>();
        interestedCategories.addAll(userCategories);
        interestedCategories.addAll(sessionCategories);

        // Find skills in interested categories that user doesn't have
        List<Skill> recommendations = new ArrayList<>();
        for (SkillCategory category : interestedCategories) {
            List<Skill> categorySkills = skillRepository.findByCategoryAndIsOfferingTrue(category);
            
            // Filter out skills user already has
            Set<String> userSkillNames = userSkills.stream()
                .map(s -> s.getName().toLowerCase())
                .collect(Collectors.toSet());

            categorySkills = categorySkills.stream()
                .filter(s -> !userSkillNames.contains(s.getName().toLowerCase()))
                .filter(s -> !s.getUser().getId().equals(userId))
                .collect(Collectors.toList());

            recommendations.addAll(categorySkills);
        }

        // Sort by rating and popularity
        recommendations.sort((a, b) -> {
            double scoreA = (a.getAverageRating() != null ? a.getAverageRating() : 0.0) * 10 + 
                           (a.getTotalSessions() != null ? a.getTotalSessions() : 0);
            double scoreB = (b.getAverageRating() != null ? b.getAverageRating() : 0.0) * 10 + 
                           (b.getTotalSessions() != null ? b.getTotalSessions() : 0);
            return Double.compare(scoreB, scoreA);
        });

        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Recommend teachers based on user preferences
     */
    public List<User> recommendTeachers(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();

        // Get skills user wants to learn
        List<Skill> wantedSkills = skillRepository.findByUserId(userId).stream()
            .filter(s -> !s.getIsOffering())
            .collect(Collectors.toList());

        Set<String> wantedSkillNames = wantedSkills.stream()
            .map(s -> s.getName().toLowerCase())
            .collect(Collectors.toSet());

        // Find teachers who teach those skills
        List<User> teachers = new ArrayList<>();
        for (String skillName : wantedSkillNames) {
            List<Skill> matchingSkills = skillRepository.findMatchingOfferedSkills(skillName, userId);
            teachers.addAll(matchingSkills.stream()
                .map(Skill::getUser)
                .collect(Collectors.toList()));
        }

        // Remove duplicates and sort by reputation
        teachers = teachers.stream()
            .distinct()
            .sorted((a, b) -> Integer.compare(
                b.getReputationScore() != null ? b.getReputationScore() : 0,
                a.getReputationScore() != null ? a.getReputationScore() : 0
            ))
            .collect(Collectors.toList());

        return teachers.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Recommend complementary skills (skills that go well together)
     */
    public List<Skill> recommendComplementarySkills(Long skillId, int limit) {
        Skill skill = skillRepository.findById(skillId).orElse(null);
        if (skill == null) return List.of();

        // Find skills in the same category
        List<Skill> complementary = skillRepository.findByCategoryAndIsOfferingTrue(skill.getCategory());

        // Remove the original skill
        complementary = complementary.stream()
            .filter(s -> !s.getId().equals(skillId))
            .collect(Collectors.toList());

        // Sort by rating
        complementary.sort((a, b) -> {
            double ratingA = a.getAverageRating() != null ? a.getAverageRating() : 0.0;
            double ratingB = b.getAverageRating() != null ? b.getAverageRating() : 0.0;
            return Double.compare(ratingB, ratingA);
        });

        return complementary.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Get personalized homepage recommendations
     */
    public Map<String, List<Skill>> getPersonalizedRecommendations(Long userId) {
        Map<String, List<Skill>> recommendations = new HashMap<>();

        recommendations.put("forYou", recommendSkillsToLearn(userId, 6));
        recommendations.put("trending", skillRepository.findTrendingSkills(LocalDateTime.now().minusDays(7)).stream().limit(6).collect(Collectors.toList()));
        recommendations.put("topRated", skillRepository.findByIsOfferingTrue().stream()
            .sorted((a, b) -> {
                double ratingA = a.getAverageRating() != null ? a.getAverageRating() : 0.0;
                double ratingB = b.getAverageRating() != null ? b.getAverageRating() : 0.0;
                return Double.compare(ratingB, ratingA);
            })
            .limit(6)
            .collect(Collectors.toList()));

        return recommendations;
    }
}
