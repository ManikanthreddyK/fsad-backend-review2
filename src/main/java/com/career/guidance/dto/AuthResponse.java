package com.career.guidance.dto;

import com.career.guidance.model.UserRole;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String token;
}
