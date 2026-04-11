package com.skillbarter.repository;

import com.skillbarter.entity.Skill;
import com.skillbarter.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByUserId(Long userId);

    List<Skill> findByCategory(SkillCategory category);

    List<Skill> findByIsOfferingTrue();

    List<Skill> findByIsOfferingFalse();

    List<Skill> findByVerifiedTrue();

    List<Skill> findByCategoryAndIsOfferingTrue(SkillCategory category);

    /** Full-text search by name or description */
    @Query("SELECT s FROM Skill s WHERE s.isOffering = true AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Skill> searchOfferedSkills(@Param("keyword") String keyword);

    /** Find skills offered by other users that match a wanted skill name */
    @Query("SELECT s FROM Skill s WHERE s.isOffering = true AND s.user.id != :userId AND " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :skillName, '%')) AND s.user.status = 'ACTIVE'")
    List<Skill> findMatchingOfferedSkills(@Param("skillName") String skillName,
                                          @Param("userId") Long userId);

    /** Skills needing verification (certificate uploaded, not yet verified) */
    @Query("SELECT s FROM Skill s WHERE s.certificatePath IS NOT NULL AND s.verified = false")
    List<Skill> findPendingVerification();

    /** Analytics: Top skills by demand (total sessions) */
    @Query("SELECT s.name, COUNT(sess) as sessionCount " +
           "FROM Skill s LEFT JOIN Session sess ON sess.skill = s " +
           "GROUP BY s.id, s.name ORDER BY sessionCount DESC")
    List<Object[]> getTopSkillsByDemand(int limit);

    /** Analytics: Trending skills (sessions in last 7 days) */
    @Query("SELECT s FROM Skill s LEFT JOIN Session sess ON sess.skill = s " +
           "WHERE sess.createdAt >= :weekAgo " +
           "GROUP BY s ORDER BY COUNT(sess) DESC")
    List<Skill> findTrendingSkills(@Param("weekAgo") java.time.LocalDateTime weekAgo);

    /** Analytics: Total endorsements for user */
    @Query("SELECT SUM(s.endorsementCount) FROM Skill s WHERE s.user.id = :userId")
    Long getTotalEndorsementsForUser(@Param("userId") Long userId);

    /** Count skills by user and offering status */
    long countByUserIdAndIsOffering(Long userId, Boolean isOffering);
}
