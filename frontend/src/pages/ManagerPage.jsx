import { useEffect, useState } from "react";
import { gatepassApi } from "../api/gatepassApi.js";
import GatepassTable from "../components/GatepassTable.jsx";
import Message from "../components/Message.jsx";
import Pagination from "../components/Pagination.jsx";

// Shows pending approvals for the selected manager and lets them approve or reject requests.
export default function ManagerPage({ manager }) {
  const [items, setItems] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [filters, setFilters] = useState({ keyword: "", page: 0, size: 20 });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadPending();
  }, [manager?.id]);

  // Loads pending requests assigned only to the logged-in reporting manager.
  async function loadPending(nextPage = filters.page) {
    if (!manager?.id) {
      return;
    }
    try {
      setError("");
      const nextFilters = { ...filters, page: nextPage };
      const result = await gatepassApi.searchPendingApprovals(manager.id, nextFilters);
      setFilters(nextFilters);
      setItems(result.content);
      setPageData(result);
    } catch (err) {
      setError(err.message);
    }
  }

  // Applies the manager keyword search and returns the table to page one.
  function applyFilters(event) {
    event.preventDefault();
    loadPending(0);
  }

  // Approves or rejects one gatepass, then reloads the pending list so the row disappears.
  async function decide(item, decision) {
    const remarks = decision === "approve" ? "Approved" : "Rejected";
    try {
      setMessage("");
      setError("");
      if (decision === "approve") {
        await gatepassApi.approveGatepass(manager.id, item.id, remarks);
        setMessage(`${item.requestNo} approved`);
      } else {
        await gatepassApi.rejectGatepass(manager.id, item.id, remarks);
        setMessage(`${item.requestNo} rejected`);
      }
      await loadPending();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section className="screen">
      <div className="screen-header">
        <div>
          <p className="eyebrow">Manager Portal</p>
          <h2>Pending Approvals</h2>
        </div>
        <button className="secondary" onClick={loadPending}>Refresh</button>
      </div>

      <Message type="success">{message}</Message>
      <Message type="error">{error}</Message>

      <form className="list-toolbar" onSubmit={applyFilters}>
        <label className="field">
          <span>Search Pending</span>
          <input
            value={filters.keyword}
            onChange={(event) => setFilters({ ...filters, keyword: event.target.value })}
            placeholder="Request no, employee, reason"
          />
        </label>
        <button className="primary">Search</button>
      </form>

      <GatepassTable
        items={items}
        actions={(item) => (
          <div className="row-actions">
            <button className="approve" onClick={() => decide(item, "approve")}>Approve</button>
            <button className="reject" onClick={() => decide(item, "reject")}>Reject</button>
          </div>
        )}
      />
      <Pagination page={pageData} onPageChange={loadPending} />
    </section>
  );
}
