package com.skillbarter.pattern;
import com.skillbarter.entity.Badge;
import com.skillbarter.entity.User;

import java.util.List;
import java.util.stream.Collectors;


public class UserProfileDecorator {

    private final User user;
    private final List<Badge> badges;
    private final Double averageRating;
    private final Long totalReviews;

    public UserProfileDecorator(User user, List<Badge> badges,
                                Double averageRating, Long totalReviews) {
        this.user = user;
        this.badges = badges;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public User getUser() { return user; }

    public List<Badge> getBadges() { return badges; }

    public boolean hasVerifiedSkills() {
        return badges.stream().anyMatch(b -> "SKILL_VERIFIED".equals(b.getBadgeType()));
    }

    public boolean hasStreakBadge() {
        return badges.stream().anyMatch(b -> "STREAK".equals(b.getBadgeType()));
    }

    public String getTrustLabel() {
        if (averageRating == null || totalReviews == 0) return "New Member";
        if (averageRating >= 4.5 && totalReviews >= 10)  return "⭐ Top Teacher";
        if (averageRating >= 4.0)                        return "Trusted Teacher";
        if (averageRating >= 3.0)                        return "Experienced";
        return "Member";
    }

    public String getFormattedRating() {
        if (averageRating == null || totalReviews == 0) return "No ratings yet";
        return String.format("%.1f / 5.0 (%d reviews)", averageRating, totalReviews);
    }

    public List<String> getVerifiedSkillNames() {
        return badges.stream()
                .filter(b -> "SKILL_VERIFIED".equals(b.getBadgeType()) && b.getSkill() != null)
                .map(b -> b.getSkill().getName())
                .collect(Collectors.toList());
    }

    public String getStreakDisplay() {
        int streak = user.getStreakCount();
        if (streak >= 30) return "🔥 " + streak + "-day streak (Legendary!)";
        if (streak >= 7)  return "🔥 " + streak + "-day streak";
        if (streak >= 3)  return streak + "-day streak";
        return "Start your streak!";
    }

    public Double getAverageRating() { return averageRating; }

    public Long getTotalReviews() { return totalReviews; }
}
