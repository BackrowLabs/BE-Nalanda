package com.schoolmanagement.repository;

import com.schoolmanagement.entity.LeaveRequest;
import com.schoolmanagement.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("""
            SELECT l FROM LeaveRequest l JOIN FETCH l.employee
            WHERE (:status IS NULL OR l.status = :status)
            ORDER BY l.createdAt DESC
            """)
    List<LeaveRequest> findAll(@Param("status") LeaveStatus status);

    @Query("""
            SELECT l FROM LeaveRequest l JOIN FETCH l.employee
            WHERE l.employee.id = :employeeId
              AND (:status IS NULL OR l.status = :status)
            ORDER BY l.createdAt DESC
            """)
    List<LeaveRequest> findByEmployee(@Param("employeeId") Long employeeId,
                                      @Param("status") LeaveStatus status);
}
