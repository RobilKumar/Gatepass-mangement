import { useEffect, useState } from "react";
import { gatepassApi } from "../api/gatepassApi.js";
import GatepassTable from "../components/GatepassTable.jsx";
import Message from "../components/Message.jsx";
import ReportDownloadPanel from "../components/ReportDownloadPanel.jsx";
import Pagination from "../components/Pagination.jsx";

// Shows HR a searchable list of all gatepass movements and report tools.
export default function HrPage() {
  const [items, setItems] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [filters, setFilters] = useState({ keyword: "", status: "", page: 0, size: 20 });
  const [error, setError] = useState("");

  useEffect(() => {
    loadGatepasses();
  }, []);

  // HR sees every gatepass record, so this loads the full movement list.
  async function loadGatepasses(nextPage = filters.page) {
    try {
      setError("");
      const nextFilters = { ...filters, page: nextPage };
      const result = await gatepassApi.searchGatepasses(nextFilters);
      setFilters(nextFilters);
      setItems(result.content);
      setPageData(result);
    } catch (err) {
      setError(err.message);
    }
  }

  // Applies HR table filters and returns to the first page.
  function applyFilters(event) {
    event.preventDefault();
    loadGatepasses(0);
  }

  return (
    <section className="screen">
      <div className="screen-header">
        <div>
          <p className="eyebrow">HR Portal</p>
          <h2>All Gatepasses</h2>
        </div>
        <button className="secondary" onClick={loadGatepasses}>Refresh</button>
      </div>

      <Message type="error">{error}</Message>
      <ReportDownloadPanel />
      <form className="list-toolbar" onSubmit={applyFilters}>
        <label className="field">
          <span>Search Movement</span>
          <input
            value={filters.keyword}
            onChange={(event) => setFilters({ ...filters, keyword: event.target.value })}
            placeholder="Request no, employee, reason"
          />
        </label>
        <label className="field">
          <span>Status</span>
          <select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value })}>
            <option value="">All Status</option>
            <option value="PENDING_MANAGER_APPROVAL">Pending</option>
            <option value="APPROVED_BY_MANAGER">Approved</option>
            <option value="REJECTED_BY_MANAGER">Rejected</option>
            <option value="CHECKED_OUT">Checked Out</option>
            <option value="CHECKED_IN">Checked In</option>
          </select>
        </label>
        <button className="primary">Search</button>
      </form>
      <GatepassTable items={items} />
      <Pagination page={pageData} onPageChange={loadGatepasses} />
    </section>
  );
}
