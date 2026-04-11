package com.skillbarter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillSwapDto {
    @NotNull(message = "Your skill is required")
    private Long mySkillId;
    
    @NotNull(message = "Desired skill is required")
    private Long desiredSkillId;
    
    private String message;
}
