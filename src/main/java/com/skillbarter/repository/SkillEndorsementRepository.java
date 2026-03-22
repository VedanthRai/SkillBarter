package com.skillbarter.repository;

import com.skillbarter.entity.SkillEndorsement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillEndorsementRepository extends JpaRepository<SkillEndorsement, Long> {

    List<SkillEndorsement> findBySkillId(Long skillId);

    List<SkillEndorsement> findByEndorserId(Long endorserId);

    boolean existsByEndorserIdAndSkillId(Long endorserId, Long skillId);

    long countBySkillId(Long skillId);

    @Query("SELECT se FROM SkillEndorsement se WHERE se.skill.user.id = :userId")
    List<SkillEndorsement> findEndorsementsReceivedByUser(@Param("userId") Long userId);

    void deleteByEndorserIdAndSkillId(Long endorserId, Long skillId);
}
