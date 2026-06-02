import type { MetadataRecord } from "../types";

type DataTableProps = {
  rows: MetadataRecord[];
  onView?: (recordId: number) => void;
  onEdit?: (recordId: number) => void;
  onDelete?: (recordId: number) => void;
};

export default function DataTable({ rows, onView, onEdit, onDelete }: DataTableProps) {
  const showActions = Boolean(onView || onEdit || onDelete);
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Mã hồ sơ</th>
            <th className="table-col-patient">Bệnh nhân</th>
            <th className="table-col-code">Mã</th>
            <th>Từ khóa</th>
            <th>Tóm tắt</th>
            <th>Cập nhật</th>
            {showActions ? <th>Thao tác</th> : null}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={showActions ? 7 : 6} className="empty">
                Không tìm thấy hồ sơ.
              </td>
            </tr>
          ) : (
            rows.map((row, index) => {
              const displayId = row.recordId ?? row.id ?? index + 1;
              const rowKey = row.recordId ?? row.id ?? `${row.patientCode}-${index}`;
              return (
                <tr key={rowKey}>
                  <td>{displayId}</td>
                  <td className="table-col-patient">{row.patientName}</td>
                  <td className="table-col-code">{row.patientCode}</td>
                  <td>{row.keywords || "-"}</td>
                  <td>{row.summary || "-"}</td>
                  <td>{new Date(row.updatedAt).toLocaleString()}</td>
                  {showActions ? (
                    <td className="table-actions-cell">
                      <div className="table-actions">
                        {onView ? (
                          <button
                            className="icon-btn"
                            type="button"
                            onClick={() => onView(displayId)}
                          >
                            Xem
                          </button>
                        ) : null}
                        {onEdit ? (
                          <button
                            className="icon-btn"
                            type="button"
                            onClick={() => onEdit(displayId)}
                          >
                            Sửa
                          </button>
                        ) : null}
                        {onDelete ? (
                          <button
                            className="icon-btn"
                            type="button"
                            onClick={() => onDelete(displayId)}
                          >
                            Xóa
                          </button>
                        ) : null}
                      </div>
                    </td>
                  ) : null}
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}
