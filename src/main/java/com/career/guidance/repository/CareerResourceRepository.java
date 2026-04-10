package com.career.guidance.repository;

import com.career.guidance.model.CareerResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerResourceRepository extends JpaRepository<CareerResource, Long> {
    List<CareerResource> findByCareerPathContainingIgnoreCase(String careerPath);
}
