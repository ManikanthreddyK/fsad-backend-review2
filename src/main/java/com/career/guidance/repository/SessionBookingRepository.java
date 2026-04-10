package com.career.guidance.repository;

import com.career.guidance.model.SessionBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionBookingRepository extends JpaRepository<SessionBooking, Long> {
    List<SessionBooking> findByStudentId(Long studentId);
    List<SessionBooking> findByCounsellorId(Long counsellorId);
}
