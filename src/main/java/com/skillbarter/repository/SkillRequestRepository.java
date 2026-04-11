package com.skillbarter.repository;

import com.skillbarter.entity.SkillRequest;
import com.skillbarter.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRequestRepository extends JpaRepository<SkillRequest, Long> {
    List<SkillRequest> findByIsActiveTrueOrderByCreatedAtDesc();
    List<SkillRequest> findByLearnerId(Long learnerId);
    List<SkillRequest> findByCategoryAndIsActiveTrue(SkillCategory category);
}
