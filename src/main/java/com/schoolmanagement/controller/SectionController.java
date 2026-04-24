package com.schoolmanagement.controller;

import com.schoolmanagement.dto.response.SectionResponse;
import com.schoolmanagement.entity.Section;
import com.schoolmanagement.repository.GradeRepository;
import com.schoolmanagement.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionRepository sectionRepository;
    private final GradeRepository gradeRepository;

    @GetMapping
    public List<SectionResponse> list() {
        return sectionRepository.findAllWithGrade().stream()
                .map(s -> new SectionResponse(s.getId(), s.getName(), s.getGrade().getId(),
                        s.getGrade().getName(), s.getFullName()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SectionResponse create(@RequestBody CreateSectionRequest req) {
        var grade = gradeRepository.findById(req.gradeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found"));
        var section = Section.builder()
                .grade(grade)
                .name(req.name().toUpperCase())
                .build();
        var saved = sectionRepository.save(section);
        return new SectionResponse(saved.getId(), saved.getName(), grade.getId(),
                grade.getName(), saved.getFullName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        sectionRepository.deleteById(id);
    }

    public record CreateSectionRequest(Long gradeId, String name) {}
}
