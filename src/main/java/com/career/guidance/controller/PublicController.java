package com.career.guidance.controller;

import com.career.guidance.model.CareerResource;
import com.career.guidance.model.Counsellor;
import com.career.guidance.repository.CareerResourceRepository;
import com.career.guidance.repository.CounsellorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final CareerResourceRepository resourceRepository;
    private final CounsellorRepository counsellorRepository;

    @GetMapping("/resources")
    public List<CareerResource> getResources(@RequestParam(required = false) String careerPath) {
        if (careerPath == null || careerPath.isBlank()) {
            return resourceRepository.findAll();
        }
        return resourceRepository.findByCareerPathContainingIgnoreCase(careerPath);
    }

    @GetMapping("/counsellors")
    public List<Counsellor> getCounsellors() {
        return counsellorRepository.findAll();
    }
}
