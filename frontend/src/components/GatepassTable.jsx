import StatusBadge from "./StatusBadge.jsx";

// Renders gatepass records in a reusable table with optional row actions.
export default function GatepassTable({ items, actions }) {
  if (!items?.length) {
    return <div className="empty">No gatepass records found.</div>;
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Request No</th>
            <th>Employee</th>
            <th>Type</th>
            <th>Date</th>
            <th>Time</th>
            <th>Reason</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id}>
              <td>{item.requestNo}</td>
              <td>{item.employeeName}</td>
              <td>{formatType(item.gatepassType)}</td>
              <td>{item.requestDate}</td>
              <td>{item.fromTime} - {item.toTime}</td>
              <td>{item.reason}</td>
              <td><StatusBadge status={item.status} /></td>
              <td>{actions ? actions(item) : "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// Converts backend enum names into labels that are easier to read in the table.
function formatType(type) {
  return String(type || "").replaceAll("_", " ");
}
