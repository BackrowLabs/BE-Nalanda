package com.schoolmanagement.repository;

import com.schoolmanagement.entity.EmployeeAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, Long> {

    Optional<EmployeeAttendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    @Query("""
            SELECT a FROM EmployeeAttendance a
            JOIN FETCH a.employee
            WHERE a.date = :date
            ORDER BY a.employee.fullName
            """)
    List<EmployeeAttendance> findByDate(@Param("date") LocalDate date);

    @Query("""
            SELECT a FROM EmployeeAttendance a
            WHERE a.employee.id = :employeeId
              AND a.date BETWEEN :from AND :to
            ORDER BY a.date
            """)
    List<EmployeeAttendance> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                         @Param("from") LocalDate from,
                                                         @Param("to") LocalDate to);
}
