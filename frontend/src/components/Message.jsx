// Shows a styled message only when there is text to display.
export default function Message({ type = "info", children }) {
  if (!children) {
    return null;
  }

  return <div className={`message message-${type}`}>{children}</div>;
}
