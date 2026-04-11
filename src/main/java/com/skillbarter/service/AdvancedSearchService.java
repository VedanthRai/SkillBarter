package com.skillbarter.service;

import com.skillbarter.dto.AdvancedSearchDto;
import com.skillbarter.entity.Skill;
import com.skillbarter.enums.SkillCategory;
import com.skillbarter.enums.VerificationLevel;
import com.skillbarter.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Search Service with multi-criteria filtering
 * 
 * SOLID - SRP: Handles only search logic
 * SOLID - OCP: Easy to add new search criteria
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedSearchService {

    private final EntityManager entityManager;
    private final SkillRepository skillRepository;

    /**
     * Advanced search with multiple filters
     */
    public List<Skill> advancedSearch(AdvancedSearchDto searchDto) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Skill> query = cb.createQuery(Skill.class);
        Root<Skill> skill = query.from(Skill.class);
        
        List<Predicate> predicates = new ArrayList<>();

        // Text search (name or description)
        if (searchDto.getQuery() != null && !searchDto.getQuery().isEmpty()) {
            String searchPattern = "%" + searchDto.getQuery().toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(skill.get("name")), searchPattern);
            Predicate descPredicate = cb.like(cb.lower(skill.get("description")), searchPattern);
            predicates.add(cb.or(namePredicate, descPredicate));
        }

        // Category filter
        if (searchDto.getCategory() != null) {
            predicates.add(cb.equal(skill.get("category"), searchDto.getCategory()));
        }

        // Price range filter
        if (searchDto.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(skill.get("hourlyRate"), searchDto.getMinPrice()));
        }
        if (searchDto.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(skill.get("hourlyRate"), searchDto.getMaxPrice()));
        }

        // Rating filter
        if (searchDto.getMinRating() != null) {
            predicates.add(cb.greaterThanOrEqualTo(skill.get("averageRating"), searchDto.getMinRating()));
        }

        // Verification level filter
        if (searchDto.getVerificationLevel() != null) {
            predicates.add(cb.equal(skill.get("verificationLevel"), searchDto.getVerificationLevel()));
        }

        // Available only filter
        if (searchDto.getAvailableOnly() != null && searchDto.getAvailableOnly()) {
            predicates.add(cb.isTrue(skill.get("isOffering")));
        }

        // Combine all predicates
        query.where(predicates.toArray(new Predicate[0]));

        // Sorting
        if (searchDto.getSortBy() != null) {
            switch (searchDto.getSortBy()) {
                case "rating":
                    query.orderBy(cb.desc(skill.get("averageRating")));
                    break;
                case "price_asc":
                    query.orderBy(cb.asc(skill.get("hourlyRate")));
                    break;
                case "price_desc":
                    query.orderBy(cb.desc(skill.get("hourlyRate")));
                    break;
                case "newest":
                    query.orderBy(cb.desc(skill.get("createdAt")));
                    break;
                case "popular":
                    query.orderBy(cb.desc(skill.get("totalSessions")));
                    break;
                default:
                    query.orderBy(cb.desc(skill.get("averageRating")));
            }
        } else {
            query.orderBy(cb.desc(skill.get("averageRating")));
        }

        List<Skill> results = entityManager.createQuery(query).getResultList();
        log.info("Advanced search returned {} results", results.size());
        return results;
    }

    /**
     * Get autocomplete suggestions
     */
    public List<String> getSearchSuggestions(String query) {
        if (query == null || query.length() < 2) {
            return List.of();
        }

        String searchPattern = query.toLowerCase() + "%";
        return entityManager.createQuery(
            "SELECT DISTINCT s.name FROM Skill s WHERE LOWER(s.name) LIKE :pattern ORDER BY s.name",
            String.class)
            .setParameter("pattern", searchPattern)
            .setMaxResults(10)
            .getResultList();
    }

    /**
     * Get popular searches
     */
    public List<String> getPopularSearches() {
        return entityManager.createQuery(
            "SELECT s.name FROM Skill s ORDER BY s.totalSessions DESC",
            String.class)
            .setMaxResults(10)
            .getResultList();
    }

    /**
     * Get trending skills (most sessions in last 7 days)
     */
    public List<Skill> getTrendingSkills() {
        return skillRepository.findTrendingSkills(LocalDateTime.now().minusDays(7));
    }

    /**
     * Get skills by location/timezone
     */
    public List<Skill> searchByLocation(String timezone) {
        return entityManager.createQuery(
            "SELECT s FROM Skill s WHERE s.user.timezone = :timezone AND s.isOffering = true",
            Skill.class)
            .setParameter("timezone", timezone)
            .getResultList();
    }
}
