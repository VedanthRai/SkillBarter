// Vijay: Matches users based on highest ratings
// Improves quality of recommendations by prioritizing experienced users
package com.skillbarter.matching;

import com.skillbarter.entity.Skill;
import com.skillbarter.entity.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Strategy: ranks matching teachers by their average rating.
 * Teachers with higher ratings appear first.
 *
 * Strategy Pattern — Concrete Strategy A
 */
@Component("ratingBasedStrategy")
public class RatingBasedMatchingStrategy implements MatchingStrategy {

    @Override
    public List<Skill> match(User learner, String wantedSkill, List<Skill> allOffered) {
        String keyword = wantedSkill.toLowerCase().trim();
        return allOffered.stream()
                // only active teachers
                .filter(s -> s.getUser().isActive())
                // name or description must contain the wanted keyword
                .filter(s -> s.getName().toLowerCase().contains(keyword) || 
                           s.getDescription().toLowerCase().contains(keyword))
                // sort by average rating descending, then total sessions descending
                .sorted(Comparator
                        .comparingDouble(Skill::getAverageRating).reversed()
                        .thenComparingInt(Skill::getTotalSessions).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "Top-Rated Teachers";
    }
}
