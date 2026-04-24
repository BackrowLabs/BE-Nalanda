package com.schoolmanagement.service;

import com.schoolmanagement.dto.request.BulkStudentAttendanceRequest;
import com.schoolmanagement.dto.request.EmployeeAttendanceRequest;
import com.schoolmanagement.dto.request.LeaveRequestDto;
import com.schoolmanagement.dto.response.EmployeeAttendanceResponse;
import com.schoolmanagement.dto.response.LeaveRequestResponse;
import com.schoolmanagement.dto.response.StudentAttendanceResponse;
import com.schoolmanagement.entity.*;
import com.schoolmanagement.enums.LeaveStatus;
import com.schoolmanagement.enums.Role;
import com.schoolmanagement.exception.BusinessException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final StudentAttendanceRepository studentAttendanceRepository;
    private final EmployeeAttendanceRepository employeeAttendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;
    private final AcademicYearRepository academicYearRepository;

    // ── Student Attendance ────────────────────────────────────────────────────

    public List<StudentAttendanceResponse> getBySection(Long sectionId, LocalDate date) {
        return studentAttendanceRepository.findBySectionAndDate(sectionId, date)
                .stream().map(this::toStudentResponse).toList();
    }

    public List<StudentAttendanceResponse> getStudentHistory(Long studentId, LocalDate from, LocalDate to) {
        return studentAttendanceRepository.findByStudentAndDateRange(studentId, from, to)
                .stream().map(this::toStudentResponse).toList();
    }

    @Transactional
    public List<StudentAttendanceResponse> saveBulk(BulkStudentAttendanceRequest req) {
        Profile recorder = currentProfile();
        var year = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year", req.academicYearId()));

        return req.entries().stream().map(entry -> {
            var student = studentRepository.findById(entry.studentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", entry.studentId()));

            StudentAttendance attendance = studentAttendanceRepository
                    .findByStudentIdAndDate(entry.studentId(), req.date())
                    .orElse(StudentAttendance.builder()
                            .student(student)
                            .academicYear(year)
                            .date(req.date())
                            .build());

            attendance.setStatus(entry.status());
            attendance.setRemarks(entry.remarks());
            attendance.setRecordedBy(recorder);

            return toStudentResponse(studentAttendanceRepository.save(attendance));
        }).toList();
    }

    // ── Employee Attendance ───────────────────────────────────────────────────

    public List<EmployeeAttendanceResponse> getEmployeeAttendanceByDate(LocalDate date) {
        return employeeAttendanceRepository.findByDate(date)
                .stream().map(this::toEmployeeResponse).toList();
    }

    public List<EmployeeAttendanceResponse> getEmployeeHistory(Long employeeId, LocalDate from, LocalDate to) {
        return employeeAttendanceRepository.findByEmployeeAndDateRange(employeeId, from, to)
                .stream().map(this::toEmployeeResponse).toList();
    }

    @Transactional
    public EmployeeAttendanceResponse saveEmployeeAttendance(EmployeeAttendanceRequest req) {
        Profile recorder = currentProfile();
        var employee = employeeRepository.findById(req.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", req.employeeId()));

        EmployeeAttendance attendance = employeeAttendanceRepository
                .findByEmployeeIdAndDate(req.employeeId(), req.date())
                .orElse(EmployeeAttendance.builder().employee(employee).date(req.date()).build());

        attendance.setStatus(req.status());
        attendance.setCheckIn(req.checkIn());
        attendance.setCheckOut(req.checkOut());
        attendance.setRemarks(req.remarks());
        attendance.setRecordedBy(recorder);

        return toEmployeeResponse(employeeAttendanceRepository.save(attendance));
    }

    // ── Leave Requests ────────────────────────────────────────────────────────

    public List<LeaveRequestResponse> getLeaves(Long employeeId, LeaveStatus status) {
        Profile caller = currentProfile();
        if (caller.getRole() == Role.ADMIN) {
            return leaveRequestRepository.findAll(status)
                    .stream().map(this::toLeaveResponse).toList();
        }
        if (caller.getEmail() == null) return List.of();
        var emp = employeeRepository.findByEmailIgnoreCaseAndActiveTrue(caller.getEmail());
        if (emp.isEmpty()) return List.of();
        return leaveRequestRepository.findByEmployee(emp.get().getId(), status)
                .stream().map(this::toLeaveResponse).toList();
    }

    @Transactional
    public LeaveRequestResponse applyLeave(LeaveRequestDto req) {
        if (req.endDate().isBefore(req.startDate())) {
            throw new BusinessException("End date cannot be before start date");
        }
        Profile caller = currentProfile();
        Employee employee;
        if (caller.getRole() == Role.ADMIN) {
            if (req.employeeId() == null)
                throw new BusinessException("employeeId is required");
            employee = employeeRepository.findById(req.employeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", req.employeeId()));
        } else {
            if (caller.getEmail() == null)
                throw new BusinessException("No email linked to your account");
            employee = employeeRepository.findByEmailIgnoreCaseAndActiveTrue(caller.getEmail())
                    .orElseThrow(() -> new BusinessException("No employee record found for your account. Ask admin to ensure your employee email matches your login email."));
        }

        LeaveRequest leave = LeaveRequest.builder()
                .employee(employee)
                .leaveType(req.leaveType())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .reason(req.reason())
                .build();

        return toLeaveResponse(leaveRequestRepository.save(leave));
    }

    @Transactional
    public LeaveRequestResponse approveOrRejectLeave(Long leaveId, boolean approved, String remarks) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request", leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Leave request is already " + leave.getStatus().name().toLowerCase());
        }

        leave.setStatus(approved ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        leave.setApprovedBy(currentProfile());
        leave.setApprovedAt(Instant.now());
        leave.setRemarks(remarks);

        return toLeaveResponse(leaveRequestRepository.save(leave));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Profile currentProfile() {
        return (Profile) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private StudentAttendanceResponse toStudentResponse(StudentAttendance a) {
        return new StudentAttendanceResponse(
                a.getId(), a.getStudent().getId(), a.getStudent().getFullName(),
                a.getStudent().getAdmissionNumber(), a.getDate(), a.getStatus(), a.getRemarks());
    }

    private EmployeeAttendanceResponse toEmployeeResponse(EmployeeAttendance a) {
        return new EmployeeAttendanceResponse(
                a.getId(), a.getEmployee().getId(), a.getEmployee().getFullName(),
                a.getEmployee().getEmployeeCode(), a.getDate(), a.getStatus(),
                a.getCheckIn(), a.getCheckOut(), a.getRemarks());
    }

    private LeaveRequestResponse toLeaveResponse(LeaveRequest l) {
        return new LeaveRequestResponse(
                l.getId(), l.getEmployee().getId(), l.getEmployee().getFullName(),
                l.getEmployee().getEmployeeCode(), l.getLeaveType(),
                l.getStartDate(), l.getEndDate(),
                LeaveRequestResponse.calculateDays(l.getStartDate(), l.getEndDate()),
                l.getReason(), l.getStatus(),
                l.getApprovedBy() != null ? l.getApprovedBy().getFullName() : null,
                l.getApprovedAt(), l.getRemarks(), l.getCreatedAt());
    }
}
