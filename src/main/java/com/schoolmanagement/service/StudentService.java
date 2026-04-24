package com.schoolmanagement.service;

import com.schoolmanagement.dto.request.CreateStudentRequest;
import com.schoolmanagement.dto.request.PromoteStudentRequest;
import com.schoolmanagement.dto.request.UpdateStudentRequest;
import com.schoolmanagement.dto.response.PageResponse;
import com.schoolmanagement.dto.response.StudentResponse;
import com.schoolmanagement.dto.response.StudentSummaryResponse;
import com.schoolmanagement.entity.Profile;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.enums.Role;
import com.schoolmanagement.exception.BusinessException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final NotificationService notificationService;

    public PageResponse<StudentSummaryResponse> getStudents(Long academicYearId, Long sectionId,
                                                            String search, int page, int size) {
        Long resolvedYearId = resolveYearId(academicYearId);

        var pageable = PageRequest.of(page, size, Sort.by("fullName"));
        var result = studentRepository.search(resolvedYearId, sectionId,
                search == null || search.isBlank() ? null : "%" + search + "%",
                null, pageable);

        return PageResponse.of(result, this::toSummary);
    }

    public StudentResponse getById(Long id) {
        Student student = studentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        return toResponse(student);
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest req) {
        String admissionNumber = req.admissionNumber() != null && !req.admissionNumber().isBlank()
                ? req.admissionNumber()
                : generateAdmissionNumber();

        if (studentRepository.existsByAdmissionNumber(admissionNumber)) {
            throw new BusinessException("Admission number already exists: " + admissionNumber);
        }

        var section = sectionRepository.findById(req.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));
        var year = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year", req.academicYearId()));

        Student student = Student.builder()
                .admissionNumber(admissionNumber)
                .fullName(req.fullName())
                .dateOfBirth(req.dateOfBirth())
                .gender(req.gender())
                .address(req.address())
                .photoUrl(req.photoUrl())
                .parentName(req.parentName())
                .parentPhone(req.parentPhone())
                .parentEmail(req.parentEmail())
                .section(section)
                .academicYear(year)
                .enrollmentDate(req.enrollmentDate() != null ? req.enrollmentDate() : LocalDate.now())
                .build();

        Student saved = studentRepository.save(student);

        Profile caller = currentProfile();
        if (caller != null && caller.getRole() == Role.OFFICE_EMPLOYEE) {
            notificationService.create(
                    "New student added: " + saved.getFullName() + " (" + saved.getAdmissionNumber() + ")",
                    Role.ADMIN,
                    saved.getId(),
                    caller.getFullName()
            );
        }

        return toResponse(saved);
    }

    @Transactional
    public StudentResponse promote(Long id, PromoteStudentRequest req) {
        Student student = studentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        var section = sectionRepository.findById(req.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));
        var year = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year", req.academicYearId()));

        student.setSection(section);
        student.setAcademicYear(year);

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, UpdateStudentRequest req) {
        Student student = studentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        var section = sectionRepository.findById(req.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));

        student.setFullName(req.fullName());
        student.setSection(section);
        student.setDateOfBirth(req.dateOfBirth());
        student.setGender(req.gender());
        student.setAddress(req.address());
        student.setPhotoUrl(req.photoUrl());
        student.setParentName(req.parentName());
        student.setParentPhone(req.parentPhone());
        student.setParentEmail(req.parentEmail());

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void deactivate(Long id) {
        Student student = studentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        student.setActive(false);
        studentRepository.save(student);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Profile currentProfile() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Profile p)) return null;
        return p;
    }

    private Long resolveYearId(Long requested) {
        if (requested != null) return requested;
        return academicYearRepository.findByCurrentTrue()
                .orElseThrow(() -> new BusinessException("No current academic year configured"))
                .getId();
    }

    private String generateAdmissionNumber() {
        return "ADM-" + System.currentTimeMillis();
    }

    private StudentSummaryResponse toSummary(Student s) {
        return new StudentSummaryResponse(
                s.getId(),
                s.getAdmissionNumber(),
                s.getFullName(),
                s.getSection() != null ? s.getSection().getFullName() : null,
                s.getAcademicYear() != null ? s.getAcademicYear().getName() : null,
                s.getParentPhone(),
                s.isActive()
        );
    }

    private StudentResponse toResponse(Student s) {
        var docs = s.getDocuments().stream()
                .map(d -> new StudentResponse.DocumentResponse(d.getId(), d.getName(), d.getUrl(), d.getUploadedAt()))
                .toList();

        return new StudentResponse(
                s.getId(),
                s.getAdmissionNumber(),
                s.getFullName(),
                s.getDateOfBirth(),
                s.getGender(),
                s.getAddress(),
                s.getPhotoUrl(),
                s.getParentName(),
                s.getParentPhone(),
                s.getParentEmail(),
                s.getSection() != null ? s.getSection().getId() : null,
                s.getSection() != null ? s.getSection().getFullName() : null,
                s.getAcademicYear() != null ? s.getAcademicYear().getId() : null,
                s.getAcademicYear() != null ? s.getAcademicYear().getName() : null,
                s.getEnrollmentDate(),
                s.isActive(),
                docs,
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
