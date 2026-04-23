package com.company.gatepass.controller;

import com.company.gatepass.dto.EmployeeResponse;
import com.company.gatepass.dto.LoginRequest;
import com.company.gatepass.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // Authenticates a user from login id and password.
    @PostMapping("/login")
    public EmployeeResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
