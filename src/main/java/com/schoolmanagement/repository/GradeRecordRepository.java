package com.schoolmanagement.repository;

import com.schoolmanagement.entity.GradeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GradeRecordRepository extends JpaRepository<GradeRecord, Long> {

    Optional<GradeRecord> findByStudentIdAndAcademicYearIdAndSubjectAndTerm(
            Long studentId, Long yearId, String subject, String term);

    @Query("""
            SELECT g FROM GradeRecord g
            WHERE g.student.id = :studentId
              AND g.academicYear.id = :yearId
            ORDER BY g.term, g.subject
            """)
    List<GradeRecord> findByStudentAndYear(@Param("studentId") Long studentId,
                                            @Param("yearId") Long yearId);

    @Query("""
            SELECT g FROM GradeRecord g
            JOIN FETCH g.student s
            WHERE s.section.id = :sectionId
              AND g.academicYear.id = :yearId
              AND g.subject = :subject
              AND g.term = :term
            ORDER BY s.fullName
            """)
    List<GradeRecord> findBySectionYearSubjectTerm(@Param("sectionId") Long sectionId,
                                                     @Param("yearId") Long yearId,
                                                     @Param("subject") String subject,
                                                     @Param("term") String term);
}
