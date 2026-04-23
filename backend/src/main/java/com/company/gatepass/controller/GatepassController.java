package com.company.gatepass.controller;

import com.company.gatepass.dto.CreateGatepassRequest;
import com.company.gatepass.dto.GatepassResponse;
import com.company.gatepass.service.GatepassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GatepassController {
    private final GatepassService gatepassService;

    // Accepts a new employee gatepass request and forwards it to the service layer.
    @PostMapping({"/api/gatepasses", "/api/gatepasses/request", "/api/gatepass/request"})
    public GatepassResponse createGatepass(
            @RequestHeader("X-Employee-Id") Long employeeId,
            @RequestBody CreateGatepassRequest request
    ) {
        return gatepassService.createGatepass(employeeId, request);
    }

    // Returns the current employee's gatepass history.
    @GetMapping({"/api/gatepasses/my", "/api/gatepass/my"})
    public List<GatepassResponse> getMyGatepasses(@RequestHeader("X-Employee-Id") Long employeeId) {
        return gatepassService.getMyGatepasses(employeeId);
    }
}
