package com.company.gatepass.service;

import com.company.gatepass.dto.CreateEmployeeRequest;
import com.company.gatepass.dto.EmployeeResponse;
import com.company.gatepass.dto.PageResponse;
import com.company.gatepass.entity.Employee;
import com.company.gatepass.enums.EmployeeRole;
import com.company.gatepass.exception.ApiException;
import jakarta.persistence.criteria.JoinType;
import com.company.gatepass.repository.DepartmentRepository;
import com.company.gatepass.repository.EmployeeRepository;
import com.company.gatepass.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PlantRepository plantRepository;

    // Returns every employee with related department, plant, and manager details.
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    // Returns employees who report to the selected manager.
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByReportingManagerId(managerId).stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    // Searches employees with optional filters and paging.
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(
            String keyword,
            EmployeeRole role,
            Boolean active,
            Long managerId,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 5), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("employeeName").ascending());

        Page<Employee> result = employeeRepository.findAll(employeeSpec(keyword, role, active, managerId), pageable);
        return PageResponse.<EmployeeResponse>builder()
                .content(result.getContent().stream().map(EmployeeResponse::from).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    // Creates a new employee after validating and applying request fields.
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        apply(employee, request);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    // Updates an existing employee after loading the saved record.
    @SuppressWarnings("null")
    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, CreateEmployeeRequest request) {
        Employee employee = getEmployee(employeeId);
        apply(employee, request);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    // Soft-deletes an employee by marking them inactive.
    @Transactional
    public void deactivateEmployee(Long employeeId) {
        Employee employee = getEmployee(employeeId);
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    // Reactivates an inactive employee account.
    @Transactional
    public EmployeeResponse activateEmployee(Long employeeId) {
        Employee employee = getEmployee(employeeId);
        employee.setActive(true);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    // Loads one employee or raises a 404 API error when the id is invalid.
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    // Copies validated request data and related master records onto an employee entity.
    @SuppressWarnings("null")
    private void apply(Employee employee, CreateEmployeeRequest request) {
        validateRequest(employee, request);

        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setEmployeeName(request.getEmployeeName());
        employee.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            employee.setPassword(request.getPassword());
        } else if (employee.getPassword() == null) {
            employee.setPassword("123456");
        }
        employee.setMobile(request.getMobile());
        employee.setDesignation(request.getDesignation());
        employee.setRole(request.getRole());

        employee.setDepartment(request.getDepartmentId() == null ? null :
                departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Department not found")));

        employee.setPlant(request.getPlantId() == null ? null :
                plantRepository.findById(request.getPlantId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plant not found")));

        employee.setReportingManager(request.getReportingManagerId() == null ? null :
                employeeRepository.findById(request.getReportingManagerId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reporting manager not found")));
    }

    // Checks required fields and prevents invalid reporting manager assignments.
    private void validateRequest(Employee employee, CreateEmployeeRequest request) {
        if (request.getEmployeeCode() == null || request.getEmployeeCode().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Employee code is required");
        }
        if (request.getEmployeeName() == null || request.getEmployeeName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Employee name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (request.getRole() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        if (request.getRole() == EmployeeRole.EMPLOYEE && request.getReportingManagerId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reporting manager is required for employee role");
        }
        if (employee.getId() != null && request.getReportingManagerId() != null &&
                employee.getId().equals(request.getReportingManagerId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Employee cannot be their own reporting manager");
        }
    }

    // Builds the dynamic database filter used by paged employee searches.
    private Specification<Employee> employeeSpec(String keyword, EmployeeRole role, Boolean active, Long managerId) {
        return (root, query, builder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("department", JoinType.LEFT);
                root.fetch("plant", JoinType.LEFT);
                root.fetch("reportingManager", JoinType.LEFT);
            }
            query.distinct(true);

            var predicates = builder.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates = builder.and(predicates, builder.or(
                        builder.like(builder.lower(root.get("employeeCode")), like),
                        builder.like(builder.lower(root.get("employeeName")), like),
                        builder.like(builder.lower(root.get("email")), like),
                        builder.like(builder.lower(root.get("mobile")), like)
                ));
            }
            if (role != null) {
                predicates = builder.and(predicates, builder.equal(root.get("role"), role));
            }
            if (active != null) {
                predicates = builder.and(predicates, builder.equal(root.get("active"), active));
            }
            if (managerId != null) {
                predicates = builder.and(predicates, builder.equal(root.get("reportingManager").get("id"), managerId));
            }
            return predicates;
        };
    }
}
