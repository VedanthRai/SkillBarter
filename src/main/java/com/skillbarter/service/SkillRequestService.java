package com.skillbarter.service;

import com.skillbarter.entity.SkillRequest;
import com.skillbarter.entity.User;
import com.skillbarter.enums.SkillCategory;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.repository.SkillRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillRequestService {

    private final SkillRequestRepository skillRequestRepository;
    private final UserService userService;

    @Transactional
    public SkillRequest createRequest(Long learnerId, String skillName, SkillCategory category, String description) {
        User learner = userService.findById(learnerId);

        SkillRequest request = SkillRequest.builder()
                .learner(learner)
                .skillName(skillName)
                .category(category)
                .description(description)
                .build();

        return skillRequestRepository.save(request);
    }

    @Transactional
    public void closeRequest(Long requestId, Long userId) {
        SkillRequest request = skillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill request not found"));

        if (!request.getLearner().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only close your own requests");
        }

        request.setIsActive(false);
        skillRequestRepository.save(request);
    }

    public List<SkillRequest> getAllActiveRequests() {
        return skillRequestRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    public List<SkillRequest> getRequestsByCategory(SkillCategory category) {
        return skillRequestRepository.findByCategoryAndIsActiveTrue(category);
    }

    public List<SkillRequest> getRequestsForLearner(Long learnerId) {
        return skillRequestRepository.findByLearnerId(learnerId);
    }

    public List<SkillRequest> getRequestsByUser(Long userId) {
        return getRequestsForLearner(userId);
    }

    @Transactional
    public void offerToTeach(Long requestId, Long teacherId) {
        SkillRequest request = skillRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill request not found"));
        
        log.info("Teacher {} offered to teach skill request {}", teacherId, requestId);
        // In a full implementation, this would create a notification or session proposal
    }

    @Transactional
    public SkillRequest createRequest(Long learnerId, com.skillbarter.dto.SkillRequestDto dto) {
        return createRequest(learnerId, dto.getSkillName(), dto.getCategory(), dto.getDescription());
    }
}
