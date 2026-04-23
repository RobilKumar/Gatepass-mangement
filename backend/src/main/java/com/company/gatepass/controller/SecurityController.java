package com.company.gatepass.controller;

import com.company.gatepass.dto.GatepassResponse;
import com.company.gatepass.dto.PageResponse;
import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.service.GatepassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security/gatepasses")
@RequiredArgsConstructor
public class SecurityController {
    private final GatepassService gatepassService;

    // Returns approved gatepasses that security can check out.
    @GetMapping("/approved")
    public List<GatepassResponse> getApprovedGatepasses() {
        return gatepassService.getApprovedForSecurity();
    }

    // Searches approved gatepasses waiting for checkout.
    @GetMapping("/approved/search")
    public PageResponse<GatepassResponse> searchApprovedGatepasses(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return gatepassService.searchGatepasses(keyword, null, null, null, null, null, null,
                null, GatepassStatus.APPROVED_BY_MANAGER, page, size);
    }

    // Returns checked-out gatepasses that security can check in.
    @GetMapping("/checked-out")
    public List<GatepassResponse> getCheckedOutGatepasses() {
        return gatepassService.getCheckedOutForSecurity();
    }

    // Searches checked-out gatepasses waiting for checkin.
    @GetMapping("/checked-out/search")
    public PageResponse<GatepassResponse> searchCheckedOutGatepasses(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return gatepassService.searchGatepasses(keyword, null, null, null, null, null, null,
                null, GatepassStatus.CHECKED_OUT, page, size);
    }

    // Marks an approved gatepass as checked out.
    @PutMapping("/{gatepassId}/checkout")
    public GatepassResponse checkout(@PathVariable Long gatepassId) {
        return gatepassService.checkout(gatepassId);
    }

    // Marks a checked-out gatepass as checked in.
    @PutMapping("/{gatepassId}/checkin")
    public GatepassResponse checkin(@PathVariable Long gatepassId) {
        return gatepassService.checkin(gatepassId);
    }
}
