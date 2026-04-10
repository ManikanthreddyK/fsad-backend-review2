package com.career.guidance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookSessionRequest {
    @NotNull
    private Long studentId;

    @NotNull
    private Long counsellorId;

    @NotBlank
    private String sessionTime;

    @NotBlank
    private String mode;
}
