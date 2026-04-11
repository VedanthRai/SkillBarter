package com.skillbarter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SessionNotesDto {
    private String teacherNotes;
    private String learnerNotes;
    
    @NotBlank(message = "Key takeaways are required")
    private String keyTakeaways;
    
    private String homework;
    private String resourcesShared;
}
