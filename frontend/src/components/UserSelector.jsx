// Renders a dropdown that lets Admin choose which employee role view to act as.
export default function UserSelector({ employees, selectedEmployeeId, onEmployeeChange }) {
  return (
    <label className="field compact-field">
      <span>Active User</span>
      <select
        value={selectedEmployeeId || ""}
        onChange={(event) => onEmployeeChange(Number(event.target.value))}
      >
        <option value="">Select user</option>
        {employees.map((employee) => (
          <option key={employee.id} value={employee.id}>
            {employee.employeeName} ({employee.role})
          </option>
        ))}
      </select>
    </label>
  );
}
