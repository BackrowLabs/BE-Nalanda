package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByAdmissionNumber(String admissionNumber);

    Optional<Student> findByIdAndActiveTrue(Long id);

    @Query("""
            SELECT s FROM Student s
            LEFT JOIN FETCH s.section sec
            LEFT JOIN FETCH sec.grade
            WHERE s.active = true
              AND (:academicYearId IS NULL OR s.academicYear.id = :academicYearId)
              AND (:sectionId     IS NULL OR s.section.id = :sectionId)
              AND (:search IS NULL OR s.fullName ilike :search
                                   OR s.admissionNumber ilike :search)
              AND (:sectionIds IS NULL OR s.section.id IN :sectionIds)
            """)
    Page<Student> search(@Param("academicYearId") Long academicYearId,
                         @Param("sectionId") Long sectionId,
                         @Param("search") String search,
                         @Param("sectionIds") List<Long> sectionIds,
                         Pageable pageable);
}
