import { useEffect, useState } from "react";
import { gatepassApi } from "../api/gatepassApi.js";
import GatepassTable from "../components/GatepassTable.jsx";
import Message from "../components/Message.jsx";
import Pagination from "../components/Pagination.jsx";

const initialForm = {
  gatepassType: "OUT_DUTY",
  requestDate: new Date().toISOString().slice(0, 10),
  fromTime: "11:00:00",
  toTime: "16:00:00",
  halfDaySession: "FIRST_HALF",
  fromPlantId: "1",
  toPlantId: "2",
  outLocation: "",
  reason: ""
};

// Lets an employee create gatepass requests and review their own request history.
export default function EmployeePage({ employee }) {
  const [form, setForm] = useState(initialForm);
  const [items, setItems] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [historyFilters, setHistoryFilters] = useState({ keyword: "", page: 0, size: 20 });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadMyGatepasses();
  }, [employee?.id]);

  // Fetches only the logged-in employee's gatepass history from the backend.
  async function loadMyGatepasses(nextPage = historyFilters.page) {
    if (!employee?.id) {
      return;
    }
    try {
      setError("");
      const nextFilters = { ...historyFilters, employeeId: employee.id, page: nextPage };
      const result = await gatepassApi.searchGatepasses(nextFilters);
      setHistoryFilters(nextFilters);
      setItems(result.content);
      setPageData(result);
    } catch (err) {
      setError(err.message);
    }
  }

  // Applies history search and returns the employee history table to the first page.
  function applyHistoryFilters(event) {
    event.preventDefault();
    loadMyGatepasses(0);
  }

  // Updates a single gatepass form field while preserving the rest of the form.
  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  // Builds the correct request payload based on gatepass type and sends it for manager approval.
  async function submit(event) {
    event.preventDefault();
    if (!employee?.id) {
      setError("Select an active employee before submitting a request.");
      return;
    }

    setLoading(true);
    setMessage("");
    setError("");

    const payload = {
      gatepassType: form.gatepassType,
      requestDate: form.requestDate,
      fromTime: form.fromTime,
      toTime: form.toTime,
      reason: form.reason
    };

    if (form.gatepassType === "OUT_DUTY" || form.gatepassType === "PERSONAL") {
      payload.outLocation = form.outLocation;
    }

    if (form.gatepassType === "PLANT_TO_PLANT") {
      payload.fromPlantId = Number(form.fromPlantId);
      payload.toPlantId = Number(form.toPlantId);
    }

    if (form.gatepassType === "HALF_DAY") {
      payload.halfDaySession = form.halfDaySession;
    }

    try {
      const created = await gatepassApi.createGatepass(employee.id, payload);
      setMessage(`Gatepass created: ${created.requestNo}`);
      setForm({ ...initialForm, gatepassType: form.gatepassType });
      await loadMyGatepasses();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="screen">
      <div className="screen-header">
        <div>
          <p className="eyebrow">Employee Portal</p>
          <h2>Create Gatepass</h2>
        </div>
        <div className="user-chip">{employee?.employeeName}</div>
      </div>

      <Message type="success">{message}</Message>
      <Message type="error">{error}</Message>

      <form className="form-grid" onSubmit={submit}>
        <label className="field">
          <span>Gatepass Type</span>
          <select value={form.gatepassType} onChange={(event) => updateField("gatepassType", event.target.value)}>
            <option value="OUT_DUTY">Out Duty</option>
            <option value="PLANT_TO_PLANT">Plant to Plant</option>
            <option value="HALF_DAY">Half Day</option>
            <option value="PERSONAL">Personal</option>
          </select>
        </label>

        <label className="field">
          <span>Date</span>
          <input type="date" value={form.requestDate} onChange={(event) => updateField("requestDate", event.target.value)} />
        </label>

        <label className="field">
          <span>From Time</span>
          <input type="time" step="1" value={form.fromTime} onChange={(event) => updateField("fromTime", event.target.value)} />
        </label>

        <label className="field">
          <span>To Time</span>
          <input type="time" step="1" value={form.toTime} onChange={(event) => updateField("toTime", event.target.value)} />
        </label>

        {form.gatepassType === "PLANT_TO_PLANT" && (
          <>
            <label className="field">
              <span>From Plant</span>
              <select value={form.fromPlantId} onChange={(event) => updateField("fromPlantId", event.target.value)}>
                <option value="1">Plant 1</option>
                <option value="2">Plant 2</option>
                <option value="3">Plant 3</option>
              </select>
            </label>
            <label className="field">
              <span>To Plant</span>
              <select value={form.toPlantId} onChange={(event) => updateField("toPlantId", event.target.value)}>
                <option value="1">Plant 1</option>
                <option value="2">Plant 2</option>
                <option value="3">Plant 3</option>
              </select>
            </label>
          </>
        )}

        {(form.gatepassType === "OUT_DUTY" || form.gatepassType === "PERSONAL") && (
          <label className="field">
            <span>Out Location</span>
            <input value={form.outLocation} onChange={(event) => updateField("outLocation", event.target.value)} placeholder="Client office" />
          </label>
        )}

        {form.gatepassType === "HALF_DAY" && (
          <label className="field">
            <span>Half Day Session</span>
            <select value={form.halfDaySession} onChange={(event) => updateField("halfDaySession", event.target.value)}>
              <option value="FIRST_HALF">First Half</option>
              <option value="SECOND_HALF">Second Half</option>
            </select>
          </label>
        )}

        <label className="field wide">
          <span>Reason</span>
          <textarea value={form.reason} onChange={(event) => updateField("reason", event.target.value)} placeholder="Reason for movement" />
        </label>

        <button className="primary" disabled={loading || !employee?.id}>
          {!employee?.id ? "Select Employee First" : loading ? "Submitting..." : "Submit Request"}
        </button>
      </form>

      <div className="section-title">
        <h3>My Requests</h3>
        <button className="secondary" onClick={loadMyGatepasses}>Refresh</button>
      </div>
      <form className="list-toolbar" onSubmit={applyHistoryFilters}>
        <label className="field">
          <span>Search My Requests</span>
          <input
            value={historyFilters.keyword}
            onChange={(event) => setHistoryFilters({ ...historyFilters, keyword: event.target.value })}
            placeholder="Request no, reason, location"
          />
        </label>
        <button className="primary">Search</button>
      </form>
      <GatepassTable items={items} />
      <Pagination page={pageData} onPageChange={loadMyGatepasses} />
    </section>
  );
}
