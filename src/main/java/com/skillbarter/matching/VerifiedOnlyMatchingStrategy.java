package com.skillbarter.matching;

import com.skillbarter.entity.Skill;
import com.skillbarter.entity.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Strategy: only returns teachers with a verified skill badge.
 * Highest quality assurance.
 *
 * Strategy Pattern — Concrete Strategy C
 */
@Component("verifiedOnlyStrategy")
public class VerifiedOnlyMatchingStrategy implements MatchingStrategy {

    @Override
    public List<Skill> match(User learner, String wantedSkill, List<Skill> allOffered) {
        String keyword = wantedSkill.toLowerCase().trim();
        return allOffered.stream()
                .filter(s -> s.getUser().isActive())
                .filter(s -> s.getName().toLowerCase().contains(keyword) || 
                           s.getDescription().toLowerCase().contains(keyword))
                .filter(Skill::getVerified)  // only verified skills
                .sorted(Comparator.comparingDouble(Skill::getAverageRating).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "Verified Teachers Only";
    }
}
