package com.career.guidance.controller;

import com.career.guidance.model.SessionBooking;
import com.career.guidance.repository.SessionBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/counsellor")
@RequiredArgsConstructor
public class CounsellorController {
    private final SessionBookingRepository bookingRepository;

    @GetMapping("/sessions/{counsellorId}")
    public List<SessionBooking> getSessionsForCounsellor(@PathVariable Long counsellorId) {
        return bookingRepository.findByCounsellorId(counsellorId);
    }
}
