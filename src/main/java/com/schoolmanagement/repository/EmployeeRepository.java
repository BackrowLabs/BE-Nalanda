package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCode(String employeeCode);

    Optional<Employee> findByIdAndActiveTrue(Long id);

    Optional<Employee> findByProfile_Id(java.util.UUID profileId);

    Optional<Employee> findByEmailIgnoreCaseAndActiveTrue(String email);

    @Query("""
            SELECT e FROM Employee e
            WHERE e.active = true
              AND (:department IS NULL OR e.department ilike :department)
              AND (:search IS NULL OR e.fullName ilike :search
                                   OR e.employeeCode ilike :search)
            """)
    Page<Employee> search(@Param("department") String department,
                          @Param("search") String search,
                          Pageable pageable);
}
