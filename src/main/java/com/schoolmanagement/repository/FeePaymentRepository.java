package com.schoolmanagement.repository;

import com.schoolmanagement.entity.FeePayment;
import com.schoolmanagement.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {

    Optional<FeePayment> findByReceiptNumber(String receiptNumber);

    Optional<FeePayment> findFirstByStudentFeeIdAndStatusOrderByCreatedAtDesc(
            Long studentFeeId, PaymentStatus status);

    @Query(value = """
            SELECT p FROM FeePayment p
            JOIN FETCH p.studentFee sf
            JOIN FETCH sf.student s
            LEFT JOIN FETCH s.section
            JOIN FETCH sf.feeInstallment fi
            JOIN FETCH fi.feeStructure
            JOIN FETCH p.collectedBy
            WHERE p.status = :status
            ORDER BY p.createdAt DESC
            """,
            countQuery = "SELECT COUNT(p) FROM FeePayment p WHERE p.status = :status")
    Page<FeePayment> findByStatus(@Param("status") PaymentStatus status, Pageable pageable);

    @Query("""
            SELECT p FROM FeePayment p
            JOIN FETCH p.collectedBy
            WHERE p.studentFee.student.id = :studentId
            ORDER BY p.createdAt DESC
            """)
    List<FeePayment> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(p) FROM FeePayment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);
}
