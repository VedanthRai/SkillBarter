package com.skillbarter.dto;

import jakarta.validation.constraints.*;
import lombok.*;
// Transfers data for raising disputes
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisputeRequestDto {

    @NotBlank(message = "Please describe the issue")
    @Size(min = 20, max = 2000, message = "Description must be 20–2000 characters")
    private String description;

    @Size(max = 2000)
    private String evidence;  // comma-separated URLs or file names
}
