package com.skillbarter.service;

import com.skillbarter.dto.ReviewDto;
import com.skillbarter.entity.Review;
import com.skillbarter.entity.Session;
import com.skillbarter.entity.User;
import com.skillbarter.enums.SessionStatus;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.repository.ReviewRepository;
import com.skillbarter.repository.SkillRepository;
import com.skillbarter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Review Service — Major Feature 4.
 * SOLID – SRP: manages only post-session reviews.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SessionService sessionService;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         SessionService sessionService,
                         SkillRepository skillRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.sessionService = sessionService;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Review submitReview(Long sessionId, Long reviewerId, ReviewDto dto) {
        Session session = sessionService.getSession(sessionId);

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new BusinessRuleException("Can only review completed sessions.");
        }
        if (!session.getLearner().getId().equals(reviewerId)) {
            throw new BusinessRuleException("Only the learner can submit a review.");
        }
        if (reviewRepository.existsBySessionId(sessionId)) {
            throw new BusinessRuleException("Review already submitted for this session.");
        }

        User reviewer = session.getLearner();
        User reviewed = session.getTeacher();

        Review review = Review.builder()
                .session(session)
                .reviewer(reviewer)
                .reviewed(reviewed)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        review = reviewRepository.save(review);

        // Update skill average rating and save
        session.getSkill().updateRating(dto.getRating());
        skillRepository.save(session.getSkill());

        // Update teacher reputation score
        reviewed.setReputationScore(reviewed.getReputationScore() + dto.getRating());
        userRepository.save(reviewed);

        return review;
    }

    public List<Review> getReviewsForUser(Long userId) {
        return reviewRepository.findByReviewedId(userId);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByReviewerId(userId);
    }

    public Double getAverageRating(Long userId) {
        Double avg = reviewRepository.findAverageRatingForUser(userId);
        return avg != null ? avg : 0.0;
    }

    public Review getReviewForSession(Long sessionId) {
        return reviewRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No review for session: " + sessionId));
    }
}
