package com.skillbarter.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewDto {

    @Min(1) @Max(5)
    @NotNull(message = "Rating is required")
    private Integer rating;

    @Size(max = 1000)
    private String comment;
}
