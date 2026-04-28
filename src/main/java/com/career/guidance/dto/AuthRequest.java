package com.career.guidance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import com.career.guidance.model.UserRole;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String interests;
    private String skills;
    private String expertise;
    private String careerPath;
    private UserRole role;
}
