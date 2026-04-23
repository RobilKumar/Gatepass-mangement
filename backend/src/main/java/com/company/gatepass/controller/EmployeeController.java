package com.company.gatepass.controller;

import com.company.gatepass.dto.CreateEmployeeRequest;
import com.company.gatepass.dto.EmployeeResponse;
import com.company.gatepass.dto.PageResponse;
import com.company.gatepass.enums.EmployeeRole;
import com.company.gatepass.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    // Handles GET /api/employees and returns all employees.
    @GetMapping
    public List<EmployeeResponse> getEmployees() {
        return employeeService.getAllEmployees();
    }

    // Handles paged employee search for admin filters and dropdown lookups.
    @GetMapping("/search")
    public PageResponse<EmployeeResponse> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmployeeRole role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long managerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return employeeService.searchEmployees(keyword, role, active, managerId, page, size);
    }

    // Returns employees assigned to one reporting manager.
    @GetMapping("/manager/{managerId}")
    public List<EmployeeResponse> getEmployeesByManager(@PathVariable Long managerId) {
        return employeeService.getEmployeesByManager(managerId);
    }

    // Creates a new employee from the admin employee form.
    @PostMapping
    public EmployeeResponse createEmployee(@RequestBody CreateEmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    // Updates an existing employee selected in the admin employee table.
    @PutMapping("/{employeeId}")
    public EmployeeResponse updateEmployee(@PathVariable Long employeeId, @RequestBody CreateEmployeeRequest request) {
        return employeeService.updateEmployee(employeeId, request);
    }

    // Reactivates an employee account.
    @PutMapping("/{employeeId}/activate")
    public EmployeeResponse activateEmployee(@PathVariable Long employeeId) {
        return employeeService.activateEmployee(employeeId);
    }

    // Deactivates an employee account while keeping their records.
    @DeleteMapping("/{employeeId}")
    public void deactivateEmployee(@PathVariable Long employeeId) {
        employeeService.deactivateEmployee(employeeId);
    }
}
