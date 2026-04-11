package com.skillbarter.repository;

import com.skillbarter.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    List<LearningPath> findByUserId(Long userId);
}
