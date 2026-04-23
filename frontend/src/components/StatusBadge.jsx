const labelMap = {
  PENDING_MANAGER_APPROVAL: "Pending",
  APPROVED_BY_MANAGER: "Approved",
  REJECTED_BY_MANAGER: "Rejected",
  CHECKED_OUT: "Checked Out",
  CHECKED_IN: "Checked In",
  CANCELLED: "Cancelled"
};

// Displays a readable status label with a CSS class based on the backend status.
export default function StatusBadge({ status }) {
  return (
    <span className={`status status-${status || "UNKNOWN"}`}>
      {labelMap[status] || status || "Unknown"}
    </span>
  );
}
