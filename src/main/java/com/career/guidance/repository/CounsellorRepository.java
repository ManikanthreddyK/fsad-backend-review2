package com.career.guidance.repository;

import com.career.guidance.model.Counsellor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounsellorRepository extends JpaRepository<Counsellor, Long> {
    Optional<Counsellor> findByEmail(String email);
}
