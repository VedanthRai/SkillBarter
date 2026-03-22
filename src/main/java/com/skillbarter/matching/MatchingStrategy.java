package com.skillbarter.matching;

import com.skillbarter.entity.Skill;
import com.skillbarter.entity.User;

import java.util.List;

/**
 * Strategy Pattern (Behavioral) — Smart Matching Engine
 *
 * SOLID – OCP: new matching algorithms can be added by implementing
 *              this interface without touching existing code.
 * SOLID – DIP: MatchingService depends on this abstraction,
 *              not on concrete implementations.
 *
 * Used in: Major Feature 2 — Skill Request & Smart Matching Engine
 */
public interface MatchingStrategy {

    /**
     * Given a learner and the skill they want to learn,
     * return a ranked list of matching teacher skills.
     *
     * @param learner      the user seeking to learn
     * @param wantedSkill  the skill name/category the learner wants
     * @param allOffered   all skills currently offered on the platform
     * @return ranked list (best match first)
     */
    List<Skill> match(User learner, String wantedSkill, List<Skill> allOffered);

    /** Human-readable name for display in UI */
    String getStrategyName();
}
