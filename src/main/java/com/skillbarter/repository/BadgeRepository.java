package com.skillbarter.repository;

import com.skillbarter.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByUserId(Long userId);

    boolean existsByUserIdAndBadgeType(Long userId, String badgeType);

    boolean existsByUserIdAndSkillId(Long userId, Long skillId);
}
