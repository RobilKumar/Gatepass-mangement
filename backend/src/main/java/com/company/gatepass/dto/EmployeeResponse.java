package com.company.gatepass.dto;

import com.company.gatepass.entity.Employee;
import com.company.gatepass.enums.EmployeeRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String employeeName;
    private String email;
    private String mobile;
    private String designation;
    private Long departmentId;
    private String departmentName;
    private Long plantId;
    private String plantName;
    private Long reportingManagerId;
    private String reportingManagerName;
    private EmployeeRole role;
    private Boolean active;

    // Converts an Employee entity into the safe response shape sent to the frontend.
    public static EmployeeResponse from(Employee employee) {
        Employee manager = employee.getReportingManager();
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getEmployeeName())
                .email(employee.getEmail())
                .mobile(employee.getMobile())
                .designation(employee.getDesignation())
                .departmentId(employee.getDepartment() == null ? null : employee.getDepartment().getId())
                .departmentName(employee.getDepartment() == null ? null : employee.getDepartment().getDepartmentName())
                .plantId(employee.getPlant() == null ? null : employee.getPlant().getId())
                .plantName(employee.getPlant() == null ? null : employee.getPlant().getPlantName())
                .reportingManagerId(manager == null ? null : manager.getId())
                .reportingManagerName(manager == null ? null : manager.getEmployeeName())
                .role(employee.getRole())
                .active(employee.getActive())
                .build();
    }
}
