package com.career.guidance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String interests;
    private String skills;
    private String expertise;
    private String careerPath;
}
