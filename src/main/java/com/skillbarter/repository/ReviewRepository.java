package com.skillbarter.repository;

import com.skillbarter.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
// Handles database operations for reviews
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByReviewedId(Long reviewedId);

    List<Review> findByReviewerId(Long reviewerId);

    Optional<Review> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewed.id = :userId")
    Double findAverageRatingForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewed.id = :userId")
    Long countReviewsForUser(@Param("userId") Long userId);
}
