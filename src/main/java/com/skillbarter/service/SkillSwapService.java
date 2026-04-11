package com.skillbarter.service;

import com.skillbarter.entity.Skill;
import com.skillbarter.entity.SkillSwap;
import com.skillbarter.entity.User;
import com.skillbarter.enums.SessionStatus;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.repository.SkillRepository;
import com.skillbarter.repository.SkillSwapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillSwapService {

    private final SkillSwapRepository skillSwapRepository;
    private final SkillRepository skillRepository;
    private final UserService userService;

    @Transactional
    public SkillSwap proposeSwap(Long userAId, Long userBId, Long skillAId, Long skillBId,
                                 LocalDateTime sessionATime, LocalDateTime sessionBTime) {
        if (userAId.equals(userBId)) {
            throw new BusinessRuleException("Cannot swap with yourself");
        }

        User userA = userService.findById(userAId);
        User userB = userService.findById(userBId);
        Skill skillA = skillRepository.findById(skillAId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill A not found"));
        Skill skillB = skillRepository.findById(skillBId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill B not found"));

        if (!skillA.getUser().getId().equals(userAId)) {
            throw new BusinessRuleException("You don't own skill A");
        }
        if (!skillB.getUser().getId().equals(userBId)) {
            throw new BusinessRuleException("User B doesn't own skill B");
        }

        SkillSwap swap = SkillSwap.builder()
                .userA(userA)
                .userB(userB)
                .skillA(skillA)
                .skillB(skillB)
                .sessionAScheduledAt(sessionATime)
                .sessionBScheduledAt(sessionBTime)
                .status(SessionStatus.REQUESTED)
                .build();

        return skillSwapRepository.save(swap);
    }

    @Transactional
    public SkillSwap acceptSwap(Long swapId, Long userId) {
        SkillSwap swap = skillSwapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap not found"));

        if (!swap.getUserB().getId().equals(userId)) {
            throw new BusinessRuleException("Only user B can accept this swap");
        }

        swap.setStatus(SessionStatus.ACCEPTED);
        return skillSwapRepository.save(swap);
    }

    @Transactional
    public SkillSwap completeSessionA(Long swapId, Long userId) {
        SkillSwap swap = skillSwapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap not found"));

        swap.setSessionACompleted(true);
        if (swap.getSessionBCompleted()) {
            swap.setStatus(SessionStatus.COMPLETED);
        }
        return skillSwapRepository.save(swap);
    }

    @Transactional
    public SkillSwap completeSessionB(Long swapId, Long userId) {
        SkillSwap swap = skillSwapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap not found"));

        swap.setSessionBCompleted(true);
        if (swap.getSessionACompleted()) {
            swap.setStatus(SessionStatus.COMPLETED);
        }
        return skillSwapRepository.save(swap);
    }

    public List<SkillSwap> getSwapsForUser(Long userId) {
        return skillSwapRepository.findByUserId(userId);
    }

    @Transactional
    public void rejectSwap(Long swapId, Long userId) {
        SkillSwap swap = skillSwapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap not found"));

        if (!swap.getUserB().getId().equals(userId)) {
            throw new BusinessRuleException("Only user B can reject this swap");
        }

        swap.setStatus(SessionStatus.CANCELLED);
        skillSwapRepository.save(swap);
        log.info("Skill swap {} rejected by user {}", swapId, userId);
    }

    @Transactional
    public SkillSwap proposeSwap(Long userAId, com.skillbarter.dto.SkillSwapDto dto) {
        // Find the skill owners
        Skill mySkill = skillRepository.findById(dto.getMySkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Your skill not found"));
        Skill desiredSkill = skillRepository.findById(dto.getDesiredSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Desired skill not found"));
        
        Long userBId = desiredSkill.getUser().getId();
        
        // Schedule sessions for next week (simplified)
        LocalDateTime sessionATime = LocalDateTime.now().plusDays(7);
        LocalDateTime sessionBTime = LocalDateTime.now().plusDays(8);
        
        return proposeSwap(userAId, userBId, dto.getMySkillId(), dto.getDesiredSkillId(), 
                          sessionATime, sessionBTime);
    }
}
