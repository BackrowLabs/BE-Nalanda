package com.schoolmanagement.repository;

import com.schoolmanagement.entity.StudentFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentFeeRepository extends JpaRepository<StudentFee, Long> {

    boolean existsByStudentIdAndFeeInstallmentId(Long studentId, Long installmentId);

    boolean existsByFeeInstallmentId(Long feeInstallmentId);

    @Query("""
            SELECT sf FROM StudentFee sf
            JOIN FETCH sf.feeInstallment fi
            JOIN FETCH fi.feeStructure fs
            JOIN FETCH fs.grade
            WHERE sf.student.id = :studentId
            ORDER BY fs.feeType, fi.installmentNumber
            """)
    List<StudentFee> findByStudentId(@Param("studentId") Long studentId);

    @Query("""
            SELECT sf FROM StudentFee sf
            JOIN FETCH sf.student s
            JOIN FETCH sf.feeInstallment fi
            JOIN FETCH fi.feeStructure fs
            WHERE fi.id = :installmentId
            ORDER BY s.fullName
            """)
    List<StudentFee> findByInstallmentId(@Param("installmentId") Long installmentId);
}
