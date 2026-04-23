package com.company.gatepass.dto;

import com.company.gatepass.enums.GatepassType;
import com.company.gatepass.enums.HalfDaySession;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateGatepassRequest {
    private GatepassType gatepassType;
    private LocalDate requestDate;
    private LocalTime fromTime;
    private LocalTime toTime;
    private HalfDaySession halfDaySession;
    private Long fromPlantId;
    private Long toPlantId;
    private String outLocation;
    private String reason;
}
