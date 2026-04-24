package com.schoolmanagement.controller;

import com.schoolmanagement.dto.request.CreateStudentRequest;
import com.schoolmanagement.dto.request.PromoteStudentRequest;
import com.schoolmanagement.dto.request.UpdateStudentRequest;
import com.schoolmanagement.dto.response.PageResponse;
import com.schoolmanagement.dto.response.StudentResponse;
import com.schoolmanagement.dto.response.StudentSummaryResponse;
import com.schoolmanagement.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public PageResponse<StudentSummaryResponse> list(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return studentService.getStudents(academicYearId, sectionId, search, page, size);
    }

    @GetMapping("/{id}")
    public StudentResponse getById(@PathVariable Long id) {
        return studentService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_EMPLOYEE')")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_EMPLOYEE')")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest req) {
        return studentService.update(id, req);
    }

    @PatchMapping("/{id}/promote")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_EMPLOYEE', 'TEACHER')")
    public StudentResponse promote(@PathVariable Long id, @Valid @RequestBody PromoteStudentRequest req) {
        return studentService.promote(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        studentService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
