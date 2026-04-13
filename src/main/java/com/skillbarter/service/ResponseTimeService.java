package com.skillbarter.service;

import com.skillbarter.entity.Session;
import com.skillbarter.entity.User;
import com.skillbarter.enums.SessionStatus;
import com.skillbarter.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
// Tracks and calculates response time metrics
/**
 * Response Time Service - Track how quickly teachers respond to booking requests
 * 
 * SOLID - SRP: Only handles response time calculations
 */
@Service
@RequiredArgsConstructor
public class ResponseTimeService {

    private final SessionRepository sessionRepository;

    /**
     * Calculate average response time for a teacher (in hours)
     */
    public Double calculateAverageResponseTime(Long teacherId) {
        List<Session> sessions = sessionRepository.findByTeacherId(teacherId);
        
        List<Duration> responseTimes = sessions.stream()
            .filter(s -> s.getStatus() == SessionStatus.ACCEPTED || s.getStatus() == SessionStatus.REJECTED)
            .filter(s -> s.getCreatedAt() != null && s.getUpdatedAt() != null)
            .map(s -> Duration.between(s.getCreatedAt(), s.getUpdatedAt()))
            .toList();

        if (responseTimes.isEmpty()) {
            return null;
        }

        long totalMinutes = responseTimes.stream()
            .mapToLong(Duration::toMinutes)
            .sum();

        double avgMinutes = (double) totalMinutes / responseTimes.size();
        return avgMinutes / 60.0; // Convert to hours
    }

    /**
     * Get response time category
     */
    public String getResponseTimeCategory(Double hours) {
        if (hours == null) return "No data";
        if (hours < 1) return "Lightning fast (< 1 hour)";
        if (hours < 6) return "Very fast (< 6 hours)";
        if (hours < 24) return "Fast (< 24 hours)";
        if (hours < 48) return "Moderate (1-2 days)";
        return "Slow (> 2 days)";
    }

    /**
     * Get response time badge
     */
    public String getResponseTimeBadge(Double hours) {
        if (hours == null) return "";
        if (hours < 1) return "⚡ Lightning Response";
        if (hours < 6) return "🚀 Quick Response";
        if (hours < 24) return "✅ Responsive";
        return "";
    }

    /**
     * Calculate acceptance rate for a teacher
     */
    public Double calculateAcceptanceRate(Long teacherId) {
        List<Session> sessions = sessionRepository.findByTeacherId(teacherId);
        
        long total = sessions.stream()
            .filter(s -> s.getStatus() == SessionStatus.ACCEPTED || 
                        s.getStatus() == SessionStatus.REJECTED ||
                        s.getStatus() == SessionStatus.COMPLETED)
            .count();

        if (total == 0) return null;

        long accepted = sessions.stream()
            .filter(s -> s.getStatus() == SessionStatus.ACCEPTED || 
                        s.getStatus() == SessionStatus.COMPLETED)
            .count();

        return (double) accepted / total * 100.0;
    }

    /**
     * Calculate completion rate for a teacher
     */
    public Double calculateCompletionRate(Long teacherId) {
        List<Session> sessions = sessionRepository.findByTeacherId(teacherId);
        
        long accepted = sessions.stream()
            .filter(s -> s.getStatus() == SessionStatus.ACCEPTED || 
                        s.getStatus() == SessionStatus.COMPLETED)
            .count();

        if (accepted == 0) return null;

        long completed = sessions.stream()
            .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
            .count();

        return (double) completed / accepted * 100.0;
    }
}
