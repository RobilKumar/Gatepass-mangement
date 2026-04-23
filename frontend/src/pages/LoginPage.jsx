import { useState } from "react";
import { gatepassApi } from "../api/gatepassApi.js";
import Message from "../components/Message.jsx";

const demoUsers = [
  { label: "Admin", loginId: "ADM001", password: "admin123" },
  { label: "Employee", loginId: "EMP001", password: "emp123" },
  { label: "Manager", loginId: "MGR001", password: "manager123" },
  { label: "HR", loginId: "HR001", password: "hr123" },
  { label: "Security", loginId: "SEC001", password: "security123" }
];

// Handles user login and quick demo credential filling.
export default function LoginPage({ onLogin }) {
  const [form, setForm] = useState({ loginId: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // Validates credentials with the backend and stores the logged-in user in browser storage.
  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const user = await gatepassApi.login(form);
      localStorage.setItem("gatepassUser", JSON.stringify(user));
      onLogin(user);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // Fills sample credentials quickly while testing each role.
  function useDemo(user) {
    setForm({ loginId: user.loginId, password: user.password });
  }

  return (
    <div className="login-page">
      <form className="login-box" onSubmit={submit}>
        <p className="eyebrow">Gatepass Management</p>
        <h1>Login</h1>
        <p className="login-copy">Use your employee code or email with password.</p>

        <Message type="error">{error}</Message>

        <label className="field">
          <span>Login ID</span>
          <input
            value={form.loginId}
            onChange={(event) => setForm({ ...form, loginId: event.target.value })}
            placeholder="EMP001 or amit@company.com"
            required
          />
        </label>

        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            placeholder="Password"
            required
          />
        </label>

        <button className="primary" disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>

        <div className="demo-users">
          <span>Quick fill</span>
          <div className="demo-buttons">
            {demoUsers.map((user) => (
              <button key={user.loginId} type="button" className="secondary small" onClick={() => useDemo(user)}>
                {user.label}
              </button>
            ))}
          </div>
        </div>
      </form>
    </div>
  );
}
