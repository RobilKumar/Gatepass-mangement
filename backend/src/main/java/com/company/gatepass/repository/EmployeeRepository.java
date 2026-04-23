package com.company.gatepass.repository;

import com.company.gatepass.entity.Employee;
import com.company.gatepass.enums.EmployeeRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    // Fetches all employees with department, plant, and manager details loaded.
    @Override
    @EntityGraph(attributePaths = {"department", "plant", "reportingManager"})
    List<Employee> findAll();

    // Finds employees assigned to one role.
    List<Employee> findByRole(EmployeeRole role);

    // Finds employees who report to a specific manager.
    @EntityGraph(attributePaths = {"department", "plant", "reportingManager"})
    List<Employee> findByReportingManagerId(Long managerId);

    // Finds an employee by email for login.
    Optional<Employee> findByEmail(String email);

    // Finds an employee by employee code for login.
    Optional<Employee> findByEmployeeCode(String employeeCode);
}
