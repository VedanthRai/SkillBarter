package com.skillbarter.dto;

import com.skillbarter.enums.SkillCategory;
import com.skillbarter.enums.VerificationLevel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for advanced skill search with multiple filters
 */
@Data
public class AdvancedSearchDto {
    private String query;
    private SkillCategory category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private VerificationLevel verificationLevel;
    private String sortBy; // "rating", "price_asc", "price_desc", "newest", "popular"
    private Boolean availableOnly;
    private String timezone;
}
