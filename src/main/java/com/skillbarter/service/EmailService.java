package com.skillbarter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@skillbarter.app}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "SkillBarter - Password Reset Request";
        String body = String.format(
                "Hello,\n\n" +
                "You requested a password reset for your SkillBarter account.\n\n" +
                "Click the link below to reset your password:\n" +
                "http://localhost:8080/auth/reset-password?token=%s\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "SkillBarter Team",
                resetToken
        );
        sendEmail(to, subject, body);
    }

    public void sendSessionReminderEmail(String to, String sessionDetails) {
        String subject = "SkillBarter - Session Reminder";
        String body = String.format(
                "Hello,\n\n" +
                "This is a reminder about your upcoming session:\n\n" +
                "%s\n\n" +
                "Best regards,\n" +
                "SkillBarter Team",
                sessionDetails
        );
        sendEmail(to, subject, body);
    }
}
