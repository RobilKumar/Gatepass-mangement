package com.company.gatepass.service;

import com.company.gatepass.dto.ApprovalRequest;
import com.company.gatepass.dto.CreateGatepassRequest;
import com.company.gatepass.dto.GatepassResponse;
import com.company.gatepass.dto.PageResponse;
import com.company.gatepass.entity.Employee;
import com.company.gatepass.entity.GatepassRequest;
import com.company.gatepass.entity.Plant;
import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.enums.GatepassType;
import com.company.gatepass.exception.ApiException;
import jakarta.persistence.criteria.JoinType;
import com.company.gatepass.repository.EmployeeRepository;
import com.company.gatepass.repository.GatepassRepository;
import com.company.gatepass.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GatepassService {
    private final GatepassRepository gatepassRepository;
    private final EmployeeRepository employeeRepository;
    private final PlantRepository plantRepository;

    // Creates a new gatepass request after checking the employee and request details.
    @Transactional
    public GatepassResponse createGatepass(Long employeeId, CreateGatepassRequest request) {
        Employee employee = getEmployee(employeeId);
        if (employee.getReportingManager() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reporting manager is not assigned to employee");
        }

        validateGatepassRequest(request);

        GatepassRequest gatepass = new GatepassRequest();
        gatepass.setRequestNo(generateRequestNo());
        gatepass.setEmployee(employee);
        gatepass.setManager(employee.getReportingManager());
        gatepass.setGatepassType(request.getGatepassType());
        gatepass.setRequestDate(request.getRequestDate());
        gatepass.setFromTime(request.getFromTime());
        gatepass.setToTime(request.getToTime());
        gatepass.setHalfDaySession(request.getHalfDaySession());
        gatepass.setFromPlant(getPlantOrNull(request.getFromPlantId()));
        gatepass.setToPlant(getPlantOrNull(request.getToPlantId()));
        gatepass.setOutLocation(request.getOutLocation());
        gatepass.setReason(request.getReason());
        gatepass.setStatus(GatepassStatus.PENDING_MANAGER_APPROVAL);

        return GatepassResponse.from(gatepassRepository.save(gatepass));
    }

    // Returns all gatepass requests created by one employee.
    @Transactional(readOnly = true)
    public List<GatepassResponse> getMyGatepasses(Long employeeId) {
        return gatepassRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(GatepassResponse::from)
                .toList();
    }

    // Returns requests waiting for a specific manager's approval.
    @Transactional(readOnly = true)
    public List<GatepassResponse> getPendingApprovals(Long managerId) {
        return gatepassRepository.findByManagerIdAndStatusOrderByCreatedAtDesc(
                        managerId,
                        GatepassStatus.PENDING_MANAGER_APPROVAL
                ).stream()
                .map(GatepassResponse::from)
                .toList();
    }

    // Returns HR movement records, optionally limited by request date range.
    @Transactional(readOnly = true)
    public List<GatepassResponse> getAllForHr(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return gatepassRepository.findByRequestDateBetweenOrderByCreatedAtDesc(fromDate, toDate).stream()
                    .map(GatepassResponse::from)
                    .toList();
        }

        return gatepassRepository.findAll().stream()
                .map(GatepassResponse::from)
                .toList();
    }

    // Returns approved requests that are ready for security checkout.
    @Transactional(readOnly = true)
    public List<GatepassResponse> getApprovedForSecurity() {
        return gatepassRepository.findByStatusOrderByCreatedAtDesc(GatepassStatus.APPROVED_BY_MANAGER).stream()
                .map(GatepassResponse::from)
                .toList();
    }

    // Returns checked-out requests that are waiting for security checkin.
    @Transactional(readOnly = true)
    public List<GatepassResponse> getCheckedOutForSecurity() {
        return gatepassRepository.findByStatusOrderByCreatedAtDesc(GatepassStatus.CHECKED_OUT).stream()
                .map(GatepassResponse::from)
                .toList();
    }

    // Searches gatepasses with filters and wraps the result in paging metadata.
    @Transactional(readOnly = true)
    public PageResponse<GatepassResponse> searchGatepasses(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            LocalTime fromTime,
            LocalTime toTime,
            Long employeeId,
            Long managerId,
            GatepassType gatepassType,
            GatepassStatus status,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 5), 100);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("requestDate").descending().and(Sort.by("createdAt").descending())
        );

        Page<GatepassRequest> result = gatepassRepository.findAll(
                gatepassSpec(keyword, fromDate, toDate, fromTime, toTime, employeeId, managerId, gatepassType, status),
                pageable
        );

        return PageResponse.<GatepassResponse>builder()
                .content(result.getContent().stream().map(GatepassResponse::from).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    // Approves a pending gatepass after confirming it belongs to the manager.
    @Transactional
    public GatepassResponse approve(Long managerId, Long gatepassId, ApprovalRequest request) {
        GatepassRequest gatepass = getGatepass(gatepassId);
        validateManagerOwnership(managerId, gatepass);
        validateStatus(gatepass, GatepassStatus.PENDING_MANAGER_APPROVAL);

        gatepass.setStatus(GatepassStatus.APPROVED_BY_MANAGER);
        gatepass.setManagerRemarks(request.getRemarks());
        gatepass.setManagerActionDate(LocalDateTime.now());
        return GatepassResponse.from(gatepassRepository.save(gatepass));
    }

    // Rejects a pending gatepass after confirming it belongs to the manager.
    @Transactional
    public GatepassResponse reject(Long managerId, Long gatepassId, ApprovalRequest request) {
        GatepassRequest gatepass = getGatepass(gatepassId);
        validateManagerOwnership(managerId, gatepass);
        validateStatus(gatepass, GatepassStatus.PENDING_MANAGER_APPROVAL);

        gatepass.setStatus(GatepassStatus.REJECTED_BY_MANAGER);
        gatepass.setManagerRemarks(request.getRemarks());
        gatepass.setManagerActionDate(LocalDateTime.now());
        return GatepassResponse.from(gatepassRepository.save(gatepass));
    }

    // Moves an approved gatepass into checked-out status and records checkout time.
    @Transactional
    public GatepassResponse checkout(Long gatepassId) {
        GatepassRequest gatepass = getGatepass(gatepassId);
        validateStatus(gatepass, GatepassStatus.APPROVED_BY_MANAGER);

        gatepass.setStatus(GatepassStatus.CHECKED_OUT);
        gatepass.setCheckoutTime(LocalDateTime.now());
        return GatepassResponse.from(gatepassRepository.save(gatepass));
    }

    // Moves a checked-out gatepass into checked-in status and records return time.
    @Transactional
    public GatepassResponse checkin(Long gatepassId) {
        GatepassRequest gatepass = getGatepass(gatepassId);
        validateStatus(gatepass, GatepassStatus.CHECKED_OUT);

        gatepass.setStatus(GatepassStatus.CHECKED_IN);
        gatepass.setCheckinTime(LocalDateTime.now());
        return GatepassResponse.from(gatepassRepository.save(gatepass));
    }

    // Validates required fields and type-specific fields before saving a request.
    private void validateGatepassRequest(CreateGatepassRequest request) {
        if (request.getGatepassType() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Gatepass type is required");
        }
        if (request.getRequestDate() == null || request.getFromTime() == null || request.getToTime() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request date, from time and to time are required");
        }
        if (!request.getFromTime().isBefore(request.getToTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "From time must be before to time");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reason is required");
        }
        if (request.getGatepassType() == GatepassType.PLANT_TO_PLANT &&
                (request.getFromPlantId() == null || request.getToPlantId() == null)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "From plant and to plant are required for plant-to-plant request");
        }
        if (request.getGatepassType() == GatepassType.OUT_DUTY &&
                (request.getOutLocation() == null || request.getOutLocation().isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Out location is required for out duty request");
        }
        if (request.getGatepassType() == GatepassType.HALF_DAY && request.getHalfDaySession() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Half day session is required");
        }
    }

    // Generates a readable unique request number using today's date and nano time.
    private String generateRequestNo() {
        return "GP-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + System.nanoTime();
    }

    // Loads an employee or raises a 404 API error when the id is invalid.
    @SuppressWarnings("null")
    private Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    // Loads a plant when an id is supplied, otherwise keeps the plant field empty.
    private Plant getPlantOrNull(Long plantId) {
        if (plantId == null) {
            return null;
        }
        return plantRepository.findById(plantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plant not found"));
    }

    // Loads a gatepass request or raises a 404 API error when the id is invalid.
    @SuppressWarnings("null")
    private GatepassRequest getGatepass(Long gatepassId) {
        return gatepassRepository.findById(gatepassId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Gatepass request not found"));
    }

    // Ensures the acting manager is the manager assigned to the gatepass.
    private void validateManagerOwnership(Long managerId, GatepassRequest gatepass) {
        if (!gatepass.getManager().getId().equals(managerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This gatepass is not assigned to this manager");
        }
    }

    // Ensures an action happens only when the gatepass is in the expected status.
    private void validateStatus(GatepassRequest gatepass, GatepassStatus expectedStatus) {
        if (gatepass.getStatus() != expectedStatus) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status. Expected " + expectedStatus);
        }
    }

    // Builds the dynamic database filter used by paged gatepass searches.
    private Specification<GatepassRequest> gatepassSpec(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            LocalTime fromTime,
            LocalTime toTime,
            Long employeeId,
            Long managerId,
            GatepassType gatepassType,
            GatepassStatus status
    ) {
        return (root, query, builder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("employee", JoinType.LEFT);
                root.fetch("manager", JoinType.LEFT);
                root.fetch("fromPlant", JoinType.LEFT);
                root.fetch("toPlant", JoinType.LEFT);
            }
            query.distinct(true);

            var predicates = builder.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates = builder.and(predicates, builder.or(
                        builder.like(builder.lower(root.get("requestNo")), like),
                        builder.like(builder.lower(root.get("reason")), like),
                        builder.like(builder.lower(root.get("outLocation")), like),
                        builder.like(builder.lower(root.get("employee").get("employeeName")), like),
                        builder.like(builder.lower(root.get("employee").get("employeeCode")), like)
                ));
            }
            if (fromDate != null) {
                predicates = builder.and(predicates, builder.greaterThanOrEqualTo(root.get("requestDate"), fromDate));
            }
            if (toDate != null) {
                predicates = builder.and(predicates, builder.lessThanOrEqualTo(root.get("requestDate"), toDate));
            }
            if (fromTime != null) {
                predicates = builder.and(predicates, builder.greaterThanOrEqualTo(root.get("fromTime"), fromTime));
            }
            if (toTime != null) {
                predicates = builder.and(predicates, builder.lessThanOrEqualTo(root.get("toTime"), toTime));
            }
            if (employeeId != null) {
                predicates = builder.and(predicates, builder.equal(root.get("employee").get("id"), employeeId));
            }
            if (managerId != null) {
                predicates = builder.and(predicates, builder.equal(root.get("manager").get("id"), managerId));
            }
            if (gatepassType != null) {
                predicates = builder.and(predicates, builder.equal(root.get("gatepassType"), gatepassType));
            }
            if (status != null) {
                predicates = builder.and(predicates, builder.equal(root.get("status"), status));
            }
            return predicates;
        };
    }
}
