package com.career.guidance.controller;

import com.career.guidance.dto.AuthRequest;
import com.career.guidance.dto.AuthResponse;
import com.career.guidance.dto.LoginRequest;
import com.career.guidance.model.AppUser;
import com.career.guidance.model.Counsellor;
import com.career.guidance.model.UserRole;
import com.career.guidance.repository.AppUserRepository;
import com.career.guidance.repository.CounsellorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AppUserRepository userRepository;
    private final CounsellorRepository counsellorRepository;

    @PostMapping("/register/student")
    public AuthResponse registerStudent(@Valid @RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent() ||
                counsellorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        AppUser user = userRepository.save(AppUser.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(UserRole.STUDENT)
                .interests(defaultValue(request.getInterests()))
                .skills(defaultValue(request.getSkills()))
                .build());
        return toResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    @PostMapping("/register/admin")
    public AuthResponse registerAdmin(@Valid @RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent() ||
                counsellorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        AppUser user = userRepository.save(AppUser.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(UserRole.ADMIN)
                .interests(defaultValue(request.getInterests()))
                .skills(defaultValue(request.getSkills()))
                .build());
        return toResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    @PostMapping("/register/counsellor")
    public AuthResponse registerCounsellor(@Valid @RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent() ||
                counsellorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        Counsellor counsellor = counsellorRepository.save(Counsellor.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .expertise(defaultValue(request.getExpertise()))
                .careerPath(defaultValue(request.getCareerPath()))
                .build());
        return toResponse(counsellor.getId(), counsellor.getName(), counsellor.getEmail(), UserRole.COUNSELLOR);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        AppUser appUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (appUser != null && appUser.getPassword() != null && appUser.getPassword().equals(request.getPassword())) {
            return toResponse(appUser.getId(), appUser.getFullName(), appUser.getEmail(), appUser.getRole());
        }

        Counsellor counsellor = counsellorRepository.findByEmail(request.getEmail()).orElse(null);
        if (counsellor != null && counsellor.getPassword() != null && counsellor.getPassword().equals(request.getPassword())) {
            return toResponse(counsellor.getId(), counsellor.getName(), counsellor.getEmail(), UserRole.COUNSELLOR);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private AuthResponse toResponse(Long id, String fullName, String email, UserRole role) {
        return AuthResponse.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .role(role)
                .build();
    }

    private String defaultValue(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }
}
