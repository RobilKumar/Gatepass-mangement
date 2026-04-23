import { useEffect, useMemo, useState } from "react";
import { gatepassApi } from "./api/gatepassApi.js";
import Message from "./components/Message.jsx";
import EmployeePage from "./pages/EmployeePage.jsx";
import ManagerPage from "./pages/ManagerPage.jsx";
import HrPage from "./pages/HrPage.jsx";
import SecurityPage from "./pages/SecurityPage.jsx";
import AdminPage from "./pages/AdminPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";

const tabsByRole = {
  ADMIN: [
    { key: "ADMIN", label: "Admin" },
    { key: "EMPLOYEE", label: "Employee" },
    { key: "MANAGER", label: "Manager" },
    { key: "HR", label: "HR" },
    { key: "SECURITY", label: "Security" }
  ],
  EMPLOYEE: [{ key: "EMPLOYEE", label: "Employee" }],
  MANAGER: [{ key: "MANAGER", label: "Manager" }],
  HR: [{ key: "HR", label: "HR" }],
  SECURITY: [{ key: "SECURITY", label: "Security" }]
};

// Controls login state, role-based tabs, and which portal page is displayed.
export default function App() {
  const [loggedInUser, setLoggedInUser] = useState(() => {
    const saved = localStorage.getItem("gatepassUser");
    return saved ? JSON.parse(saved) : null;
  });
  const [activeTab, setActiveTab] = useState(loggedInUser?.role || "EMPLOYEE");
  const [employees, setEmployees] = useState([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (loggedInUser) {
      setActiveTab(loggedInUser.role);
      loadEmployees();
    }
  }, [loggedInUser]);

  useEffect(() => {
    if (!loggedInUser) {
      return;
    }

    if (loggedInUser.role !== "ADMIN") {
      setSelectedEmployeeId(loggedInUser.id);
      return;
    }

    const preferred = employees.find((employee) => employee.role === activeTab);
    if (preferred) {
      setSelectedEmployeeId(preferred.id);
    }
  }, [activeTab, employees, loggedInUser]);

  // Chooses the employee record that should be used by the active role page.
  const selectedEmployee = useMemo(
    () => {
      if (loggedInUser?.role !== "ADMIN") {
        return loggedInUser;
      }
      return employees.find((employee) => employee.id === Number(selectedEmployeeId));
    },
    [employees, selectedEmployeeId, loggedInUser]
  );

  const tabs = tabsByRole[loggedInUser?.role] || [];

  // Loads employees for admin switching and picks a sensible default employee.
  async function loadEmployees() {
    try {
      setError("");
      const result = await gatepassApi.searchEmployees({ page: 0, size: 20 });
      const data = result.content;
      setEmployees(data);
      const firstEmployee = data.find((employee) => employee.role === "EMPLOYEE") || data[0];
      if (firstEmployee) {
        setSelectedEmployeeId(firstEmployee.id);
      }
    } catch (err) {
      setError(err.message);
    }
  }

  // Clears the saved session and returns the app to the login screen.
  function logout() {
    localStorage.removeItem("gatepassUser");
    setLoggedInUser(null);
    setEmployees([]);
    setSelectedEmployeeId("");
    setError("");
  }

  // Stores the logged-in user in state and opens the page for their role.
  function handleLogin(user) {
    setLoggedInUser(user);
    setActiveTab(user.role);
  }

  if (!loggedInUser) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return (
    <div className="app">
      <header className="topbar">
        <div>
          <p className="eyebrow">Gatepass Management</p>
          <h1>Employee Movement Portal</h1>
        </div>
        <div className="session-box">
          <strong>{loggedInUser.employeeName}</strong>
          <span>{loggedInUser.employeeCode} | {loggedInUser.role}</span>
          <button className="secondary small" onClick={logout}>Logout</button>
        </div>
      </header>

      <nav className="tabs" aria-label="Portal sections">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            className={activeTab === tab.key ? "tab active" : "tab"}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <Message type="error">{error}</Message>

      <main>
        {activeTab === "ADMIN" && <AdminPage onEmployeesChanged={setEmployees} />}
        {activeTab === "EMPLOYEE" && <EmployeePage employee={selectedEmployee} />}
        {activeTab === "MANAGER" && <ManagerPage manager={selectedEmployee} />}
        {activeTab === "HR" && <HrPage />}
        {activeTab === "SECURITY" && <SecurityPage />}
      </main>
    </div>
  );
}
