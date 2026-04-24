package com.schoolmanagement.repository;

import com.schoolmanagement.entity.StudentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, Long> {

    Optional<StudentAttendance> findByStudentIdAndDate(Long studentId, LocalDate date);

    @Query("""
            SELECT a FROM StudentAttendance a
            JOIN FETCH a.student s
            WHERE s.section.id = :sectionId
              AND a.date = :date
            ORDER BY s.fullName
            """)
    List<StudentAttendance> findBySectionAndDate(@Param("sectionId") Long sectionId,
                                                  @Param("date") LocalDate date);

    @Query("""
            SELECT a FROM StudentAttendance a
            WHERE a.student.id = :studentId
              AND a.date BETWEEN :from AND :to
            ORDER BY a.date
            """)
    List<StudentAttendance> findByStudentAndDateRange(@Param("studentId") Long studentId,
                                                       @Param("from") LocalDate from,
                                                       @Param("to") LocalDate to);
}
