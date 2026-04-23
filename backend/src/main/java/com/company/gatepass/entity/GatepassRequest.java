package com.company.gatepass.entity;

import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.enums.GatepassType;
import com.company.gatepass.enums.HalfDaySession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "gatepass_request")
public class GatepassRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gatepass_id")
    private Long id;

    @Column(name = "request_no", nullable = false, unique = true)
    private String requestNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Employee manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "gatepass_type", nullable = false)
    private GatepassType gatepassType;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "from_time", nullable = false)
    private LocalTime fromTime;

    @Column(name = "to_time", nullable = false)
    private LocalTime toTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "half_day_session")
    private HalfDaySession halfDaySession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_plant_id")
    private Plant fromPlant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_plant_id")
    private Plant toPlant;

    @Column(name = "out_location")
    private String outLocation;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GatepassStatus status = GatepassStatus.PENDING_MANAGER_APPROVAL;

    @Column(name = "manager_remarks")
    private String managerRemarks;

    @Column(name = "manager_action_date")
    private LocalDateTime managerActionDate;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Sets create and update timestamps before the request is first inserted.
    @PrePersist
    void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    // Refreshes the update timestamp before changes are saved.
    @PreUpdate
    void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
