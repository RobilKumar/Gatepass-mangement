package com.company.gatepass.dto;

import com.company.gatepass.entity.GatepassRequest;
import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.enums.GatepassType;
import com.company.gatepass.enums.HalfDaySession;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class GatepassResponse {
    private Long id;
    private String requestNo;
    private Long employeeId;
    private String employeeName;
    private Long managerId;
    private String managerName;
    private GatepassType gatepassType;
    private LocalDate requestDate;
    private LocalTime fromTime;
    private LocalTime toTime;
    private HalfDaySession halfDaySession;
    private String fromPlantName;
    private String toPlantName;
    private String outLocation;
    private String reason;
    private GatepassStatus status;
    private String managerRemarks;
    private LocalDateTime managerActionDate;
    private LocalDateTime checkoutTime;
    private LocalDateTime checkinTime;
    private LocalDateTime createdAt;

    // Converts a GatepassRequest entity into the response shape used by tables and reports.
    public static GatepassResponse from(GatepassRequest gatepass) {
        return GatepassResponse.builder()
                .id(gatepass.getId())
                .requestNo(gatepass.getRequestNo())
                .employeeId(gatepass.getEmployee().getId())
                .employeeName(gatepass.getEmployee().getEmployeeName())
                .managerId(gatepass.getManager().getId())
                .managerName(gatepass.getManager().getEmployeeName())
                .gatepassType(gatepass.getGatepassType())
                .requestDate(gatepass.getRequestDate())
                .fromTime(gatepass.getFromTime())
                .toTime(gatepass.getToTime())
                .halfDaySession(gatepass.getHalfDaySession())
                .fromPlantName(gatepass.getFromPlant() == null ? null : gatepass.getFromPlant().getPlantName())
                .toPlantName(gatepass.getToPlant() == null ? null : gatepass.getToPlant().getPlantName())
                .outLocation(gatepass.getOutLocation())
                .reason(gatepass.getReason())
                .status(gatepass.getStatus())
                .managerRemarks(gatepass.getManagerRemarks())
                .managerActionDate(gatepass.getManagerActionDate())
                .checkoutTime(gatepass.getCheckoutTime())
                .checkinTime(gatepass.getCheckinTime())
                .createdAt(gatepass.getCreatedAt())
                .build();
    }
}
