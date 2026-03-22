package com.skillbarter.service;

import com.skillbarter.entity.Skill;
import com.skillbarter.entity.SkillEndorsement;
import com.skillbarter.entity.User;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.repository.SkillEndorsementRepository;
import com.skillbarter.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Endorsement Service — Peer skill endorsements.
 *
 * SOLID – SRP: manages only endorsement lifecycle.
 * SOLID – OCP: endorsement rules can be extended without modifying this class.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EndorsementService {

    private final SkillEndorsementRepository endorsementRepo;
    private final SkillRepository skillRepository;
    private final UserService userService;

    @Transactional
    public SkillEndorsement endorse(Long endorserId, Long skillId, String note) {
        User endorser = userService.findById(endorserId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new BusinessRuleException("Skill not found: " + skillId));

        if (skill.getUser().getId().equals(endorserId)) {
            throw new BusinessRuleException("You cannot endorse your own skill.");
        }
        if (endorsementRepo.existsByEndorserIdAndSkillId(endorserId, skillId)) {
            throw new BusinessRuleException("You have already endorsed this skill.");
        }

        SkillEndorsement endorsement = SkillEndorsement.builder()
                .endorser(endorser)
                .skill(skill)
                .note(note)
                .build();

        endorsement = endorsementRepo.save(endorsement);

        // Increment endorsement count on skill
        skill.setEndorsementCount(skill.getEndorsementCount() + 1);
        skillRepository.save(skill);

        log.info("User {} endorsed skill {} ({})", endorserId, skillId, skill.getName());
        return endorsement;
    }

    @Transactional
    public void removeEndorsement(Long endorserId, Long skillId) {
        if (!endorsementRepo.existsByEndorserIdAndSkillId(endorserId, skillId)) {
            throw new BusinessRuleException("No endorsement found to remove.");
        }
        endorsementRepo.deleteByEndorserIdAndSkillId(endorserId, skillId);

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new BusinessRuleException("Skill not found: " + skillId));
        skill.setEndorsementCount(Math.max(0, skill.getEndorsementCount() - 1));
        skillRepository.save(skill);
    }

    public List<SkillEndorsement> getEndorsementsForSkill(Long skillId) {
        return endorsementRepo.findBySkillId(skillId);
    }

    public boolean hasEndorsed(Long endorserId, Long skillId) {
        return endorsementRepo.existsByEndorserIdAndSkillId(endorserId, skillId);
    }

    public long getEndorsementCount(Long skillId) {
        return endorsementRepo.countBySkillId(skillId);
    }
}
