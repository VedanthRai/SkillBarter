package com.skillbarter.service;

import com.skillbarter.dto.SkillDto;
import com.skillbarter.entity.Skill;
import com.skillbarter.entity.User;
import com.skillbarter.enums.SkillCategory;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Skill Service — Minor Feature 1 (Search), Major Feature 1 (Verification).
 * SOLID – SRP: skill CRUD and certificate management only.
 */
@Service
public class SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillService.class);

    private final SkillRepository skillRepository;
    private final UserService userService;

    @Autowired
    public SkillService(SkillRepository skillRepository, UserService userService) {
        this.skillRepository = skillRepository;
        this.userService = userService;
    }

    @Value("${app.upload.dir:./uploads/certificates}")
    private String uploadDir;

    @Transactional
    public Skill addSkill(Long userId, SkillDto dto, MultipartFile certificate) throws IOException {
        User user = userService.findById(userId);

        Skill skill = Skill.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .hourlyRate(dto.getHourlyRate())
                .proficiencyLevel(dto.getProficiencyLevel())
                .isOffering(dto.getIsOffering())
                .user(user)
                .build();

        if (certificate != null && !certificate.isEmpty()) {
            String path = saveCertificate(certificate, userId);
            skill.setCertificatePath(path);
        }

        return skillRepository.save(skill);
    }

    @Transactional
    public Skill updateSkill(Long skillId, Long userId, SkillDto dto) {
        Skill skill = findById(skillId);
        if (!skill.getUser().getId().equals(userId)) {
            throw new BusinessRuleException("You do not own this skill.");
        }
        skill.setName(dto.getName());
        skill.setDescription(dto.getDescription());
        skill.setCategory(dto.getCategory());
        skill.setHourlyRate(dto.getHourlyRate());
        skill.setProficiencyLevel(dto.getProficiencyLevel());
        return skillRepository.save(skill);
    }

    @Transactional
    public void deleteSkill(Long skillId, Long userId) {
        Skill skill = findById(skillId);
        if (!skill.getUser().getId().equals(userId)) {
            throw new BusinessRuleException("You do not own this skill.");
        }
        skillRepository.delete(skill);
    }

    /** Admin/Verifier: approve a skill certificate and award badge */
    @Transactional
    public Skill verifySkill(Long skillId, Long verifierId) {
        Skill skill = findById(skillId);
        if (skill.getCertificatePath() == null) {
            throw new BusinessRuleException("No certificate uploaded for this skill.");
        }
        skill.setVerified(true);
        skill = skillRepository.save(skill);
        userService.awardSkillVerificationBadge(skill.getUser().getId(), skillId, skill.getName());
        log.info("Skill {} verified by verifier {}", skillId, verifierId);
        return skill;
    }

    // ── Search (Minor Feature 1) ──────────────────────────────────────────

    public List<Skill> searchOfferedSkills(String keyword) {
        if (keyword == null || keyword.isBlank()) return skillRepository.findByIsOfferingTrue();
        return skillRepository.searchOfferedSkills(keyword.trim());
    }

    public List<Skill> getByCategory(SkillCategory category) {
        return skillRepository.findByCategoryAndIsOfferingTrue(category);
    }

    public List<Skill> getPendingVerification() {
        return skillRepository.findPendingVerification();
    }

    public List<Skill> getSkillsForUser(Long userId) {
        return skillRepository.findByUserId(userId);
    }

    public Skill findById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
    }

    public List<Skill> getAllOffered() {
        return skillRepository.findByIsOfferingTrue();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String saveCertificate(MultipartFile file, Long userId) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String filename = "cert_" + userId + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = Paths.get(uploadDir, filename);
        file.transferTo(target.toFile());
        return filename;
    }
}
