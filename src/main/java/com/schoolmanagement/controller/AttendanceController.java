package com.schoolmanagement.controller;

import com.schoolmanagement.dto.request.BulkStudentAttendanceRequest;
import com.schoolmanagement.dto.request.EmployeeAttendanceRequest;
import com.schoolmanagement.dto.request.LeaveRequestDto;
import com.schoolmanagement.dto.response.EmployeeAttendanceResponse;
import com.schoolmanagement.dto.response.LeaveRequestResponse;
import com.schoolmanagement.dto.response.StudentAttendanceResponse;
import com.schoolmanagement.enums.LeaveStatus;
import com.schoolmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ── Student Attendance ────────────────────────────────────────────────────

    @GetMapping("/students/section/{sectionId}")
    public List<StudentAttendanceResponse> getBySectionAndDate(
            @PathVariable Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getBySection(sectionId, date);
    }

    @GetMapping("/students/{studentId}/history")
    public List<StudentAttendanceResponse> getStudentHistory(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceService.getStudentHistory(studentId, from, to);
    }

    @PostMapping("/students/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'OFFICE_EMPLOYEE')")
    public List<StudentAttendanceResponse> saveBulk(@Valid @RequestBody BulkStudentAttendanceRequest req) {
        return attendanceService.saveBulk(req);
    }

    // ── Employee Attendance ───────────────────────────────────────────────────

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_EMPLOYEE')")
    public List<EmployeeAttendanceResponse> getEmployeeByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getEmployeeAttendanceByDate(date);
    }

    @GetMapping("/employees/{employeeId}/history")
    public List<EmployeeAttendanceResponse> getEmployeeHistory(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceService.getEmployeeHistory(employeeId, from, to);
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICE_EMPLOYEE')")
    public EmployeeAttendanceResponse saveEmployee(@Valid @RequestBody EmployeeAttendanceRequest req) {
        return attendanceService.saveEmployeeAttendance(req);
    }

    // ── Leave Requests ────────────────────────────────────────────────────────

    @GetMapping("/leaves")
    public List<LeaveRequestResponse> getLeaves(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveStatus status) {
        return attendanceService.getLeaves(employeeId, status);
    }

    @PostMapping("/leaves")
    public ResponseEntity<LeaveRequestResponse> applyLeave(@Valid @RequestBody LeaveRequestDto req) {
        return ResponseEntity.status(201).body(attendanceService.applyLeave(req));
    }

    @PatchMapping("/leaves/{id}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveRequestResponse decideLeave(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        boolean approved = "true".equalsIgnoreCase(body.get("approved"));
        return attendanceService.approveOrRejectLeave(id, approved, body.get("remarks"));
    }
}
