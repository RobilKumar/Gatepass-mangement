import { useEffect, useMemo, useState } from "react";
import { gatepassApi } from "../api/gatepassApi.js";
import GatepassTable from "../components/GatepassTable.jsx";
import Message from "../components/Message.jsx";
import ReportDownloadPanel from "../components/ReportDownloadPanel.jsx";
import Pagination from "../components/Pagination.jsx";

const initialEmployee = {
  employeeCode: "",
  employeeName: "",
  email: "",
  password: "123456",
  mobile: "",
  designation: "",
  departmentId: "",
  plantId: "",
  reportingManagerId: "",
  role: "EMPLOYEE"
};

const adminSections = [
  { key: "EMPLOYEES", label: "Employees" },
  { key: "MASTERS", label: "Masters" },
  { key: "REPORTS", label: "Reports" },
  { key: "GATEPASSES", label: "Gatepasses" }
];

// Displays admin tools for employees, master data, reports, and gatepass search.
export default function AdminPage({ onEmployeesChanged }) {
  const [activeSection, setActiveSection] = useState("EMPLOYEES");
  const [employees, setEmployees] = useState([]);
  const [employeePage, setEmployeePage] = useState(null);
  const [employeeFilters, setEmployeeFilters] = useState({ keyword: "", role: "", active: "", page: 0, size: 20 });
  const [managerOptions, setManagerOptions] = useState([]);
  const [managerKeyword, setManagerKeyword] = useState("");
  const [departments, setDepartments] = useState([]);
  const [plants, setPlants] = useState([]);
  const [gatepasses, setGatepasses] = useState([]);
  const [gatepassPage, setGatepassPage] = useState(null);
  const [gatepassFilters, setGatepassFilters] = useState({ keyword: "", page: 0, size: 20 });
  const [employeeForm, setEmployeeForm] = useState(initialEmployee);
  const [editingEmployeeId, setEditingEmployeeId] = useState(null);
  const [departmentName, setDepartmentName] = useState("");
  const [plantForm, setPlantForm] = useState({ code: "", name: "", location: "" });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadAdminData();
  }, []);

  // Keeps only active managers for the reporting manager dropdown.
  const managers = useMemo(
    () => managerOptions.filter((employee) => employee.role === "MANAGER" && employee.active),
    [managerOptions]
  );

  // Loads small master lists and first paged result sets without pulling 10k records into the browser.
  async function loadAdminData() {
    try {
      setError("");
      const [employeeData, managerData, departmentData, plantData, gatepassData] = await Promise.all([
        gatepassApi.searchEmployees(employeeFilters),
        gatepassApi.searchEmployees({ role: "MANAGER", active: true, page: 0, size: 100 }),
        gatepassApi.getDepartments(),
        gatepassApi.getPlants(),
        gatepassApi.searchGatepasses(gatepassFilters)
      ]);
      setEmployees(employeeData.content);
      setEmployeePage(employeeData);
      setManagerOptions(managerData.content);
      setDepartments(departmentData);
      setPlants(plantData);
      setGatepasses(gatepassData.content);
      setGatepassPage(gatepassData);
      onEmployeesChanged?.(employeeData.content);
    } catch (err) {
      setError(err.message);
    }
  }

  // Reloads only the employee table using the current search filters and requested page number.
  async function loadEmployeePage(nextPage = employeeFilters.page) {
    try {
      setError("");
      const nextFilters = { ...employeeFilters, page: nextPage };
      const result = await gatepassApi.searchEmployees(nextFilters);
      setEmployeeFilters(nextFilters);
      setEmployees(result.content);
      setEmployeePage(result);
      onEmployeesChanged?.(result.content);
    } catch (err) {
      setError(err.message);
    }
  }

  // Reloads only the gatepass table using the current keyword and requested page number.
  async function loadGatepassPage(nextPage = gatepassFilters.page) {
    try {
      setError("");
      const nextFilters = { ...gatepassFilters, page: nextPage };
      const result = await gatepassApi.searchGatepasses(nextFilters);
      setGatepassFilters(nextFilters);
      setGatepasses(result.content);
      setGatepassPage(result);
    } catch (err) {
      setError(err.message);
    }
  }

  // Applies employee table filters from the toolbar and returns to page one.
  function applyEmployeeFilters(event) {
    event.preventDefault();
    loadEmployeePage(0);
  }

  // Applies gatepass table filters from the toolbar and returns to page one.
  function applyGatepassFilters(event) {
    event.preventDefault();
    loadGatepassPage(0);
  }

  // Searches active managers by name/code so Admin can assign a reporting manager without loading all managers.
  async function searchManagers() {
    try {
      setError("");
      const result = await gatepassApi.searchEmployees({
        keyword: managerKeyword,
        role: "MANAGER",
        active: true,
        page: 0,
        size: 100
      });
      setManagerOptions(result.content);
    } catch (err) {
      setError(err.message);
    }
  }

  // Updates one field in the employee form without disturbing the other entered values.
  function setEmployeeField(field, value) {
    setEmployeeForm((current) => ({ ...current, [field]: value }));
  }

  // Converts form dropdown values into the numeric/null shape expected by the backend APIs.
  function buildEmployeePayload() {
    return {
      ...employeeForm,
      departmentId: employeeForm.departmentId ? Number(employeeForm.departmentId) : null,
      plantId: employeeForm.plantId ? Number(employeeForm.plantId) : null,
      reportingManagerId: employeeForm.reportingManagerId ? Number(employeeForm.reportingManagerId) : null
    };
  }

  // Creates a new employee or updates the selected employee when Admin is in edit mode.
  async function saveEmployee(event) {
    event.preventDefault();
    try {
      setMessage("");
      setError("");
      const payload = buildEmployeePayload();
      if (editingEmployeeId) {
        await gatepassApi.updateEmployee(editingEmployeeId, payload);
        setMessage("Employee updated");
      } else {
        await gatepassApi.createEmployee(payload);
        setMessage("Employee created");
      }
      clearEmployeeForm();
      await loadEmployeePage(0);
      const managers = await gatepassApi.searchEmployees({ role: "MANAGER", active: true, page: 0, size: 100 });
      setManagerOptions(managers.content);
    } catch (err) {
      setError(err.message);
    }
  }

  // Copies a row from Employee Master into the form so Admin can update manager, role, plant, password, etc.
  function editEmployee(employee) {
    setEditingEmployeeId(employee.id);
    setMessage("");
    setError("");
    setEmployeeForm({
      employeeCode: employee.employeeCode || "",
      employeeName: employee.employeeName || "",
      email: employee.email || "",
      password: "",
      mobile: employee.mobile || "",
      designation: employee.designation || "",
      departmentId: employee.departmentId || "",
      plantId: employee.plantId || "",
      reportingManagerId: employee.reportingManagerId || "",
      role: employee.role || "EMPLOYEE"
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  // Leaves edit mode and returns the employee form to a clean create state.
  function clearEmployeeForm() {
    setEditingEmployeeId(null);
    setEmployeeForm(initialEmployee);
  }

  // Soft-deletes an employee by making them inactive while keeping their history for reports.
  async function deactivateEmployee(employee) {
    try {
      setMessage("");
      setError("");
      await gatepassApi.deactivateEmployee(employee.id);
      setMessage(`${employee.employeeName} deactivated`);
      await loadEmployeePage(employeeFilters.page);
    } catch (err) {
      setError(err.message);
    }
  }

  // Restores an inactive employee so they can login and be used in workflows again.
  async function activateEmployee(employee) {
    try {
      setMessage("");
      setError("");
      await gatepassApi.activateEmployee(employee.id);
      setMessage(`${employee.employeeName} activated`);
      await loadEmployeePage(employeeFilters.page);
    } catch (err) {
      setError(err.message);
    }
  }

  // Creates a new department master record used while creating or editing employees.
  async function createDepartment(event) {
    event.preventDefault();
    try {
      setMessage("");
      setError("");
      await gatepassApi.createDepartment({ name: departmentName });
      setMessage("Department created");
      setDepartmentName("");
      await loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  }

  // Creates a new plant master record used for employee plant and plant-to-plant movement.
  async function createPlant(event) {
    event.preventDefault();
    try {
      setMessage("");
      setError("");
      await gatepassApi.createPlant(plantForm);
      setMessage("Plant created");
      setPlantForm({ code: "", name: "", location: "" });
      await loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section className="screen">
      <div className="screen-header">
        <div>
          <p className="eyebrow">Admin Portal</p>
          <h2>Masters And Control</h2>
        </div>
        <button className="secondary" onClick={loadAdminData}>Refresh</button>
      </div>

      <Message type="success">{message}</Message>
      <Message type="error">{error}</Message>

      <nav className="subnav" aria-label="Admin sections">
        {adminSections.map((section) => (
          <button
            key={section.key}
            className={activeSection === section.key ? "subnav-button active" : "subnav-button"}
            onClick={() => setActiveSection(section.key)}
          >
            {section.label}
          </button>
        ))}
      </nav>

      {activeSection === "EMPLOYEES" && (
        <>
          <form className="panel form-grid admin-employee-form" onSubmit={saveEmployee}>
            <div className="form-heading wide">
              <div>
                <p className="eyebrow">Employee Master</p>
                <h3>{editingEmployeeId ? "Edit Employee" : "Create Employee"}</h3>
              </div>
              {editingEmployeeId && (
                <button type="button" className="secondary small" onClick={clearEmployeeForm}>Cancel Edit</button>
              )}
            </div>

            <label className="field">
              <span>Employee Code</span>
              <input value={employeeForm.employeeCode} onChange={(event) => setEmployeeField("employeeCode", event.target.value)} required />
            </label>

            <label className="field">
              <span>Employee Name</span>
              <input value={employeeForm.employeeName} onChange={(event) => setEmployeeField("employeeName", event.target.value)} required />
            </label>

            <label className="field">
              <span>Email</span>
              <input type="email" value={employeeForm.email} onChange={(event) => setEmployeeField("email", event.target.value)} required />
            </label>

            <label className="field">
              <span>{editingEmployeeId ? "New Password" : "Password"}</span>
              <input
                value={employeeForm.password}
                onChange={(event) => setEmployeeField("password", event.target.value)}
                placeholder={editingEmployeeId ? "Leave blank to keep old password" : "Password"}
                required={!editingEmployeeId}
              />
            </label>

            <label className="field">
              <span>Mobile</span>
              <input value={employeeForm.mobile} onChange={(event) => setEmployeeField("mobile", event.target.value)} />
            </label>

            <label className="field">
              <span>Designation</span>
              <input value={employeeForm.designation} onChange={(event) => setEmployeeField("designation", event.target.value)} />
            </label>

            <label className="field">
              <span>Role</span>
              <select value={employeeForm.role} onChange={(event) => setEmployeeField("role", event.target.value)}>
                <option value="EMPLOYEE">Employee</option>
                <option value="MANAGER">Manager</option>
                <option value="HR">HR</option>
                <option value="ADMIN">Admin</option>
                <option value="SECURITY">Security</option>
              </select>
            </label>

            <label className="field">
              <span>Department</span>
              <select value={employeeForm.departmentId} onChange={(event) => setEmployeeField("departmentId", event.target.value)}>
                <option value="">None</option>
                {departments.map((department) => (
                  <option key={department.id} value={department.id}>{department.departmentName}</option>
                ))}
              </select>
            </label>

            <label className="field">
              <span>Plant</span>
              <select value={employeeForm.plantId} onChange={(event) => setEmployeeField("plantId", event.target.value)}>
                <option value="">None</option>
                {plants.map((plant) => (
                  <option key={plant.id} value={plant.id}>{plant.plantName}</option>
                ))}
              </select>
            </label>

            <label className="field wide">
              <span>Find Manager</span>
              <div className="inline-search">
                <input
                  value={managerKeyword}
                  onChange={(event) => setManagerKeyword(event.target.value)}
                  placeholder="Type manager name or code"
                />
                <button type="button" className="secondary" onClick={searchManagers}>Find</button>
              </div>
            </label>

            <label className="field wide">
              <span>Reporting Manager</span>
              <select value={employeeForm.reportingManagerId} onChange={(event) => setEmployeeField("reportingManagerId", event.target.value)}>
                <option value="">None</option>
                {managers.map((manager) => (
                  <option key={manager.id} value={manager.id}>{manager.employeeName}</option>
                ))}
              </select>
            </label>

            <button className="primary">{editingEmployeeId ? "Update Employee" : "Create Employee"}</button>
          </form>

          <div className="section-title">
            <h3>Employee List</h3>
          </div>
          <form className="list-toolbar" onSubmit={applyEmployeeFilters}>
            <label className="field">
              <span>Search Employee</span>
              <input
                value={employeeFilters.keyword}
                onChange={(event) => setEmployeeFilters({ ...employeeFilters, keyword: event.target.value })}
                placeholder="Code, name, email, mobile"
              />
            </label>
            <label className="field">
              <span>Role</span>
              <select value={employeeFilters.role} onChange={(event) => setEmployeeFilters({ ...employeeFilters, role: event.target.value })}>
                <option value="">All Roles</option>
                <option value="EMPLOYEE">Employee</option>
                <option value="MANAGER">Manager</option>
                <option value="HR">HR</option>
                <option value="ADMIN">Admin</option>
                <option value="SECURITY">Security</option>
              </select>
            </label>
            <label className="field">
              <span>Status</span>
              <select value={employeeFilters.active} onChange={(event) => setEmployeeFilters({ ...employeeFilters, active: event.target.value })}>
                <option value="">All</option>
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
            </label>
            <button className="primary">Search</button>
          </form>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Name</th>
                  <th>Role</th>
                  <th>Department</th>
                  <th>Plant</th>
                  <th>Manager</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {employees.map((employee) => (
                  <tr key={employee.id}>
                    <td>{employee.employeeCode}</td>
                    <td>{employee.employeeName}</td>
                    <td>{employee.role}</td>
                    <td>{employee.departmentName || "-"}</td>
                    <td>{employee.plantName || "-"}</td>
                    <td>{employee.reportingManagerName || "-"}</td>
                    <td>{employee.active ? "Active" : "Inactive"}</td>
                    <td>
                      <div className="row-actions">
                        <button className="secondary small" onClick={() => editEmployee(employee)}>Edit</button>
                        {employee.active ? (
                          <button className="reject small" onClick={() => deactivateEmployee(employee)}>Deactivate</button>
                        ) : (
                          <button className="approve small" onClick={() => activateEmployee(employee)}>Activate</button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={employeePage} onPageChange={loadEmployeePage} />
        </>
      )}

      {activeSection === "MASTERS" && (
        <div className="admin-master-grid">
          <form className="panel compact-form" onSubmit={createDepartment}>
            <p className="eyebrow">Department Master</p>
            <h3>Create Department</h3>
            <label className="field">
              <span>Department Name</span>
              <input value={departmentName} onChange={(event) => setDepartmentName(event.target.value)} required />
            </label>
            <button className="primary">Save Department</button>
          </form>

          <form className="panel compact-form" onSubmit={createPlant}>
            <p className="eyebrow">Plant Master</p>
            <h3>Create Plant</h3>
            <label className="field">
              <span>Plant Code</span>
              <input value={plantForm.code} onChange={(event) => setPlantForm({ ...plantForm, code: event.target.value })} required />
            </label>
            <label className="field">
              <span>Plant Name</span>
              <input value={plantForm.name} onChange={(event) => setPlantForm({ ...plantForm, name: event.target.value })} required />
            </label>
            <label className="field">
              <span>Location</span>
              <input value={plantForm.location} onChange={(event) => setPlantForm({ ...plantForm, location: event.target.value })} />
            </label>
            <button className="primary">Save Plant</button>
          </form>
        </div>
      )}

      {activeSection === "REPORTS" && (
        <ReportDownloadPanel />
      )}

      {activeSection === "GATEPASSES" && (
        <div>
          <div className="section-title">
            <h3>All Gatepasses</h3>
          </div>
          <form className="list-toolbar" onSubmit={applyGatepassFilters}>
            <label className="field">
              <span>Search Gatepass</span>
              <input
                value={gatepassFilters.keyword}
                onChange={(event) => setGatepassFilters({ ...gatepassFilters, keyword: event.target.value })}
                placeholder="Request no, employee, reason"
              />
            </label>
            <button className="primary">Search</button>
          </form>
          <GatepassTable items={gatepasses} />
          <Pagination page={gatepassPage} onPageChange={loadGatepassPage} />
        </div>
      )}
    </section>
  );
}
