package com.company.gatepass.service;

import com.company.gatepass.dto.EmployeeResponse;
import com.company.gatepass.dto.LoginRequest;
import com.company.gatepass.entity.Employee;
import com.company.gatepass.exception.ApiException;
import com.company.gatepass.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final EmployeeRepository employeeRepository;

    // Validates login id and password, then returns the employee profile for the session.
    @Transactional(readOnly = true)
    public EmployeeResponse login(LoginRequest request) {
        if (request.getLoginId() == null || request.getLoginId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Login ID is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        Employee employee = employeeRepository.findByEmployeeCode(request.getLoginId())
                .or(() -> employeeRepository.findByEmail(request.getLoginId()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid login ID or password"));

        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User is inactive");
        }
        if (!request.getPassword().equals(employee.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid login ID or password");
        }

        return EmployeeResponse.from(employee);
    }
}
