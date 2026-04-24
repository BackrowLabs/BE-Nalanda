package com.schoolmanagement.controller;

import com.schoolmanagement.dto.request.SaveGradeRequest;
import com.schoolmanagement.dto.response.GradeRecordResponse;
import com.schoolmanagement.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/student/{studentId}")
    public List<GradeRecordResponse> getStudentGrades(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long academicYearId) {
        return gradeService.getStudentGrades(studentId, academicYearId);
    }

    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<GradeRecordResponse> getSectionGrades(
            @PathVariable Long sectionId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam String subject,
            @RequestParam String term) {
        return gradeService.getSectionGrades(sectionId, academicYearId, subject, term);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<GradeRecordResponse> saveGrades(@Valid @RequestBody SaveGradeRequest req) {
        return gradeService.saveGrades(req);
    }
}
