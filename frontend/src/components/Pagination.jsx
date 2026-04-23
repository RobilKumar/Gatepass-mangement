// Renders paging controls and calls the parent with the requested page index.
export default function Pagination({ page, onPageChange }) {
  if (!page) {
    return null;
  }

  const currentPage = page.page + 1;
  const totalPages = Math.max(page.totalPages, 1);

  return (
    <div className="pagination">
      <div>
        Showing page <strong>{currentPage}</strong> of <strong>{totalPages}</strong>
        {" "}({page.totalElements} records)
      </div>
      <div className="row-actions">
        <button className="secondary small" disabled={page.first} onClick={() => onPageChange(0)}>
          First
        </button>
        <button className="secondary small" disabled={page.first} onClick={() => onPageChange(page.page - 1)}>
          Previous
        </button>
        <button className="secondary small" disabled={page.last} onClick={() => onPageChange(page.page + 1)}>
          Next
        </button>
        <button className="secondary small" disabled={page.last} onClick={() => onPageChange(totalPages - 1)}>
          Last
        </button>
      </div>
    </div>
  );
}
