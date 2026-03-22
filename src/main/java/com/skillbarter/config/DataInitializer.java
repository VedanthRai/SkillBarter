package com.skillbarter.config;

import com.skillbarter.dto.SessionRequestDto;
import com.skillbarter.entity.Session;
import com.skillbarter.entity.Skill;
import com.skillbarter.entity.User;
import com.skillbarter.enums.Role;
import com.skillbarter.enums.SkillCategory;
import com.skillbarter.enums.UserStatus;
import com.skillbarter.repository.SessionRepository;
import com.skillbarter.repository.SkillRepository;
import com.skillbarter.repository.UserRepository;
import com.skillbarter.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("DataInitializer: ensuring demo data is present...");

        User admin = ensureUser("admin", "admin@skillbarter.app",
                "Admin@1234", Role.ROLE_ADMIN,
                "Platform administrator.", BigDecimal.valueOf(100), 0, 10, 0);

        User verifier = ensureUser("verifier1", "verifier@skillbarter.app",
                "Verify@1234", Role.ROLE_VERIFIER,
                "Dispute tribunal verifier.", BigDecimal.valueOf(50), 0, 5, 0);

        User alice = ensureUser("alice_dev", "alice@example.com",
                "Test@1234", Role.ROLE_USER,
                "Full-stack dev, 5 years Java and Python. Loves teaching beginners.",
                BigDecimal.valueOf(15), 7, 45, 12);

        User bob = ensureUser("bob_music", "bob@example.com",
                "Test@1234", Role.ROLE_USER,
                "Classical guitarist and music theory enthusiast.",
                BigDecimal.valueOf(8), 3, 20, 5);

        User charlie = ensureUser("charlie_lang", "charlie@example.com",
                "Test@1234", Role.ROLE_USER,
                "Trilingual in English, Spanish, and French. Business communication specialist.",
                BigDecimal.valueOf(20), 14, 80, 22);

        Skill python = ensureSkill(buildSkill(
                "Python Programming", SkillCategory.TECHNOLOGY,
                "Learn Python from scratch with projects, OOP, and file handling.",
                BigDecimal.valueOf(2.0), "ADVANCED", true, alice, true, 4.8, 10));

        Skill springBoot = ensureSkill(buildSkill(
                "Java Spring Boot", SkillCategory.TECHNOLOGY,
                "Build production-ready REST APIs with Spring Boot, JPA, and security.",
                BigDecimal.valueOf(2.5), "EXPERT", true, alice, true, 4.9, 8));

        Skill guitar = ensureSkill(buildSkill(
                "Classical Guitar", SkillCategory.MUSIC,
                "Acoustic guitar for all levels with scales, chords, and fingerpicking.",
                BigDecimal.valueOf(1.5), "INTERMEDIATE", false, bob, false, 4.5, 4));

        Skill spanish = ensureSkill(buildSkill(
                "Spanish Conversation", SkillCategory.LANGUAGES,
                "Conversational Spanish for travel, business, and daily life.",
                BigDecimal.valueOf(1.0), "ADVANCED", false, charlie, true, 4.7, 18));

        ensureSkill(buildSkill(
                "French for Beginners", SkillCategory.LANGUAGES,
                "Pronunciation, greetings, basic grammar, and survival phrases.",
                BigDecimal.valueOf(1.0), "INTERMEDIATE", false, charlie, true, 4.6, 12));

        ensureSkill(buildSkill(
                "Data Structures and Algorithms", SkillCategory.ACADEMICS,
                "Arrays, linked lists, trees, graphs, sorting, and interview practice.",
                BigDecimal.valueOf(2.0), "ADVANCED", false, alice, false, 0.0, 0));

        ensureSkill(buildSkill(
                "Music Theory", SkillCategory.MUSIC,
                "Scales, intervals, chords, rhythm, and ear training for any instrument.",
                BigDecimal.valueOf(1.5), "INTERMEDIATE", false, bob, true, 4.3, 3));

        ensureDemoSession(
                alice.getId(),
                guitar.getId(),
                bob.getId(),
                LocalDateTime.now().plusDays(2).withHour(18).withMinute(0).withSecond(0).withNano(0),
                90,
                "[DEMO] Alice wants to practice fingerstyle basics before the weekend showcase.",
                true,
                false,
                "https://meet.google.com/demo-guitar-session",
                new String[] {
                        "Hi Bob, I would like to focus on fingerpicking and chord transitions.",
                        "Sounds good. I added a Meet link and I will share a warm-up exercise there.",
                        "Perfect, I will join 10 minutes early to test audio."
                });

        ensureDemoSession(
                bob.getId(),
                springBoot.getId(),
                alice.getId(),
                LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0),
                60,
                "[DEMO] Bob wants help understanding Spring Security before his interview.",
                false,
                false,
                null,
                new String[0]);

        ensureDemoSession(
                alice.getId(),
                spanish.getId(),
                charlie.getId(),
                LocalDateTime.now().plusDays(4).withHour(19).withMinute(30).withSecond(0).withNano(0),
                60,
                "[DEMO] Alice wants a travel-focused Spanish practice session for her upcoming trip.",
                true,
                false,
                "https://zoom.us/j/demo-spanish-practice",
                new String[] {
                        "Hola Charlie, can we focus on airport and hotel conversations?",
                        "Absolutely. I will prepare a travel phrase sheet for us."
                });

        log.info("DataInitializer: demo users, skills, and sessions are ready.");
        log.info("Admin    -> admin@skillbarter.app / Admin@1234");
        log.info("Verifier -> verifier@skillbarter.app / Verify@1234");
        log.info("Alice    -> alice@example.com / Test@1234");
        log.info("Bob      -> bob@example.com / Test@1234");
        log.info("Charlie  -> charlie@example.com / Test@1234");
    }

    private User ensureUser(String username, String email, String rawPassword,
                            Role role, String bio, BigDecimal credits, int streak,
                            int reputationScore, int completedSessions) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setBio(bio);
        user.setEmailVerified(true);

        if (user.getCreditBalance() == null || user.getCreditBalance().compareTo(credits) < 0) {
            user.setCreditBalance(credits);
        }
        if (user.getEscrowBalance() == null) {
            user.setEscrowBalance(BigDecimal.ZERO);
        }

        user.setStreakCount(Math.max(defaultInt(user.getStreakCount()), streak));
        user.setReputationScore(Math.max(defaultInt(user.getReputationScore()), reputationScore));
        user.setTotalSessionsCompleted(Math.max(defaultInt(user.getTotalSessionsCompleted()), completedSessions));
        return userRepository.save(user);
    }

    private Skill ensureSkill(Skill candidate) {
        return skillRepository.findByUserId(candidate.getUser().getId()).stream()
                .filter(existing -> existing.getName().equalsIgnoreCase(candidate.getName()))
                .findFirst()
                .map(existing -> {
                    existing.setDescription(candidate.getDescription());
                    existing.setCategory(candidate.getCategory());
                    existing.setHourlyRate(candidate.getHourlyRate());
                    existing.setProficiencyLevel(candidate.getProficiencyLevel());
                    existing.setCertificatePath(candidate.getCertificatePath());
                    existing.setVerified(candidate.getVerified());
                    existing.setIsOffering(candidate.getIsOffering());
                    existing.setAverageRating(candidate.getAverageRating());
                    existing.setTotalRatings(candidate.getTotalRatings());
                    existing.setTotalSessions(candidate.getTotalSessions());
                    return skillRepository.save(existing);
                })
                .orElseGet(() -> skillRepository.save(candidate));
    }

    private void ensureDemoSession(Long learnerId, Long skillId, Long teacherId,
                                   LocalDateTime scheduledAt, int durationMinutes,
                                   String demoNote, boolean accepted, boolean started,
                                   String meetingLink, String[] messages) {
        if (sessionRepository.existsByLearnerIdAndTeacherIdAndSkillIdAndLearnerNotes(
                learnerId, teacherId, skillId, demoNote)) {
            return;
        }

        Session session = sessionService.requestSession(learnerId, SessionRequestDto.builder()
                .skillId(skillId)
                .scheduledAt(scheduledAt)
                .durationMinutes(durationMinutes)
                .notes(demoNote)
                .build());

        if (accepted) {
            session = sessionService.acceptSession(session.getId(), teacherId);
        }
        if (meetingLink != null && !meetingLink.isBlank()) {
            sessionService.updateMeetingLink(session.getId(), teacherId, meetingLink);
        }
        if (started) {
            sessionService.startSession(session.getId(), teacherId);
        }

        for (int i = 0; i < messages.length; i++) {
            Long authorId = (i % 2 == 0) ? learnerId : teacherId;
            sessionService.addMessage(session.getId(), authorId, messages[i]);
        }
    }

    private Skill buildSkill(String name, SkillCategory category, String description,
                             BigDecimal rate, String level, boolean certificateUploaded,
                             User user, boolean verified, double avgRating, int totalRatings) {
        return Skill.builder()
                .name(name)
                .category(category)
                .description(description)
                .hourlyRate(rate)
                .proficiencyLevel(level)
                .certificatePath(certificateUploaded ? "demo_cert_" + name.replaceAll(" ", "_") + ".pdf" : null)
                .verified(verified)
                .isOffering(true)
                .user(user)
                .averageRating(avgRating)
                .totalRatings(totalRatings)
                .totalSessions(totalRatings)
                .build();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
