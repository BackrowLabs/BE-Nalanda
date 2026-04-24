package com.schoolmanagement.service;

import com.schoolmanagement.dto.request.SaveGradeRequest;
import com.schoolmanagement.dto.response.GradeRecordResponse;
import com.schoolmanagement.entity.GradeRecord;
import com.schoolmanagement.entity.Profile;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRecordRepository gradeRecordRepository;
    private final StudentRepository studentRepository;
    private final AcademicYearRepository academicYearRepository;

    public List<GradeRecordResponse> getStudentGrades(Long studentId, Long academicYearId) {
        Long resolvedYearId = resolveYearId(academicYearId);
        return gradeRecordRepository.findByStudentAndYear(studentId, resolvedYearId)
                .stream().map(this::toResponse).toList();
    }

    public List<GradeRecordResponse> getSectionGrades(Long sectionId, Long academicYearId,
                                                        String subject, String term) {
        Long resolvedYearId = resolveYearId(academicYearId);
        return gradeRecordRepository.findBySectionYearSubjectTerm(sectionId, resolvedYearId, subject, term)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<GradeRecordResponse> saveGrades(SaveGradeRequest req) {
        Profile recorder = currentProfile();
        var year = academicYearRepository.findById(req.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year", req.academicYearId()));

        return req.entries().stream().map(entry -> {
            var student = studentRepository.findById(entry.studentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", entry.studentId()));

            GradeRecord record = gradeRecordRepository
                    .findByStudentIdAndAcademicYearIdAndSubjectAndTerm(
                            entry.studentId(), req.academicYearId(), req.subject(), req.term())
                    .orElse(GradeRecord.builder()
                            .student(student)
                            .academicYear(year)
                            .subject(req.subject())
                            .term(req.term())
                            .build());

            record.setLetterGrade(entry.letterGrade());
            record.setMarks(entry.marks());
            record.setRemarks(entry.remarks());
            record.setRecordedBy(recorder);

            return toResponse(gradeRecordRepository.save(record));
        }).toList();
    }

    private Long resolveYearId(Long requested) {
        if (requested != null) return requested;
        return academicYearRepository.findByCurrentTrue()
                .orElseThrow(() -> new com.schoolmanagement.exception.BusinessException("No current academic year configured"))
                .getId();
    }

    private Profile currentProfile() {
        return (Profile) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private GradeRecordResponse toResponse(GradeRecord g) {
        var student = g.getStudent();
        return new GradeRecordResponse(
                g.getId(), student.getId(), student.getFullName(), student.getAdmissionNumber(),
                student.getSection() != null ? student.getSection().getFullName() : null,
                g.getAcademicYear().getId(), g.getAcademicYear().getName(),
                g.getSubject(), g.getTerm(), g.getLetterGrade(), g.getMarks(),
                g.getRemarks(), g.getUpdatedAt());
    }
}
