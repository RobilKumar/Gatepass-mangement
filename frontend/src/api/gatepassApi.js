const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

// Handles JSON API calls and converts backend error responses into readable UI messages.
async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {})
      }
    });
  } catch (error) {
    throw new Error("Backend is not reachable. Start Spring Boot on http://localhost:8080.");
  }

  const text = await response.text();
  let data = null;

  if (text) {
    try {
      data = JSON.parse(text);
    } catch (error) {
      data = { message: text };
    }
  }

  if (!response.ok) {
    if (response.status === 500 && typeof data?.message === "string" && data.message.includes("proxy")) {
      throw new Error("Frontend cannot connect to backend. Start Spring Boot on http://localhost:8080.");
    }
    throw new Error(data?.message || `Request failed with status ${response.status}`);
  }

  return data;
}

// Handles file downloads, mainly the Excel movement report.
async function download(path, filename) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`);
  } catch (error) {
    throw new Error("Backend is not reachable. Start Spring Boot on http://localhost:8080.");
  }

  if (!response.ok) {
    const text = await response.text();
    let data = null;
    try {
      data = text ? JSON.parse(text) : null;
    } catch (error) {
      data = { message: text };
    }
    throw new Error(data?.message || `Download failed with status ${response.status}`);
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export const gatepassApi = {
  // Sends login credentials and returns the authenticated employee profile.
  login(payload) {
    return request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },

  // Fetches the complete employee list from the backend.
  getEmployees() {
    return request("/api/employees");
  },

  // Searches employees with optional filters such as keyword, role, status, and page.
  searchEmployees(filters = {}) {
    const params = toParams(filters);
    return request(`/api/employees/search${params}`);
  },

  // Creates a new employee master record.
  createEmployee(payload) {
    return request("/api/employees", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },

  // Updates an existing employee by id.
  updateEmployee(employeeId, payload) {
    return request(`/api/employees/${employeeId}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
  },

  // Marks an employee inactive without deleting their history.
  deactivateEmployee(employeeId) {
    return request(`/api/employees/${employeeId}`, {
      method: "DELETE"
    });
  },

  // Restores an inactive employee account.
  activateEmployee(employeeId) {
    return request(`/api/employees/${employeeId}/activate`, {
      method: "PUT"
    });
  },

  // Fetches all department master records.
  getDepartments() {
    return request("/api/masters/departments");
  },

  // Creates a department master record.
  createDepartment(payload) {
    return request("/api/masters/departments", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },

  // Fetches all plant master records.
  getPlants() {
    return request("/api/masters/plants");
  },

  // Creates a plant master record.
  createPlant(payload) {
    return request("/api/masters/plants", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },

  // Submits a gatepass request for the selected employee.
  createGatepass(employeeId, payload) {
    return request("/api/gatepasses", {
      method: "POST",
      headers: { "X-Employee-Id": String(employeeId) },
      body: JSON.stringify(payload)
    });
  },

  // Fetches gatepasses belonging to one employee.
  getMyGatepasses(employeeId) {
    return request("/api/gatepasses/my", {
      headers: { "X-Employee-Id": String(employeeId) }
    });
  },

  // Fetches pending approvals assigned to one manager.
  getPendingApprovals(managerId) {
    return request("/api/manager/gatepasses/pending", {
      headers: { "X-Manager-Id": String(managerId) }
    });
  },

  // Searches pending approvals for one manager.
  searchPendingApprovals(managerId, filters = {}) {
    const params = toParams(filters);
    return request(`/api/manager/gatepasses/pending/search${params}`, {
      headers: { "X-Manager-Id": String(managerId) }
    });
  },

  // Approves one gatepass request as the selected manager.
  approveGatepass(managerId, gatepassId, remarks) {
    return request(`/api/manager/gatepasses/${gatepassId}/approve`, {
      method: "PUT",
      headers: { "X-Manager-Id": String(managerId) },
      body: JSON.stringify({ remarks })
    });
  },

  // Rejects one gatepass request as the selected manager.
  rejectGatepass(managerId, gatepassId, remarks) {
    return request(`/api/manager/gatepasses/${gatepassId}/reject`, {
      method: "PUT",
      headers: { "X-Manager-Id": String(managerId) },
      body: JSON.stringify({ remarks })
    });
  },

  // Fetches all gatepass records for HR.
  getHrGatepasses() {
    return request("/api/hr/gatepasses");
  },

  // Searches gatepasses across HR/Admin reporting screens.
  searchGatepasses(filters = {}) {
    const params = toParams(filters);
    return request(`/api/hr/gatepasses/search${params}`);
  },

  // Fetches manager-approved gatepasses waiting for security checkout.
  getApprovedForSecurity() {
    return request("/api/security/gatepasses/approved");
  },

  // Searches approved gatepasses waiting for security checkout.
  searchApprovedForSecurity(filters = {}) {
    const params = toParams(filters);
    return request(`/api/security/gatepasses/approved/search${params}`);
  },

  // Fetches checked-out gatepasses waiting for security checkin.
  getCheckedOutForSecurity() {
    return request("/api/security/gatepasses/checked-out");
  },

  // Searches checked-out gatepasses waiting for security checkin.
  searchCheckedOutForSecurity(filters = {}) {
    const params = toParams(filters);
    return request(`/api/security/gatepasses/checked-out/search${params}`);
  },

  // Marks a gatepass as checked out by security.
  checkout(gatepassId) {
    return request(`/api/security/gatepasses/${gatepassId}/checkout`, {
      method: "PUT"
    });
  },

  // Marks a checked-out gatepass as checked in by security.
  checkin(gatepassId) {
    return request(`/api/security/gatepasses/${gatepassId}/checkin`, {
      method: "PUT"
    });
  },

  // Builds report query parameters and downloads the Excel movement report.
  downloadMovementReport(filters) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        params.append(key, value);
      }
    });

    const query = params.toString();
    const path = `/api/reports/gatepass-movement.xlsx${query ? `?${query}` : ""}`;
    return download(path, "gatepass-movement-report.xlsx");
  }
};

// Converts a filter object into a query string while skipping empty values.
function toParams(filters) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.append(key, value);
    }
  });
  const query = params.toString();
  return query ? `?${query}` : "";
}
