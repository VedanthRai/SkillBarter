package com.skillbarter.dto;

import com.skillbarter.enums.SkillCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillDto {

    @NotBlank(message = "Skill name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Category is required")
    private SkillCategory category;

    @NotNull
    @DecimalMin(value = "0.5", message = "Minimum rate is 0.5 credits/hour")
    @DecimalMax(value = "10.0", message = "Maximum rate is 10 credits/hour")
    private BigDecimal hourlyRate;

    private String proficiencyLevel;

    @NotNull
    private Boolean isOffering;
}
