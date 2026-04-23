package com.company.gatepass.controller;

import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.enums.GatepassType;
import com.company.gatepass.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    // Generates and returns the filtered gatepass movement Excel report.
    @GetMapping("/gatepass-movement.xlsx")
    public ResponseEntity<byte[]> downloadMovementReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime fromTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime toTime,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) GatepassType gatepassType,
            @RequestParam(required = false) GatepassStatus status
    ) {
        byte[] report = reportService.generateMovementReport(
                fromDate,
                toDate,
                fromTime,
                toTime,
                employeeId,
                managerId,
                gatepassType,
                status
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("gatepass-movement-report.xlsx")
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(report);
    }
}
