package com.schoolmanagement.controller;

import com.schoolmanagement.dto.request.CreateFeeStructureRequest;
import com.schoolmanagement.dto.request.UpdateFeeStructureRequest;
import com.schoolmanagement.dto.response.FeeStructureResponse;
import com.schoolmanagement.dto.response.StudentFeeResponse;
import com.schoolmanagement.service.FeeStructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fee-structures")
@RequiredArgsConstructor
public class FeeStructureController {

    private final FeeStructureService feeStructureService;

    @GetMapping
    public List<FeeStructureResponse> list(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long gradeId) {
        return feeStructureService.getAll(academicYearId, gradeId);
    }

    @GetMapping("/{id}")
    public FeeStructureResponse getById(@PathVariable Long id) {
        return feeStructureService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeStructureResponse> create(@Valid @RequestBody CreateFeeStructureRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feeStructureService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FeeStructureResponse update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateFeeStructureRequest req) {
        return feeStructureService.update(id, req);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> publish(@PathVariable Long id) {
        int count = feeStructureService.publishToStudents(id);
        return ResponseEntity.ok(Map.of("message", "Fees assigned to students", "count", count));
    }

    @GetMapping("/student/{studentId}")
    public List<StudentFeeResponse> getStudentFees(@PathVariable Long studentId) {
        return feeStructureService.getStudentFees(studentId);
    }

    @GetMapping("/installment/{installmentId}/fees")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_EMPLOYEE')")
    public List<StudentFeeResponse> getInstallmentFees(@PathVariable Long installmentId) {
        return feeStructureService.getInstallmentFees(installmentId);
    }
}
