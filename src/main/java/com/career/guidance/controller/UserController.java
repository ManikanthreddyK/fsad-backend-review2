package com.career.guidance.controller;

import com.career.guidance.dto.BookSessionRequest;
import com.career.guidance.model.ActivityLog;
import com.career.guidance.model.AppUser;
import com.career.guidance.model.Counsellor;
import com.career.guidance.model.SessionBooking;
import com.career.guidance.repository.ActivityLogRepository;
import com.career.guidance.repository.AppUserRepository;
import com.career.guidance.repository.CounsellorRepository;
import com.career.guidance.repository.SessionBookingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final AppUserRepository userRepository;
    private final CounsellorRepository counsellorRepository;
    private final SessionBookingRepository bookingRepository;
    private final ActivityLogRepository activityLogRepository;

    @GetMapping("/students")
    public List<AppUser> getStudents() {
        return userRepository.findAll().stream().filter(u -> "STUDENT".equals(u.getRole().name())).toList();
    }

    @PostMapping("/sessions")
    public SessionBooking bookSession(@Valid @RequestBody BookSessionRequest request) {
        AppUser student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Counsellor counsellor = counsellorRepository.findById(request.getCounsellorId())
                .orElseThrow(() -> new RuntimeException("Counsellor not found"));

        SessionBooking booking = SessionBooking.builder()
                .student(student)
                .counsellor(counsellor)
                .sessionTime(LocalDateTime.parse(request.getSessionTime()))
                .mode(request.getMode())
                .status("SCHEDULED")
                .build();

        SessionBooking saved = bookingRepository.save(booking);
        activityLogRepository.save(ActivityLog.builder()
                .user(student)
                .action("Booked counselling session with " + counsellor.getName())
                .createdAt(LocalDateTime.now())
                .build());
        return saved;
    }

    @GetMapping("/sessions/{studentId}")
    public List<SessionBooking> getStudentSessions(@PathVariable Long studentId) {
        return bookingRepository.findByStudentId(studentId);
    }
}
