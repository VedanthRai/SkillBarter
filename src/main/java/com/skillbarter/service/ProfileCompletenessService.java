package com.skillbarter.service;

import com.skillbarter.entity.User;
import com.skillbarter.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileCompletenessService {

    private final SkillRepository skillRepository;

    /**
     * Calculate profile completeness percentage (0-100)
     */
    public int calculateCompleteness(User user) {
        int score = 0;
        int maxScore = 100;

        // Basic info (30 points)
        if (user.getUsername() != null && !user.getUsername().isEmpty()) score += 5;
        if (user.getEmail() != null && !user.getEmail().isEmpty()) score += 5;
        if (user.getBio() != null && !user.getBio().isEmpty()) score += 10;
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) score += 10;

        // Contact info (20 points)
        if (user.getTimezone() != null && !user.getTimezone().isEmpty()) score += 10;
        if (user.getLocation() != null && !user.getLocation().isEmpty()) score += 10;

        // Skills (30 points)
        long skillsOffered = skillRepository.countByUserIdAndIsOffering(user.getId(), true);
        long skillsWanted = skillRepository.countByUserIdAndIsOffering(user.getId(), false);
        if (skillsOffered > 0) score += 15;
        if (skillsWanted > 0) score += 15;

        // Activity (20 points)
        if (user.getTotalSessionsCompleted() != null && user.getTotalSessionsCompleted() > 0) score += 10;
        if (user.getAverageRating() != null && user.getAverageRating() > 0) score += 10;

        return Math.min(score, maxScore);
    }

    /**
     * Get missing profile items
     */
    public Map<String, String> getMissingItems(User user) {
        Map<String, String> missing = new HashMap<>();

        if (user.getBio() == null || user.getBio().isEmpty()) {
            missing.put("bio", "Add a bio to tell others about yourself");
        }
        if (user.getProfilePicture() == null || user.getProfilePicture().isEmpty()) {
            missing.put("picture", "Upload a profile picture");
        }
        if (user.getTimezone() == null || user.getTimezone().isEmpty()) {
            missing.put("timezone", "Set your timezone");
        }
        if (user.getLocation() == null || user.getLocation().isEmpty()) {
            missing.put("location", "Add your location");
        }

        long skillsOffered = skillRepository.countByUserIdAndIsOffering(user.getId(), true);
        if (skillsOffered == 0) {
            missing.put("skills_offered", "Add skills you can teach");
        }

        long skillsWanted = skillRepository.countByUserIdAndIsOffering(user.getId(), false);
        if (skillsWanted == 0) {
            missing.put("skills_wanted", "Add skills you want to learn");
        }

        if (user.getTotalSessionsCompleted() == null || user.getTotalSessionsCompleted() == 0) {
            missing.put("sessions", "Complete your first session");
        }

        return missing;
    }

    /**
     * Get completion level (Beginner, Intermediate, Expert)
     */
    public String getCompletionLevel(int percentage) {
        if (percentage >= 90) return "Expert";
        if (percentage >= 70) return "Advanced";
        if (percentage >= 50) return "Intermediate";
        if (percentage >= 30) return "Beginner";
        return "Getting Started";
    }
}
