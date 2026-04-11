package com.skillbarter.service;

import com.skillbarter.entity.*;
import com.skillbarter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Activity Feed Service - Generate social timeline of user activities
 * 
 * SOLID - SRP: Only handles activity feed generation
 * SOLID - OCP: Easy to add new activity types
 */
@Service
@RequiredArgsConstructor
public class ActivityFeedService {

    private final SessionRepository sessionRepository;
    private final SkillRepository skillRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserRepository userRepository;

    /**
     * Activity item for feed
     */
    public static class ActivityItem {
        private String type; // SESSION_COMPLETED, SKILL_ADDED, BADGE_EARNED, etc.
        private String icon;
        private String title;
        private String description;
        private LocalDateTime timestamp;
        private String actionUrl;
        private String actorName;
        private String actorId;

        public ActivityItem(String type, String icon, String title, String description, 
                          LocalDateTime timestamp, String actionUrl, String actorName, String actorId) {
            this.type = type;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.actionUrl = actionUrl;
            this.actorName = actorName;
            this.actorId = actorId;
        }

        // Getters
        public String getType() { return type; }
        public String getIcon() { return icon; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getActionUrl() { return actionUrl; }
        public String getActorName() { return actorName; }
        public String getActorId() { return actorId; }
    }

    /**
     * Get activity feed for a user
     */
    public List<ActivityItem> getUserActivityFeed(Long userId, int limit) {
        List<ActivityItem> activities = new ArrayList<>();

        // Recent sessions
        List<Session> sessions = sessionRepository.findAllByUserId(userId).stream()
            .limit(10)
            .collect(Collectors.toList());

        for (Session session : sessions) {
            String title = session.getStatus().name().equals("COMPLETED") 
                ? "Completed session" 
                : "Session " + session.getStatus().name().toLowerCase();
            
            String description = session.getSkill().getName() + " with " + 
                (session.getTeacher().getId().equals(userId) 
                    ? session.getLearner().getUsername() 
                    : session.getTeacher().getUsername());

            activities.add(new ActivityItem(
                "SESSION",
                "📚",
                title,
                description,
                session.getCreatedAt(),
                "/sessions/" + session.getId(),
                session.getTeacher().getUsername(),
                session.getTeacher().getId().toString()
            ));
        }

        // Recent skills added
        List<Skill> skills = skillRepository.findByUserId(userId).stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .collect(Collectors.toList());

        for (Skill skill : skills) {
            activities.add(new ActivityItem(
                "SKILL_ADDED",
                skill.getIsOffering() ? "🎓" : "📖",
                skill.getIsOffering() ? "Added skill to teach" : "Added skill to learn",
                skill.getName() + " - " + skill.getCategory(),
                skill.getCreatedAt(),
                "/skills/" + skill.getId(),
                skill.getUser().getUsername(),
                skill.getUser().getId().toString()
            ));
        }

        // Sort by timestamp descending
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return activities.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Get platform-wide activity feed (public activities)
     */
    public List<ActivityItem> getPlatformActivityFeed(int limit) {
        List<ActivityItem> activities = new ArrayList<>();

        // Recent completed sessions
        List<Session> sessions = sessionRepository.findAll().stream()
            .filter(s -> s.getCompletedAt() != null)
            .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
            .limit(20)
            .collect(Collectors.toList());

        for (Session session : sessions) {
            activities.add(new ActivityItem(
                "SESSION_COMPLETED",
                "✅",
                session.getLearner().getUsername() + " learned " + session.getSkill().getName(),
                "Taught by " + session.getTeacher().getUsername(),
                session.getCompletedAt(),
                "/skills/" + session.getSkill().getId(),
                session.getLearner().getUsername(),
                session.getLearner().getId().toString()
            ));
        }

        // Recent skills added
        List<Skill> skills = skillRepository.findByIsOfferingTrue().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(20)
            .collect(Collectors.toList());

        for (Skill skill : skills) {
            activities.add(new ActivityItem(
                "NEW_TEACHER",
                "🌟",
                skill.getUser().getUsername() + " is now teaching " + skill.getName(),
                skill.getCategory() + " • " + skill.getHourlyRate() + " credits/hour",
                skill.getCreatedAt(),
                "/skills/" + skill.getId(),
                skill.getUser().getUsername(),
                skill.getUser().getId().toString()
            ));
        }

        // Sort by timestamp descending
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return activities.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Get trending activities (most popular in last 24 hours)
     */
    public List<ActivityItem> getTrendingActivities(int limit) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<ActivityItem> activities = new ArrayList<>();

        // Most booked skills in last 24 hours
        List<Skill> trendingSkills = skillRepository.findTrendingSkills(LocalDateTime.now().minusDays(1));

        for (Skill skill : trendingSkills) {
            activities.add(new ActivityItem(
                "TRENDING",
                "🔥",
                skill.getName() + " is trending",
                "Popular in " + skill.getCategory(),
                LocalDateTime.now(),
                "/skills/" + skill.getId(),
                skill.getUser().getUsername(),
                skill.getUser().getId().toString()
            ));
        }

        return activities.stream().limit(limit).collect(Collectors.toList());
    }
}
