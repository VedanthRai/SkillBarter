package com.skillbarter.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionRequestDto {

    @NotNull(message = "Skill is required")
    private Long skillId;

    @NotNull(message = "Scheduled time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledAt;

    @Min(value = 30, message = "Minimum session duration is 30 minutes")
    @Max(value = 480, message = "Maximum session duration is 8 hours")
    @Builder.Default
    private int durationMinutes = 60;

    @Size(max = 500)
    private String notes;

    /** Manual future check since @Future on LocalDateTime needs extra config */
    public boolean isScheduledInFuture() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }
}
