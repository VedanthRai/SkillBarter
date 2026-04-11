package com.skillbarter.dto;

import com.skillbarter.enums.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkillRequestDto {
    @NotBlank(message = "Skill name is required")
    private String skillName;
    
    @NotNull(message = "Category is required")
    private SkillCategory category;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private BigDecimal maxCreditsWilling;
}
