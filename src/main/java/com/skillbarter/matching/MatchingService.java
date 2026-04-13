// Vijay: Context class for Strategy Pattern
// Selects appropriate matching strategy (rating, affordability, verified)
// Implements Open/Closed Principle by allowing new strategies without modifying this class
package com.skillbarter.matching;

import com.skillbarter.entity.Skill;
import com.skillbarter.entity.User;
import com.skillbarter.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Context class for the Strategy Pattern.
 *
 * Holds all MatchingStrategy implementations injected by Spring
 * and delegates matching calls to the selected strategy.
 *
 * SOLID – DIP: depends on MatchingStrategy abstraction.
 * SOLID – OCP: new strategies require zero changes to this class.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final SkillRepository skillRepository;
    private final RatingBasedMatchingStrategy ratingBasedStrategy;
    private final AffordabilityMatchingStrategy affordabilityStrategy;
    private final VerifiedOnlyMatchingStrategy verifiedOnlyStrategy;

    @Value("${app.matching.max.suggestions:5}")
    private int maxSuggestions;

    /**
     * Execute matching with a named strategy bean.
     */
    public List<Skill> findMatches(User learner, String wantedSkill, String strategyName) {
        MatchingStrategy strategy = resolveStrategy(strategyName);
        List<Skill> allOffered = skillRepository.findByIsOfferingTrue();
        return strategy.match(learner, wantedSkill, allOffered)
                       .stream().limit(maxSuggestions).toList();
    }

    /**
     * Return the full ranked list for discovery/search pages.
     */
    public List<Skill> findAllMatches(User learner, String wantedSkill, String strategyName) {
        MatchingStrategy strategy = resolveStrategy(strategyName);
        List<Skill> allOffered = skillRepository.findByIsOfferingTrue();
        return strategy.match(learner, wantedSkill, allOffered);
    }

    /**
     * Default matching using rating-based strategy.
     */
    public List<Skill> findMatchesAllStrategies(User learner, String wantedSkill) {
        return findMatches(learner, wantedSkill, "ratingBasedStrategy");
    }

    /**
     * Returns a map of strategy bean names to strategy instances for UI display.
     */
    public Map<String, MatchingStrategy> getAvailableStrategies() {
        Map<String, MatchingStrategy> map = new LinkedHashMap<>();
        map.put("ratingBasedStrategy",   ratingBasedStrategy);
        map.put("affordabilityStrategy", affordabilityStrategy);
        map.put("verifiedOnlyStrategy",  verifiedOnlyStrategy);
        return map;
    }

    private MatchingStrategy resolveStrategy(String name) {
        return switch (name) {
            case "affordabilityStrategy"  -> affordabilityStrategy;
            case "verifiedOnlyStrategy"   -> verifiedOnlyStrategy;
            default                       -> ratingBasedStrategy;
        };
    }
}
