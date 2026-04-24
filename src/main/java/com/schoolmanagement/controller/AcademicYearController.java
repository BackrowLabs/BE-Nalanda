package com.schoolmanagement.controller;

import com.schoolmanagement.dto.response.AcademicYearResponse;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.repository.AcademicYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearRepository academicYearRepository;

    @GetMapping
    public List<AcademicYearResponse> list() {
        return academicYearRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AcademicYearResponse create(@RequestBody CreateAcademicYearRequest req) {
        var year = AcademicYear.builder()
                .name(req.name())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .build();
        return toResponse(academicYearRepository.save(year));
    }

    @PatchMapping("/{id}/set-current")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AcademicYearResponse setCurrent(@PathVariable Long id) {
        academicYearRepository.clearAllCurrent();
        AcademicYear year = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic year not found"));
        year.setCurrent(true);
        return toResponse(academicYearRepository.save(year));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        academicYearRepository.deleteById(id);
    }

    private AcademicYearResponse toResponse(AcademicYear y) {
        return new AcademicYearResponse(y.getId(), y.getName(), y.getStartDate(), y.getEndDate(), y.isCurrent());
    }

    public record CreateAcademicYearRequest(String name, LocalDate startDate, LocalDate endDate) {}
}
