package com.skillbarter.service;

import com.skillbarter.entity.LearningPath;
import com.skillbarter.entity.LearningPathStep;
import com.skillbarter.entity.User;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.repository.LearningPathRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final UserService userService;

    @Transactional
    public LearningPath createPath(Long userId, String title, String description, List<String> skillNames) {
        User user = userService.findById(userId);

        LearningPath path = LearningPath.builder()
                .user(user)
                .title(title)
                .description(description)
                .build();

        for (int i = 0; i < skillNames.size(); i++) {
            LearningPathStep step = LearningPathStep.builder()
                    .learningPath(path)
                    .stepOrder(i + 1)
                    .skillName(skillNames.get(i))
                    .build();
            path.getSteps().add(step);
        }

        return learningPathRepository.save(path);
    }

    @Transactional
    public LearningPath markStepCompleted(Long pathId, Integer stepOrder, Long userId) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found"));

        if (!path.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own learning paths");
        }

        path.getSteps().stream()
                .filter(step -> step.getStepOrder().equals(stepOrder))
                .findFirst()
                .ifPresent(step -> step.setIsCompleted(true));

        return learningPathRepository.save(path);
    }

    public List<LearningPath> getPathsForUser(Long userId) {
        return learningPathRepository.findByUserId(userId);
    }

    public LearningPath getPath(Long pathId) {
        return learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found"));
    }

    @Transactional
    public void deletePath(Long pathId, Long userId) {
        LearningPath path = getPath(pathId);
        if (!path.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own learning paths");
        }
        learningPathRepository.delete(path);
    }

    public LearningPath getPathWithProgress(Long pathId) {
        return getPath(pathId);
    }

    @Transactional
    public LearningPath markStepComplete(Long pathId, Long stepId) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found"));

        path.getSteps().stream()
                .filter(step -> step.getId().equals(stepId))
                .findFirst()
                .ifPresent(step -> step.setIsCompleted(true));

        return learningPathRepository.save(path);
    }

    @Transactional
    public LearningPath createPath(Long userId, com.skillbarter.dto.LearningPathDto dto) {
        // Convert skill IDs to skill names (simplified - in production would fetch actual skills)
        List<String> skillNames = dto.getSkillIds() != null 
            ? dto.getSkillIds().stream().map(id -> "Skill-" + id).toList()
            : new ArrayList<>();
        return createPath(userId, dto.getTitle(), dto.getDescription(), skillNames);
    }
}
