import { useEffect, useState } from "react";
import { gatepassApi } from "../api/gatepassApi.js";
import GatepassTable from "../components/GatepassTable.jsx";
import Message from "../components/Message.jsx";
import Pagination from "../components/Pagination.jsx";

// Gives security staff separate queues for checkout and checkin actions.
export default function SecurityPage() {
  const [approvedItems, setApprovedItems] = useState([]);
  const [checkedOutItems, setCheckedOutItems] = useState([]);
  const [approvedPage, setApprovedPage] = useState(null);
  const [checkedOutPage, setCheckedOutPage] = useState(null);
  const [approvedFilters, setApprovedFilters] = useState({ keyword: "", page: 0, size: 20 });
  const [checkedOutFilters, setCheckedOutFilters] = useState({ keyword: "", page: 0, size: 20 });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadLists();
  }, []);

  // Loads both security queues: approved requests for checkout and checked-out requests for checkin.
  async function loadLists() {
    try {
      setError("");
      const [approved, checkedOut] = await Promise.all([
        gatepassApi.searchApprovedForSecurity(approvedFilters),
        gatepassApi.searchCheckedOutForSecurity(checkedOutFilters)
      ]);
      setApprovedItems(approved.content);
      setCheckedOutItems(checkedOut.content);
      setApprovedPage(approved);
      setCheckedOutPage(checkedOut);
    } catch (err) {
      setError(err.message);
    }
  }

  // Reloads approved queue with a selected page number.
  async function loadApprovedPage(nextPage) {
    const nextFilters = { ...approvedFilters, page: nextPage };
    setApprovedFilters(nextFilters);
    const result = await gatepassApi.searchApprovedForSecurity(nextFilters);
    setApprovedItems(result.content);
    setApprovedPage(result);
  }

  // Reloads checked-out queue with a selected page number.
  async function loadCheckedOutPage(nextPage) {
    const nextFilters = { ...checkedOutFilters, page: nextPage };
    setCheckedOutFilters(nextFilters);
    const result = await gatepassApi.searchCheckedOutForSecurity(nextFilters);
    setCheckedOutItems(result.content);
    setCheckedOutPage(result);
  }

  // Applies keyword searches for both security queues.
  async function applyFilters(event) {
    event.preventDefault();
    const nextApprovedFilters = { ...approvedFilters, page: 0 };
    const nextCheckedOutFilters = { ...checkedOutFilters, page: 0 };
    setApprovedFilters(nextApprovedFilters);
    setCheckedOutFilters(nextCheckedOutFilters);

    try {
      setError("");
      const [approved, checkedOut] = await Promise.all([
        gatepassApi.searchApprovedForSecurity(nextApprovedFilters),
        gatepassApi.searchCheckedOutForSecurity(nextCheckedOutFilters)
      ]);
      setApprovedItems(approved.content);
      setCheckedOutItems(checkedOut.content);
      setApprovedPage(approved);
      setCheckedOutPage(checkedOut);
    } catch (err) {
      setError(err.message);
    }
  }

  // Marks an approved gatepass as checked out when the employee leaves.
  async function checkout(item) {
    try {
      setError("");
      await gatepassApi.checkout(item.id);
      setMessage(`${item.requestNo} checked out`);
      await loadLists();
    } catch (err) {
      setError(err.message);
    }
  }

  // Marks a checked-out gatepass as checked in when the employee returns.
  async function checkin(item) {
    try {
      setError("");
      await gatepassApi.checkin(item.id);
      setMessage(`${item.requestNo} checked in`);
      await loadLists();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section className="screen">
      <div className="screen-header">
        <div>
          <p className="eyebrow">Security Portal</p>
          <h2>Approved Gatepasses</h2>
        </div>
        <button className="secondary" onClick={loadLists}>Refresh</button>
      </div>

      <Message type="success">{message}</Message>
      <Message type="error">{error}</Message>

      <form className="list-toolbar" onSubmit={applyFilters}>
        <label className="field">
          <span>Search Approved</span>
          <input
            value={approvedFilters.keyword}
            onChange={(event) => setApprovedFilters({ ...approvedFilters, keyword: event.target.value })}
            placeholder="Request no, employee, reason"
          />
        </label>
        <label className="field">
          <span>Search Checked Out</span>
          <input
            value={checkedOutFilters.keyword}
            onChange={(event) => setCheckedOutFilters({ ...checkedOutFilters, keyword: event.target.value })}
            placeholder="Request no, employee, reason"
          />
        </label>
        <button className="primary">Search</button>
      </form>

      <GatepassTable
        items={approvedItems}
        actions={(item) => (
          <button className="primary small" onClick={() => checkout(item)}>Checkout</button>
        )}
      />
      <Pagination page={approvedPage} onPageChange={loadApprovedPage} />

      <div className="section-title">
        <h3>Checked Out</h3>
      </div>
      <GatepassTable
        items={checkedOutItems}
        actions={(item) => (
          <button className="approve small" onClick={() => checkin(item)}>Checkin</button>
        )}
      />
      <Pagination page={checkedOutPage} onPageChange={loadCheckedOutPage} />
    </section>
  );
}
