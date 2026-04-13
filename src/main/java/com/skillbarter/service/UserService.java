package com.skillbarter.service;

import com.skillbarter.dto.RegisterRequest;
import com.skillbarter.entity.Badge;
import com.skillbarter.entity.User;
import com.skillbarter.enums.Role;
import com.skillbarter.enums.UserStatus;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.exception.UserAlreadyExistsException;
import com.skillbarter.pattern.DomainEvents;
import com.skillbarter.pattern.UserProfileDecorator;
import com.skillbarter.repository.BadgeRepository;
import com.skillbarter.repository.ReviewRepository;
import com.skillbarter.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletService walletService;
    private ReferralService referralService;

    @Autowired
    public UserService(UserRepository userRepository,
                       BadgeRepository badgeRepository,
                       ReviewRepository reviewRepository,
                       PasswordEncoder passwordEncoder,
                       ApplicationEventPublisher eventPublisher,
                       @Lazy WalletService walletService,
                       @Lazy ReferralService referralService) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.walletService = walletService;
        this.referralService = referralService;
    }

    @Value("${app.signup.bonus.credits:5}")
    private int signupBonusCreditsInt;

    private BigDecimal getSignupBonus() {
        return BigDecimal.valueOf(signupBonusCreditsInt);
    }

    @Value("${app.upload.dir:./uploads/certificates}")
    private String uploadDir;

    // ── Registration ─────────────────────────────────────────────────────

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username taken: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .bio(request.getBio())
                .creditBalance(getSignupBonus())   // welcome bonus
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .role(Role.ROLE_USER)
                .build();

        user = userRepository.save(user);
        walletService.recordSignupBonus(user, getSignupBonus());
        
        // Process referral code if provided
        if (request.getReferralCode() != null && !request.getReferralCode().isEmpty()) {
            referralService.processReferral(request.getReferralCode(), user.getId());
        }
        
        log.info("New user registered: {} (id={})", user.getUsername(), user.getId());
        return user;
    }

    // ── Profile ──────────────────────────────────────────────────────────

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional
    public User updateProfile(Long userId, String bio, String timezone, MultipartFile avatar) throws IOException {
        User user = findById(userId);
        if (bio != null) user.setBio(bio);
        if (timezone != null && !timezone.isBlank()) user.setTimezone(timezone);
        if (avatar != null && !avatar.isEmpty()) {
            String filename = saveFile(avatar, "avatar_" + userId);
            user.setProfilePictureUrl(filename);
        }
        return userRepository.save(user);
    }

    // ── Skill Verification Badge ─────────────────────────────────────────

    @Transactional
    public void awardSkillVerificationBadge(Long userId, Long skillId, String skillName) {
        User user = findById(userId);
        if (badgeRepository.existsByUserIdAndSkillId(userId, skillId)) return;

        Badge badge = Badge.builder()
                .user(user)
                .badgeName("✅ Verified: " + skillName)
                .badgeType("SKILL_VERIFIED")
                .description("Certificate verified by a SkillBarter verifier.")
                .build();
        badge = badgeRepository.save(badge);
        log.info("Skill verification badge awarded to user {} for skill {}", userId, skillName);
        eventPublisher.publishEvent(new DomainEvents.BadgeAwardedEvent(this, badge));
    }

    @Transactional
    public void checkAndAwardStreakBadge(User user) {
        int streak = user.getStreakCount();
        if (streak == 7 || streak == 30 || streak == 100) {
            String label = streak + "-Day Streak";
            if (!badgeRepository.existsByUserIdAndBadgeType(user.getId(), "STREAK_" + streak)) {
                Badge badge = Badge.builder()
                        .user(user)
                        .badgeName("🔥 " + label)
                        .badgeType("STREAK_" + streak)
                        .description("Completed " + streak + " consecutive sessions!")
                        .build();
                badge = badgeRepository.save(badge);
                eventPublisher.publishEvent(new DomainEvents.BadgeAwardedEvent(this, badge));
            }
        }
    }

    // ── Admin ────────────────────────────────────────────────────────────

    @Transactional
    public void banUser(Long userId, String reason) {
        User user = findById(userId);
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
        log.warn("User {} banned. Reason: {}", userId, reason);
    }

    @Transactional
    public void suspendUser(Long userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    @Transactional
    public void reinstateUser(Long userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getLeaderboardByReputation() {
        return userRepository.findTopByReputationScore();
    }

    public List<User> getLeaderboardByStreak() {
        return userRepository.findTopByStreakCount();
    }

    // ── Decorator ────────────────────────────────────────────────────────

    public UserProfileDecorator buildProfileDecorator(Long userId) {
        User user = findById(userId);
        List<Badge> badges = badgeRepository.findByUserId(userId);
        Double avgRating = reviewRepository.findAverageRatingForUser(userId);
        Long totalReviews = reviewRepository.countReviewsForUser(userId);
        return new UserProfileDecorator(user, badges, avgRating, totalReviews);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private String saveFile(MultipartFile file, String prefix) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String filename = prefix + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = Paths.get(uploadDir, filename);
        file.transferTo(target.toFile());
        return filename;
    }

    public User getUserById(Long userId) {
        return findById(userId);
    }
}
