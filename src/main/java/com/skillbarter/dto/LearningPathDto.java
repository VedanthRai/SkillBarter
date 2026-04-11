package com.skillbarter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class LearningPathDto {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private List<Long> skillIds;
}
