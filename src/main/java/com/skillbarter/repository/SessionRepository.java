package com.skillbarter.repository;

import com.skillbarter.entity.Session;
import com.skillbarter.enums.SessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByLearnerId(Long learnerId);

    List<Session> findByTeacherId(Long teacherId);

    List<Session> findByStatus(SessionStatus status);

    List<Session> findByLearnerIdAndStatus(Long learnerId, SessionStatus status);

    List<Session> findByTeacherIdAndStatus(Long teacherId, SessionStatus status);

    boolean existsByLearnerIdAndTeacherIdAndSkillIdAndLearnerNotes(
            Long learnerId, Long teacherId, Long skillId, String learnerNotes);

    /** Sessions involving a user as either learner or teacher */
    @EntityGraph(attributePaths = {"learner", "teacher", "skill"})
    @Query("SELECT s FROM Session s WHERE (s.learner.id = :userId OR s.teacher.id = :userId) " +
            "ORDER BY s.scheduledAt DESC")
    List<Session> findAllByUserId(@Param("userId") Long userId);

    @Override
    @EntityGraph(attributePaths = {"learner", "teacher", "skill", "messages", "messages.sender", "review", "dispute", "transaction"})
    Optional<Session> findById(Long id);

    /** Find sessions that are accepted and scheduled in the past (auto-complete candidates) */
    @Query("SELECT s FROM Session s WHERE s.status = 'ACCEPTED' AND s.scheduledAt < :cutoff")
    List<Session> findExpiredAcceptedSessions(@Param("cutoff") LocalDateTime cutoff);

    /** Count completed sessions for a user as teacher */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.teacher.id = :userId AND s.status = 'COMPLETED'")
    long countCompletedAsTeacher(@Param("userId") Long userId);

    /** Count completed sessions for a user as learner */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.learner.id = :userId AND s.status = 'COMPLETED'")
    long countCompletedAsLearner(@Param("userId") Long userId);

    /** Find sessions scheduled between two times with specific status (for reminders) */
    @Query("SELECT s FROM Session s WHERE s.scheduledAt BETWEEN :start AND :end AND s.status = :status")
    List<Session> findSessionsScheduledBetween(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end,
        @Param("status") SessionStatus status
    );

    /** Analytics: Sessions by category */
    @Query("SELECT s.skill.category, COUNT(s) FROM Session s GROUP BY s.skill.category")
    List<Object[]> countSessionsByCategory();

    /** Analytics: Sessions by hour of day */
    @Query("SELECT HOUR(s.scheduledAt), COUNT(s) FROM Session s " +
           "WHERE s.scheduledAt IS NOT NULL GROUP BY HOUR(s.scheduledAt)")
    List<Object[]> getSessionsByHourOfDay();

    /** Analytics: Completion rate by day */
    @Query("SELECT DATE(s.completedAt) as date, " +
           "(COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(*)) as rate " +
           "FROM Session s WHERE s.completedAt >= :startDate " +
           "GROUP BY DATE(s.completedAt) ORDER BY DATE(s.completedAt)")
    List<Object[]> getCompletionRateByDay(@Param("startDate") java.time.LocalDateTime startDate);

    /** Analytics: Count reviews by user */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.review IS NOT NULL AND s.learner.id = :userId")
    long countReviewsByUser(@Param("userId") Long userId);
}
