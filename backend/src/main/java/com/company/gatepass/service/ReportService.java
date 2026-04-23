package com.company.gatepass.service;

import com.company.gatepass.entity.Employee;
import com.company.gatepass.entity.GatepassRequest;
import com.company.gatepass.enums.GatepassStatus;
import com.company.gatepass.enums.GatepassType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class ReportService {
    @PersistenceContext
    private EntityManager entityManager;

    // Generates the Excel movement report and returns it as downloadable bytes.
    @Transactional(readOnly = true)
    public byte[] generateMovementReport(
            LocalDate fromDate,
            LocalDate toDate,
            LocalTime fromTime,
            LocalTime toTime,
            Long employeeId,
            Long managerId,
            GatepassType gatepassType,
            GatepassStatus status
    ) {
        List<GatepassRequest> records = findReportRecords(
                fromDate,
                toDate,
                fromTime,
                toTime,
                employeeId,
                managerId,
                gatepassType,
                status
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Movement Report");
            sheet.createFreezePane(0, 3);

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Gatepass Movement Report");
            titleCell.setCellStyle(titleStyle);

            Row filterRow = sheet.createRow(1);
            filterRow.createCell(0).setCellValue("Filters");
            filterRow.createCell(1).setCellValue(buildFilterText(fromDate, toDate, fromTime, toTime, employeeId, managerId, gatepassType, status));

            String[] headers = {
                    "Request No",
                    "Employee Code",
                    "Employee Name",
                    "Department",
                    "Employee Plant",
                    "Manager",
                    "Gatepass Type",
                    "Request Date",
                    "From Time",
                    "To Time",
                    "From Plant",
                    "To Plant",
                    "Out Location",
                    "Reason",
                    "Status",
                    "Manager Remarks",
                    "Approved/Rejected At",
                    "Checkout Time",
                    "Checkin Time",
                    "Created At"
            };

            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 3;
            for (GatepassRequest record : records) {
                Row row = sheet.createRow(rowIndex++);
                Employee employee = record.getEmployee();

                write(row, 0, record.getRequestNo(), textStyle);
                write(row, 1, employee.getEmployeeCode(), textStyle);
                write(row, 2, employee.getEmployeeName(), textStyle);
                write(row, 3, employee.getDepartment() == null ? "" : employee.getDepartment().getDepartmentName(), textStyle);
                write(row, 4, employee.getPlant() == null ? "" : employee.getPlant().getPlantName(), textStyle);
                write(row, 5, record.getManager().getEmployeeName(), textStyle);
                write(row, 6, formatEnum(record.getGatepassType()), textStyle);
                write(row, 7, formatDate(record.getRequestDate()), textStyle);
                write(row, 8, formatTime(record.getFromTime()), textStyle);
                write(row, 9, formatTime(record.getToTime()), textStyle);
                write(row, 10, record.getFromPlant() == null ? "" : record.getFromPlant().getPlantName(), textStyle);
                write(row, 11, record.getToPlant() == null ? "" : record.getToPlant().getPlantName(), textStyle);
                write(row, 12, record.getOutLocation(), textStyle);
                write(row, 13, record.getReason(), textStyle);
                write(row, 14, formatEnum(record.getStatus()), textStyle);
                write(row, 15, record.getManagerRemarks(), textStyle);
                write(row, 16, formatDateTime(record.getManagerActionDate()), textStyle);
                write(row, 17, formatDateTime(record.getCheckoutTime()), textStyle);
                write(row, 18, formatDateTime(record.getCheckinTime()), textStyle);
                write(row, 19, formatDateTime(record.getCreatedAt()), textStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) > 9000) {
                    sheet.setColumnWidth(i, 9000);
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate Excel report", exception);
        }
    }

    // Builds and runs the report query using only the filters the user selected.
    private List<GatepassRequest> findReportRecords(
            LocalDate fromDate,
            LocalDate toDate,
            LocalTime fromTime,
            LocalTime toTime,
            Long employeeId,
            Long managerId,
            GatepassType gatepassType,
            GatepassStatus status
    ) {
        List<String> conditions = new ArrayList<>();
        StringBuilder jpql = new StringBuilder("""
                select g
                from GatepassRequest g
                join fetch g.employee e
                join fetch g.manager m
                left join fetch e.department
                left join fetch e.plant
                left join fetch g.fromPlant
                left join fetch g.toPlant
                """);

        if (fromDate != null) {
            conditions.add("g.requestDate >= :fromDate");
        }
        if (toDate != null) {
            conditions.add("g.requestDate <= :toDate");
        }
        if (fromTime != null) {
            conditions.add("g.fromTime >= :fromTime");
        }
        if (toTime != null) {
            conditions.add("g.toTime <= :toTime");
        }
        if (employeeId != null) {
            conditions.add("g.employee.id = :employeeId");
        }
        if (managerId != null) {
            conditions.add("g.manager.id = :managerId");
        }
        if (gatepassType != null) {
            conditions.add("g.gatepassType = :gatepassType");
        }
        if (status != null) {
            conditions.add("g.status = :status");
        }

        if (!conditions.isEmpty()) {
            StringJoiner where = new StringJoiner(" and ");
            conditions.forEach(where::add);
            jpql.append(" where ").append(where);
        }

        jpql.append(" order by g.requestDate desc, g.createdAt desc");

        TypedQuery<GatepassRequest> query = entityManager.createQuery(jpql.toString(), GatepassRequest.class);
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (fromTime != null) {
            query.setParameter("fromTime", fromTime);
        }
        if (toTime != null) {
            query.setParameter("toTime", toTime);
        }
        if (employeeId != null) {
            query.setParameter("employeeId", employeeId);
        }
        if (managerId != null) {
            query.setParameter("managerId", managerId);
        }
        if (gatepassType != null) {
            query.setParameter("gatepassType", gatepassType);
        }
        if (status != null) {
            query.setParameter("status", status);
        }

        return query.getResultList();
    }

    // Creates the workbook style used by the report title row.
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    // Creates the workbook style used by the column header row.
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    // Creates the workbook style used by normal report cells.
    private CellStyle createTextStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }

    // Writes one string value into a cell and applies the provided style.
    private void write(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    // Creates the human-readable filter summary shown near the top of the report.
    private String buildFilterText(
            LocalDate fromDate,
            LocalDate toDate,
            LocalTime fromTime,
            LocalTime toTime,
            Long employeeId,
            Long managerId,
            GatepassType gatepassType,
            GatepassStatus status
    ) {
        return "From Date: " + value(fromDate) +
                ", To Date: " + value(toDate) +
                ", From Time: " + value(fromTime) +
                ", To Time: " + value(toTime) +
                ", Employee ID: " + value(employeeId) +
                ", Manager ID: " + value(managerId) +
                ", Type: " + value(gatepassType) +
                ", Status: " + value(status);
    }

    // Converts null filters into "All" so the report summary is clear.
    private String value(Object value) {
        return value == null ? "All" : String.valueOf(value);
    }

    // Formats enum constants into readable words for report cells.
    private String formatEnum(Enum<?> value) {
        return value == null ? "" : value.name().replace("_", " ");
    }

    // Formats a date for the Excel report, leaving blanks for missing values.
    private String formatDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    // Formats a time for the Excel report, leaving blanks for missing values.
    private String formatTime(LocalTime value) {
        return value == null ? "" : value.toString();
    }

    // Formats a timestamp for the Excel report, leaving blanks for missing values.
    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.toString().replace("T", " ");
    }
}
