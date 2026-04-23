package com.company.gatepass.controller;

import com.company.gatepass.dto.ApprovalRequest;
import com.company.gatepass.dto.GatepassResponse;
import com.company.gatepass.dto.PageResponse;
import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.service.GatepassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager/gatepasses")
@RequiredArgsConstructor
public class ManagerController {
    private final GatepassService gatepassService;

    // Returns pending approvals assigned to the manager id sent in the request header.
    @GetMapping("/pending")
    public List<GatepassResponse> getPendingApprovals(@RequestHeader("X-Manager-Id") Long managerId) {
        return gatepassService.getPendingApprovals(managerId);
    }

    // Searches pending approvals for the manager with keyword and paging support.
    @GetMapping("/pending/search")
    public PageResponse<GatepassResponse> searchPendingApprovals(
            @RequestHeader("X-Manager-Id") Long managerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return gatepassService.searchGatepasses(
                keyword,
                null,
                null,
                null,
                null,
                null,
                managerId,
                null,
                GatepassStatus.PENDING_MANAGER_APPROVAL,
                page,
                size
        );
    }

    // Approves a gatepass through the PUT endpoint used by the frontend.
    @PutMapping("/{gatepassId}/approve")
    public GatepassResponse approveUsingPut(
            @RequestHeader("X-Manager-Id") Long managerId,
            @PathVariable Long gatepassId,
            @RequestBody ApprovalRequest request
    ) {
        return gatepassService.approve(managerId, gatepassId, request);
    }

    // Approves a gatepass through a POST endpoint for API compatibility.
    @PostMapping("/{gatepassId}/approve")
    public GatepassResponse approve(
            @RequestHeader("X-Manager-Id") Long managerId,
            @PathVariable Long gatepassId,
            @RequestBody ApprovalRequest request
    ) {
        return gatepassService.approve(managerId, gatepassId, request);
    }

    // Rejects a gatepass through the PUT endpoint used by the frontend.
    @PutMapping("/{gatepassId}/reject")
    public GatepassResponse rejectUsingPut(
            @RequestHeader("X-Manager-Id") Long managerId,
            @PathVariable Long gatepassId,
            @RequestBody ApprovalRequest request
    ) {
        return gatepassService.reject(managerId, gatepassId, request);
    }

    // Rejects a gatepass through a POST endpoint for API compatibility.
    @PostMapping("/{gatepassId}/reject")
    public GatepassResponse reject(
            @RequestHeader("X-Manager-Id") Long managerId,
            @PathVariable Long gatepassId,
            @RequestBody ApprovalRequest request
    ) {
        return gatepassService.reject(managerId, gatepassId, request);
    }
}
