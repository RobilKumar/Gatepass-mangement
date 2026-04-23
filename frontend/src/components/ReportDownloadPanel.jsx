import { useEffect, useMemo, useState } from "react";
import { gatepassApi } from "../api/gatepassApi.js";
import Message from "./Message.jsx";

const initialFilters = {
  fromDate: "",
  toDate: "",
  fromTime: "",
  toTime: "",
  managerId: "",
  employeeId: "",
  gatepassType: "",
  status: ""
};

// Collects report filters and downloads the movement report as an Excel file.
export default function ReportDownloadPanel() {
  const [filters, setFilters] = useState(initialFilters);
  const [employees, setEmployees] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadEmployees();
  }, []);

  // Builds the manager dropdown from the loaded employee records.
  const managers = useMemo(
    () => employees.filter((employee) => employee.role === "MANAGER" && employee.active),
    [employees]
  );

  // Builds the employee dropdown from the loaded employee records.
  const employeeUsers = useMemo(
    () => employees.filter((employee) => employee.role === "EMPLOYEE" && employee.active),
    [employees]
  );

  // Loads employees so the report can be filtered by manager or employee.
  async function loadEmployees() {
    try {
      const [managerPage, employeePage] = await Promise.all([
        gatepassApi.searchEmployees({ role: "MANAGER", active: true, page: 0, size: 100 }),
        gatepassApi.searchEmployees({ role: "EMPLOYEE", active: true, page: 0, size: 100 })
      ]);
      setEmployees([...managerPage.content, ...employeePage.content]);
    } catch (err) {
      setError(err.message);
    }
  }

  // Updates one report filter without clearing the other selected filters.
  function updateFilter(field, value) {
    setFilters((current) => ({ ...current, [field]: value }));
  }

  // Sends selected filters to the backend and downloads the generated Excel file.
  async function downloadReport(event) {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    setError("");

    try {
      await gatepassApi.downloadMovementReport(filters);
      setMessage("Excel report downloaded");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="panel report-panel">
      <div className="screen-header">
        <div>
          <p className="eyebrow">Movement Report</p>
          <h3>Download Excel</h3>
        </div>
        <button type="button" className="secondary small" onClick={() => setFilters(initialFilters)}>Clear Filters</button>
      </div>

      <Message type="success">{message}</Message>
      <Message type="error">{error}</Message>

      <form className="report-grid" onSubmit={downloadReport}>
        <label className="field">
          <span>From Date</span>
          <input type="date" value={filters.fromDate} onChange={(event) => updateFilter("fromDate", event.target.value)} />
        </label>

        <label className="field">
          <span>To Date</span>
          <input type="date" value={filters.toDate} onChange={(event) => updateFilter("toDate", event.target.value)} />
        </label>

        <label className="field">
          <span>From Time</span>
          <input type="time" step="1" value={filters.fromTime} onChange={(event) => updateFilter("fromTime", event.target.value)} />
        </label>

        <label className="field">
          <span>To Time</span>
          <input type="time" step="1" value={filters.toTime} onChange={(event) => updateFilter("toTime", event.target.value)} />
        </label>

        <label className="field">
          <span>Manager</span>
          <select value={filters.managerId} onChange={(event) => updateFilter("managerId", event.target.value)}>
            <option value="">All Managers</option>
            {managers.map((manager) => (
              <option key={manager.id} value={manager.id}>{manager.employeeName}</option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>Employee</span>
          <select value={filters.employeeId} onChange={(event) => updateFilter("employeeId", event.target.value)}>
            <option value="">All Employees</option>
            {employeeUsers.map((employee) => (
              <option key={employee.id} value={employee.id}>{employee.employeeName}</option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>Gatepass Type</span>
          <select value={filters.gatepassType} onChange={(event) => updateFilter("gatepassType", event.target.value)}>
            <option value="">All Types</option>
            <option value="OUT_DUTY">Out Duty</option>
            <option value="PLANT_TO_PLANT">Plant to Plant</option>
            <option value="HALF_DAY">Half Day</option>
            <option value="PERSONAL">Personal</option>
          </select>
        </label>

        <label className="field">
          <span>Status</span>
          <select value={filters.status} onChange={(event) => updateFilter("status", event.target.value)}>
            <option value="">All Status</option>
            <option value="PENDING_MANAGER_APPROVAL">Pending</option>
            <option value="APPROVED_BY_MANAGER">Approved</option>
            <option value="REJECTED_BY_MANAGER">Rejected</option>
            <option value="CHECKED_OUT">Checked Out</option>
            <option value="CHECKED_IN">Checked In</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </label>

        <button className="primary" disabled={loading}>
          {loading ? "Downloading..." : "Download Excel"}
        </button>
      </form>
    </section>
  );
}
