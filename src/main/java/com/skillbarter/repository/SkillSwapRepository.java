package com.skillbarter.repository;

import com.skillbarter.entity.SkillSwap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillSwapRepository extends JpaRepository<SkillSwap, Long> {
    @Query("SELECT s FROM SkillSwap s WHERE s.userA.id = :userId OR s.userB.id = :userId")
    List<SkillSwap> findByUserId(Long userId);
}
