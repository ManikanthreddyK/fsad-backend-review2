package com.career.guidance.controller;

import com.career.guidance.dto.AuthRequest;
import com.career.guidance.dto.AuthResponse;
import com.career.guidance.dto.GoogleAuthRequest;
import com.career.guidance.dto.LoginRequest;
import com.career.guidance.model.AppUser;
import com.career.guidance.model.Counsellor;
import com.career.guidance.model.UserRole;
import com.career.guidance.repository.AppUserRepository;
import com.career.guidance.repository.CounsellorRepository;
import com.career.guidance.service.GoogleTokenService;
import com.career.guidance.service.JwtService;
import com.career.guidance.service.RecaptchaService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
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
    private final GoogleTokenService googleTokenService;
    private final JwtService jwtService;
    private final RecaptchaService recaptchaService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest request) {
        UserRole requestedRole = request.getRole();
        if (requestedRole == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role is required");
        }
        return switch (requestedRole) {
            case STUDENT -> registerStudent(request);
            case ADMIN -> registerAdmin(request);
            case COUNSELLOR -> registerCounsellor(request);
        };
    }

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
        if (request.getCaptchaToken() == null || request.getCaptchaToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please complete CAPTCHA");
        }
        if (!recaptchaService.verifyToken(request.getCaptchaToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CAPTCHA verification failed");
        }

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

    @PostMapping("/google")
    public AuthResponse googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        GoogleTokenService.GoogleProfile googleProfile = googleTokenService.verify(request.getCredential());
        if (googleProfile.email() == null || googleProfile.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google account email is not available");
        }

        if (counsellorRepository.findByEmail(googleProfile.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This email belongs to a counsellor account. Use password login.");
        }

        AppUser user = userRepository.findByEmail(googleProfile.email())
                .orElseGet(() -> userRepository.save(AppUser.builder()
                        .fullName(defaultValue(googleProfile.name()))
                        .email(googleProfile.email())
                        .password(null)
                        .role(UserRole.STUDENT)
                        .interests("Not provided")
                        .skills("Not provided")
                        .build()));
        return toResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    @GetMapping("/me")
    public AuthResponse me(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            var claims = jwtService.parseToken(token);
            Long userId = Long.parseLong(claims.get("sub"));
            String role = claims.get("role");
            if (UserRole.COUNSELLOR.name().equals(role)) {
                Counsellor counsellor = counsellorRepository.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Counsellor not found"));
                return toResponse(counsellor.getId(), counsellor.getName(), counsellor.getEmail(), UserRole.COUNSELLOR);
            }
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            return toResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    private AuthResponse toResponse(Long id, String fullName, String email, UserRole role) {
        return AuthResponse.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .role(role)
                .token(jwtService.generateToken(id, email, fullName, role))
                .build();
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }
        return authHeader.substring(7);
    }

    private String defaultValue(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }
}
