package com.skillbarter.service;

import com.skillbarter.entity.Session;
import com.skillbarter.enums.SessionStatus;
import com.skillbarter.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Session Reminder Service
 * 
 * Sends automated reminders to reduce no-shows:
 * - 24 hours before session
 * - 1 hour before session
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionReminderService {

    private final SessionRepository sessionRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * Run every hour to check for upcoming sessions
     */
    @Scheduled(cron = "0 0 * * * *")
    public void sendSessionReminders() {
        log.info("Running session reminder check...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);
        LocalDateTime in1Hour = now.plusHours(1);
        
        // Find sessions starting in 24 hours
        List<Session> sessions24h = sessionRepository.findSessionsScheduledBetween(
            in24Hours.minusMinutes(30), 
            in24Hours.plusMinutes(30),
            SessionStatus.ACCEPTED
        );
        
        for (Session session : sessions24h) {
            send24HourReminder(session);
        }
        
        // Find sessions starting in 1 hour
        List<Session> sessions1h = sessionRepository.findSessionsScheduledBetween(
            in1Hour.minusMinutes(15), 
            in1Hour.plusMinutes(15),
            SessionStatus.ACCEPTED
        );
        
        for (Session session : sessions1h) {
            send1HourReminder(session);
        }
        
        log.info("Session reminder check completed. Sent {} 24h reminders, {} 1h reminders", 
                 sessions24h.size(), sessions1h.size());
    }

    private void send24HourReminder(Session session) {
        String subject = "Reminder: Session tomorrow with " + session.getTeacher().getUsername();
        String message = String.format(
            "Hi %s,\n\n" +
            "This is a reminder that you have a session scheduled for tomorrow:\n\n" +
            "Skill: %s\n" +
            "Teacher: %s\n" +
            "Time: %s\n" +
            "Duration: %d minutes\n" +
            "Meeting Link: %s\n\n" +
            "See you there!\n\n" +
            "SkillBarter Team",
            session.getLearner().getUsername(),
            session.getSkill().getName(),
            session.getTeacher().getUsername(),
            session.getScheduledAt(),
            session.getDurationMinutes(),
            session.getMeetingLink() != null ? session.getMeetingLink() : "To be provided"
        );
        
        emailService.sendEmail(session.getLearner().getEmail(), subject, message);
        emailService.sendEmail(session.getTeacher().getEmail(), 
            "Reminder: Teaching session tomorrow", message);
        
        log.info("Sent 24h reminder for session {}", session.getId());
    }

    private void send1HourReminder(Session session) {
        String subject = "Starting Soon: Session in 1 hour!";
        String message = String.format(
            "Hi %s,\n\n" +
            "Your session starts in 1 hour:\n\n" +
            "Skill: %s\n" +
            "Time: %s\n" +
            "Meeting Link: %s\n\n" +
            "Don't be late!\n\n" +
            "SkillBarter Team",
            session.getLearner().getUsername(),
            session.getSkill().getName(),
            session.getScheduledAt(),
            session.getMeetingLink() != null ? session.getMeetingLink() : "Check session page"
        );
        
        emailService.sendEmail(session.getLearner().getEmail(), subject, message);
        emailService.sendEmail(session.getTeacher().getEmail(), subject, message);
        
        // Also send in-app notification
        notificationService.sendAdminMessage(session.getLearner(), 
            "⏰ Your session starts in 1 hour! " + session.getSkill().getName());
        notificationService.sendAdminMessage(session.getTeacher(), 
            "⏰ Your teaching session starts in 1 hour! " + session.getSkill().getName());
        
        log.info("Sent 1h reminder for session {}", session.getId());
    }
}
