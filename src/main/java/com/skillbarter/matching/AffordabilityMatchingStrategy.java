// Vijay: Matches users based on lowest cost (time credits)
// Helps users find affordable learning options
package com.skillbarter.matching;

import com.skillbarter.entity.Skill;
import com.skillbarter.entity.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Strategy: ranks matching teachers by hourly rate (cheapest first).
 * Ideal for learners with limited credit balance.
 *
 * Strategy Pattern — Concrete Strategy B
 */
@Component("affordabilityStrategy")
public class AffordabilityMatchingStrategy implements MatchingStrategy {

    @Override
    public List<Skill> match(User learner, String wantedSkill, List<Skill> allOffered) {
        String keyword = wantedSkill.toLowerCase().trim();
        return allOffered.stream()
                .filter(s -> s.getUser().isActive())
                .filter(s -> s.getName().toLowerCase().contains(keyword) || 
                           s.getDescription().toLowerCase().contains(keyword))
                // learner must be able to afford at least 1 hour
                .filter(s -> learner.getCreditBalance().compareTo(s.getHourlyRate()) >= 0)
                .sorted(Comparator.comparing(Skill::getHourlyRate)
                        .thenComparingDouble(Skill::getAverageRating).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "Most Affordable";
    }
}
