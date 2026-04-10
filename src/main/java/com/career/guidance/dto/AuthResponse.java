package com.career.guidance.dto;

import com.career.guidance.model.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
}
