package com.company.gatepass.controller;

import com.company.gatepass.dto.GatepassResponse;
import com.company.gatepass.dto.PageResponse;
import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.enums.GatepassType;
import com.company.gatepass.service.GatepassService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrController {
    private final GatepassService gatepassService;

    // Returns HR gatepass records, optionally filtered by date range.
    @GetMapping("/gatepasses")
    public List<GatepassResponse> getGatepasses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return gatepassService.getAllForHr(fromDate, toDate);
    }

    // Searches all gatepasses for HR/Admin screens with date, time, person, type, status, and paging filters.
    @GetMapping("/gatepasses/search")
    public PageResponse<GatepassResponse> searchGatepasses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime fromTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime toTime,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) GatepassType gatepassType,
            @RequestParam(required = false) GatepassStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return gatepassService.searchGatepasses(
                keyword,
                fromDate,
                toDate,
                fromTime,
                toTime,
                employeeId,
                managerId,
                gatepassType,
                status,
                page,
                size
        );
    }
}
