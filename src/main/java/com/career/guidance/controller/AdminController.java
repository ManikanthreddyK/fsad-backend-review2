package com.career.guidance.controller;

import com.career.guidance.model.ActivityLog;
import com.career.guidance.model.AppUser;
import com.career.guidance.model.CareerResource;
import com.career.guidance.model.Counsellor;
import com.career.guidance.model.SessionBooking;
import com.career.guidance.model.UserRole;
import com.career.guidance.repository.ActivityLogRepository;
import com.career.guidance.repository.AppUserRepository;
import com.career.guidance.repository.CareerResourceRepository;
import com.career.guidance.repository.CounsellorRepository;
import com.career.guidance.repository.SessionBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final CareerResourceRepository resourceRepository;
    private final ActivityLogRepository activityLogRepository;
    private final SessionBookingRepository bookingRepository;
    private final AppUserRepository userRepository;
    private final CounsellorRepository counsellorRepository;

    @PostMapping("/resources")
    public CareerResource addResource(@RequestBody CareerResource resource) {
        return resourceRepository.save(resource);
    }

    @GetMapping("/resources")
    public List<CareerResource> getAllResources() {
        return resourceRepository.findAll();
    }

    @DeleteMapping("/resources/{id}")
    public void deleteResource(@PathVariable Long id) {
        resourceRepository.deleteById(id);
    }

    @GetMapping("/engagement")
    public Map<String, Long> engagement() {
        long totalResources = resourceRepository.count();
        long totalSessions = bookingRepository.count();
        long totalActivityEvents = activityLogRepository.count();
        long totalStudents = userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.STUDENT).count();
        long totalCounsellors = counsellorRepository.count();
        return Map.of(
                "totalResources", totalResources,
                "totalSessions", totalSessions,
                "totalActivityEvents", totalActivityEvents,
                "totalStudents", totalStudents,
                "totalCounsellors", totalCounsellors
        );
    }

    @GetMapping("/sessions")
    public List<SessionBooking> allSessions() {
        return bookingRepository.findAll();
    }

    @GetMapping("/activity")
    public List<ActivityLog> activityLogs() {
        return activityLogRepository.findAll();
    }

    @GetMapping("/students")
    public List<AppUser> allStudents() {
        return userRepository.findAll().stream().filter(user -> user.getRole() == UserRole.STUDENT).toList();
    }

    @GetMapping("/counsellors")
    public List<Counsellor> allCounsellors() {
        return counsellorRepository.findAll();
    }

    @DeleteMapping("/students/{id}")
    public void deleteStudent(@PathVariable Long id) {
        bookingRepository.deleteAll(bookingRepository.findByStudentId(id));
        userRepository.deleteById(id);
    }

    @DeleteMapping("/counsellors/{id}")
    public void deleteCounsellor(@PathVariable Long id) {
        bookingRepository.deleteAll(bookingRepository.findByCounsellorId(id));
        counsellorRepository.deleteById(id);
    }
}
