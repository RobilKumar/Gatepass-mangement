package com.company.gatepass.dto;

import com.company.gatepass.enums.EmployeeRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEmployeeRequest {
    private String employeeCode;
    private String employeeName;
    private String email;
    private String password;
    private String mobile;
    private String designation;
    private Long departmentId;
    private Long plantId;
    private Long reportingManagerId;
    private EmployeeRole role;
}
