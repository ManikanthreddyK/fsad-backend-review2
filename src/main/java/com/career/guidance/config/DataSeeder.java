package com.career.guidance.config;

import com.career.guidance.model.*;
import com.career.guidance.repository.AppUserRepository;
import com.career.guidance.repository.CareerResourceRepository;
import com.career.guidance.repository.CounsellorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder {
    private final AppUserRepository userRepository;
    private final CounsellorRepository counsellorRepository;
    private final CareerResourceRepository resourceRepository;

    @PostConstruct
    public void seed() {
        if (userRepository.count() == 0) {
            userRepository.save(AppUser.builder()
                    .fullName("Aarav Student")
                    .email("student1@example.com")
                    .password("student123")
                    .role(UserRole.STUDENT)
                    .interests("Technology, Problem Solving")
                    .skills("Java, Communication")
                    .build());
            userRepository.save(AppUser.builder()
                    .fullName("Priya Admin")
                    .email("admin@example.com")
                    .password("admin123")
                    .role(UserRole.ADMIN)
                    .interests("Operations")
                    .skills("Management")
                    .build());
        }

        if (counsellorRepository.count() == 0) {
            counsellorRepository.save(Counsellor.builder()
                    .name("Dr. Meera Kapoor")
                    .expertise("Software Engineering Careers")
                    .careerPath("Software Engineering")
                    .email("meera@careerhub.com")
                    .password("counsellor123")
                    .build());
            counsellorRepository.save(Counsellor.builder()
                    .name("Rahul Iyer")
                    .expertise("Finance and Consulting")
                    .careerPath("Finance and Consulting")
                    .email("rahul@careerhub.com")
                    .password("counsellor123")
                    .build());
        }

        if (resourceRepository.count() == 0) {
            resourceRepository.save(CareerResource.builder()
                    .title("Roadmap to Full Stack Development")
                    .careerPath("Software Engineering")
                    .description("Learn frontend, backend, projects, and interview strategy.")
                    .link("https://roadmap.sh/full-stack")
                    .build());
            resourceRepository.save(CareerResource.builder()
                    .title("How to Build a Career in Data Science")
                    .careerPath("Data Science")
                    .description("Skill map from statistics to ML engineering.")
                    .link("https://www.kaggle.com/learn")
                    .build());
        }

        // Backfill passwords for older records created before auth was introduced.
        userRepository.findByEmail("student1@example.com").ifPresent(user -> {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword("student123");
                userRepository.save(user);
            }
        });
        userRepository.findByEmail("admin@example.com").ifPresent(user -> {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword("admin123");
                userRepository.save(user);
            }
        });
        counsellorRepository.findByEmail("meera@careerhub.com").ifPresent(c -> {
            if (c.getPassword() == null || c.getPassword().isBlank()) {
                c.setPassword("counsellor123");
            }
            if (c.getCareerPath() == null || c.getCareerPath().isBlank()) {
                c.setCareerPath("Software Engineering");
            }
            counsellorRepository.save(c);
        });
        counsellorRepository.findByEmail("rahul@careerhub.com").ifPresent(c -> {
            if (c.getPassword() == null || c.getPassword().isBlank()) {
                c.setPassword("counsellor123");
            }
            if (c.getCareerPath() == null || c.getCareerPath().isBlank()) {
                c.setCareerPath("Finance and Consulting");
            }
            counsellorRepository.save(c);
        });
    }
}
