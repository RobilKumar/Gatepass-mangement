package com.company.gatepass.repository;

import com.company.gatepass.entity.GatepassRequest;
import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.enums.GatepassType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface GatepassRepository extends JpaRepository<GatepassRequest, Long>, JpaSpecificationExecutor<GatepassRequest> {
    // Fetches all gatepasses with related people and plant data already loaded.
    @Override
    @EntityGraph(attributePaths = {"employee", "manager", "fromPlant", "toPlant"})
    List<GatepassRequest> findAll();

    // Finds one employee's gatepass history newest first.
    @EntityGraph(attributePaths = {"employee", "manager", "fromPlant", "toPlant"})
    List<GatepassRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // Finds pending or filtered requests assigned to a manager newest first.
    @EntityGraph(attributePaths = {"employee", "manager", "fromPlant", "toPlant"})
    List<GatepassRequest> findByManagerIdAndStatusOrderByCreatedAtDesc(Long managerId, GatepassStatus status);

    // Finds gatepasses in a single workflow status newest first.
    @EntityGraph(attributePaths = {"employee", "manager", "fromPlant", "toPlant"})
    List<GatepassRequest> findByStatusOrderByCreatedAtDesc(GatepassStatus status);

    // Finds gatepasses within a request date range newest first.
    @EntityGraph(attributePaths = {"employee", "manager", "fromPlant", "toPlant"})
    List<GatepassRequest> findByRequestDateBetweenOrderByCreatedAtDesc(LocalDate fromDate, LocalDate toDate);

    // Runs the report query with optional filters and eager-loaded related data.
    @EntityGraph(attributePaths = {"employee", "manager", "fromPlant", "toPlant", "employee.department", "employee.plant"})
    @Query("""
            select g
            from GatepassRequest g
            where (:fromDate is null or g.requestDate >= :fromDate)
              and (:toDate is null or g.requestDate <= :toDate)
              and (:fromTime is null or g.fromTime >= :fromTime)
              and (:toTime is null or g.toTime <= :toTime)
              and (:employeeId is null or g.employee.id = :employeeId)
              and (:managerId is null or g.manager.id = :managerId)
              and (:gatepassType is null or g.gatepassType = :gatepassType)
              and (:status is null or g.status = :status)
            order by g.requestDate desc, g.createdAt desc
            """)
    List<GatepassRequest> findForReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime,
            @Param("employeeId") Long employeeId,
            @Param("managerId") Long managerId,
            @Param("gatepassType") GatepassType gatepassType,
            @Param("status") GatepassStatus status
    );
}
