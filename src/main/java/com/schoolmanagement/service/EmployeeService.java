package com.schoolmanagement.service;

import com.schoolmanagement.dto.request.CreateEmployeeRequest;
import com.schoolmanagement.dto.request.UpdateEmployeeRequest;
import com.schoolmanagement.dto.response.EmployeeResponse;
import com.schoolmanagement.dto.response.EmployeeSummaryResponse;
import com.schoolmanagement.dto.response.PageResponse;
import com.schoolmanagement.entity.Employee;
import com.schoolmanagement.exception.BusinessException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public PageResponse<EmployeeSummaryResponse> getEmployees(String department, String search,
                                                               int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("fullName"));
        var result = employeeRepository.search(
                department == null || department.isBlank() ? null : department,
                search == null || search.isBlank() ? null : "%" + search + "%",
                pageable);
        return PageResponse.of(result, this::toSummary);
    }

    public EmployeeResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    public java.util.Optional<EmployeeResponse> findByEmail(String email) {
        return employeeRepository.findByEmailIgnoreCaseAndActiveTrue(email).map(this::toResponse);
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest req) {
        String code = req.employeeCode() != null && !req.employeeCode().isBlank()
                ? req.employeeCode()
                : generateCode();

        if (employeeRepository.existsByEmployeeCode(code)) {
            throw new BusinessException("Employee code already exists: " + code);
        }

        Employee employee = Employee.builder()
                .employeeCode(code)
                .fullName(req.fullName())
                .designation(req.designation())
                .department(req.department())
                .phone(req.phone())
                .email(req.email())
                .dateOfBirth(req.dateOfBirth())
                .gender(req.gender())
                .address(req.address())
                .joinDate(req.joinDate() != null ? req.joinDate() : LocalDate.now())
                .monthlySalary(req.monthlySalary())
                .build();

        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse update(Long id, UpdateEmployeeRequest req) {
        Employee employee = findActive(id);

        employee.setFullName(req.fullName());
        employee.setDesignation(req.designation());
        employee.setDepartment(req.department());
        employee.setPhone(req.phone());
        employee.setEmail(req.email());
        employee.setDateOfBirth(req.dateOfBirth());
        employee.setGender(req.gender());
        employee.setAddress(req.address());
        employee.setJoinDate(req.joinDate());
        employee.setMonthlySalary(req.monthlySalary());

        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deactivate(Long id) {
        Employee employee = findActive(id);
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Employee findActive(Long id) {
        return employeeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    private String generateCode() {
        return "EMP-" + System.currentTimeMillis();
    }

    private EmployeeSummaryResponse toSummary(Employee e) {
        return new EmployeeSummaryResponse(
                e.getId(), e.getEmployeeCode(), e.getFullName(),
                e.getDesignation(), e.getDepartment(), e.getPhone(), e.isActive());
    }

    private EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(
                e.getId(), e.getEmployeeCode(), e.getFullName(),
                e.getDesignation(), e.getDepartment(), e.getPhone(), e.getEmail(),
                e.getDateOfBirth(), e.getGender(), e.getAddress(),
                e.getJoinDate(), e.getMonthlySalary(), e.isActive(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
