package com.schoolmanagement.repository;

import com.schoolmanagement.entity.FeeInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeInstallmentRepository extends JpaRepository<FeeInstallment, Long> {

    List<FeeInstallment> findByFeeStructureIdOrderByInstallmentNumber(Long feeStructureId);

    @Query("""
            SELECT fi FROM FeeInstallment fi
            JOIN FETCH fi.feeStructure fs
            WHERE fs.grade.id = :gradeId AND fs.academicYear.id = :yearId
            """)
    List<FeeInstallment> findByGradeAndYear(@Param("gradeId") Long gradeId,
                                             @Param("yearId") Long yearId);
}
