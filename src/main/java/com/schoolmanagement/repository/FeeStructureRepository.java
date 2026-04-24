package com.schoolmanagement.repository;

import com.schoolmanagement.entity.FeeStructure;
import com.schoolmanagement.enums.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    boolean existsByAcademicYearIdAndGradeIdAndFeeType(Long yearId, Long gradeId, FeeType feeType);

    @Query("""
            SELECT fs FROM FeeStructure fs
            JOIN FETCH fs.grade
            JOIN FETCH fs.academicYear
            WHERE (:yearId IS NULL OR fs.academicYear.id = :yearId)
              AND (:gradeId IS NULL OR fs.grade.id = :gradeId)
            ORDER BY fs.grade.orderNum, fs.feeType
            """)
    List<FeeStructure> findAllFiltered(@Param("yearId") Long yearId, @Param("gradeId") Long gradeId);

    @Query("""
            SELECT fs FROM FeeStructure fs
            JOIN FETCH fs.installments
            WHERE fs.grade.id = :gradeId AND fs.academicYear.id = :yearId
            """)
    List<FeeStructure> findWithInstallmentsByGradeAndYear(@Param("gradeId") Long gradeId,
                                                           @Param("yearId") Long yearId);
}
