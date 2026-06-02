import { useEffect, useState } from "react";
import {
  deleteDoctorRecord,
  getDoctorPatientBasic,
  getDoctorRecord,
  getDoctorRecordDetail,
  searchRecords,
  updateDoctorRecord
} from "../api";
import DataTable from "../components/DataTable";
import type {
  DoctorPatientBasic,
  DoctorRecord,
  DoctorRecordDetail,
  MetadataRecord
} from "../types";

type StatusOption = "PROCESSING" | "COMPLETED";

const securityLevelLabels: Record<string, string> = {
  PUBLIC: "Công khai",
  CONFIDENTIAL: "Bảo mật",
  RESTRICTED: "Hạn chế"
};

const statusLabels: Record<StatusOption, string> = {
  PROCESSING: "Đang xử lý",
  COMPLETED: "Hoàn tất"
};

const statusDescriptions: Record<StatusOption, string> = {
  PROCESSING: "Đang điều trị / theo dõi",
  COMPLETED: "Kết thúc điều trị"
};

type DoctorRecordManagePageProps = {
  onStatusChange?: (status: string) => void;
};

export default function DoctorRecordManagePage({ onStatusChange }: DoctorRecordManagePageProps) {
  const [patientCode, setPatientCode] = useState("BN-3");
  const [recordIdQuery, setRecordIdQuery] = useState("");
  const [keywordQuery, setKeywordQuery] = useState("");
  const [records, setRecords] = useState<MetadataRecord[]>([]);
  const [totalRecords, setTotalRecords] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<DoctorPatientBasic | null>(null);
  const [recordDetail, setRecordDetail] = useState<DoctorRecordDetail | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [editRecord, setEditRecord] = useState<DoctorRecord | null>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editForm, setEditForm] = useState({
    diagnosis: "",
    keywords: "",
    summary: "",
    status: "PROCESSING" as StatusOption,
    securityLevel: "CONFIDENTIAL"
  });
  const pageSize = 50;
  const totalPages = Math.max(1, Math.ceil(totalRecords / pageSize));
  const currentPageSafe = Math.min(currentPage, totalPages);

  useEffect(() => {
    onStatusChange?.(status);
  }, [status, onStatusChange]);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const updateStatus = (nextStatus: string) => {
    setStatus(nextStatus);
  };

  const currentStatusLabel = editRecord
    ? `${statusLabels[editRecord.status as StatusOption] || editRecord.status} (${
        editRecord.status
      })`
    : "-";

  const currentStatusTone = editRecord?.status === "COMPLETED" ? "badge success" : "badge warn";

  const formatDateTime = (value?: string | null) => {
    if (!value) {
      return "-";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "-";
    }
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    const seconds = String(date.getSeconds()).padStart(2, "0");
    return `${day}/${month}/${year} – ${hours}:${minutes}:${seconds}`;
  };

  const getStatusLabel = (value?: string | null) => {
    if (!value) {
      return "Không xác định";
    }
    if (value === "PROCESSING" || value === "COMPLETED") {
      return statusLabels[value];
    }
    return "Lưu trữ";
  };

  const getStatusTone = (value?: string | null) => {
    if (value === "COMPLETED") {
      return "badge success";
    }
    if (value === "PROCESSING") {
      return "badge warn";
    }
    return "badge neutral";
  };

  const getStatusHint = (value?: string | null) => {
    if (value === "PROCESSING") {
      return "Hồ sơ đang trong quá trình theo dõi / điều trị";
    }
    if (value === "COMPLETED") {
      return "Hồ sơ đã kết thúc điều trị";
    }
    return "Hồ sơ chỉ đọc, không chỉnh sửa";
  };

  const formatSecurityLevel = (value?: string | null) => {
    if (!value) {
      return "-";
    }
    return securityLevelLabels[value] || value;
  };

  const renderKeywordTags = (keywords?: string | null) => {
    if (!keywords) {
      return <div className="muted-inline">Chưa có dữ liệu</div>;
    }
    const items = keywords
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean);
    if (items.length === 0) {
      return <div className="muted-inline">Chưa có dữ liệu</div>;
    }
    return (
      <div className="tag-list">
        {items.map((item) => (
          <span key={item} className="tag">
            {item}
          </span>
        ))}
      </div>
    );
  };

  const ensureDoctorAuth = () => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");
    if (!token) {
      updateStatus("Bạn chưa đăng nhập. Vui lòng đăng nhập tài khoản bác sĩ.");
      return false;
    }
    if (role !== "DOCTOR") {
      updateStatus("Tài khoản hiện tại không có quyền tạo/sửa/xóa hồ sơ.");
      return false;
    }
    return true;
  };

  const handleSearch = async (page = 1) => {
    const patientValue = patientCode.trim();
    const keywordValue = keywordQuery.trim();
    const recordIdValue = recordIdQuery.trim();
    const hasPatient = patientValue.length > 0;
    const patientValid = !hasPatient || /^BN-\d+$/.test(patientValue);

    if (hasPatient && !patientValid) {
      updateStatus("Mã bệnh nhân không hợp lệ. Định dạng BN-<số>.");
      setRecords([]);
      setTotalRecords(0);
      setCurrentPage(1);
      setDetail(null);
      return;
    }

    if (!keywordValue && !recordIdValue && !hasPatient) {
      updateStatus("Vui lòng nhập mã bệnh nhân, mã hồ sơ hoặc từ khóa.");
      setRecords([]);
      setTotalRecords(0);
      setCurrentPage(1);
      setDetail(null);
      return;
    }

    if (recordIdValue && !/^\d+$/.test(recordIdValue)) {
      updateStatus("Mã hồ sơ không hợp lệ.");
      setRecordDetail(null);
      setTotalRecords(0);
      setCurrentPage(1);
      return;
    }

    updateStatus("Đang tìm kiếm...");
    setLoading(true);
    try {
      const data = await searchRecords({
        keyword: keywordValue || undefined,
        patientCode: hasPatient ? patientValue : undefined,
        recordId: recordIdValue ? Number(recordIdValue) : undefined,
        page: Math.max(0, page - 1),
        size: pageSize
      });
      const items = data.items ?? [];
      setRecords(items);
      setTotalRecords(data.total ?? 0);
      setCurrentPage(page);
      if (hasPatient) {
        setDetail(null);
        void getDoctorPatientBasic(patientValue)
          .then(setDetail)
          .catch(() => setDetail(null));
      } else {
        setDetail(null);
      }
      setRecordDetail(null);
      if (items.length === 0 && recordIdValue) {
        updateStatus(`Không tìm thấy hồ sơ mã #${recordIdValue}.`);
      } else {
        updateStatus(items.length === 0 ? "Không tìm thấy hồ sơ." : "Tìm kiếm hoàn tất.");
      }
    } catch (err) {
      updateStatus(err instanceof Error ? err.message : "Tìm kiếm thất bại.");
      setRecords([]);
      setTotalRecords(0);
      setCurrentPage(1);
      setDetail(null);
      setRecordDetail(null);
    } finally {
      setLoading(false);
    }
  };

  const handleLoadEdit = async (recordId: number) => {
    if (!ensureDoctorAuth()) {
      return;
    }
    setActionLoading(true);
    updateStatus("Đang tải hồ sơ...");
    try {
      const data = await getDoctorRecord(recordId);
      setEditRecord(data);
      setEditForm({
        diagnosis: "",
        keywords: data.keywords || "",
        summary: data.summary || "",
        status: (data.status as StatusOption) || "PROCESSING",
        securityLevel: data.securityLevel || "CONFIDENTIAL"
      });
      updateStatus("Đã tải hồ sơ.");
      setShowEditModal(true);
    } catch (err) {
      setEditRecord(null);
      updateStatus(err instanceof Error ? err.message : "Không thể tải hồ sơ.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleUpdate = async () => {
    if (!editRecord) {
      updateStatus("Vui lòng chọn hồ sơ để cập nhật.");
      return;
    }
    if (!ensureDoctorAuth()) {
      return;
    }
    setActionLoading(true);
    updateStatus("Đang cập nhật hồ sơ...");
    try {
      await updateDoctorRecord(editRecord.recordId, {
        diagnosis: editForm.diagnosis || undefined,
        keywords: editForm.keywords || undefined,
        summary: editForm.summary || undefined,
        status: editForm.status,
        securityLevel: editForm.securityLevel
      });
      updateStatus("Cập nhật hồ sơ thành công.");
      if (/^BN-\d+$/.test(patientCode)) {
        handleSearch(currentPageSafe);
      }
    } catch (err) {
      updateStatus(err instanceof Error ? err.message : "Cập nhật thất bại.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (recordId: number) => {
    if (!ensureDoctorAuth()) {
      return;
    }
    if (!window.confirm(`Bạn có chắc chắn muốn xóa hồ sơ #${recordId}?`)) {
      updateStatus("Đã hủy xóa.");
      return;
    }
    setActionLoading(true);
    updateStatus("Đang xóa hồ sơ...");
    try {
      await deleteDoctorRecord(recordId);
      setRecords((prev) =>
        prev.filter((item) => (item.recordId ?? item.id) !== recordId)
      );
      setTotalRecords((prev) => Math.max(0, prev - 1));
      if (recordDetail?.recordId === recordId) {
        setRecordDetail(null);
      }
      if (editRecord?.recordId === recordId) {
        setEditRecord(null);
      }
      updateStatus("Xóa hồ sơ thành công.");
      setShowEditModal(false);
      setShowDetailModal(false);
    } catch (err) {
      updateStatus(err instanceof Error ? err.message : "Xóa thất bại.");
    } finally {
      setActionLoading(false);
    }
  };


  return (
    <div className="page">
      <section className="panel manage-section">
        <div className="section-head">
          <div>
            <h3>Tra cứu hồ sơ</h3>
            <p className="subtle">Tìm theo mã bệnh nhân để hiển thị các hồ sơ.</p>
          </div>
          <span className="chip">Tra cứu</span>
        </div>
                <form
          className="search-row"
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch(1);
          }}
        >
          <label className="search-field">
            Mã bệnh nhân
            <input
              value={patientCode}
              onChange={(e) => setPatientCode(e.target.value)}
              placeholder="BN-3"
            />
          </label>
          <label className="search-field">
            Mã hồ sơ (tùy chọn)
            <input
              value={recordIdQuery}
              onChange={(e) => setRecordIdQuery(e.target.value)}
              placeholder="1"
            />
          </label>
          <label className="search-field">
            Từ khóa
            <input
              value={keywordQuery}
              onChange={(e) => setKeywordQuery(e.target.value)}
              placeholder="sốt, ho"
            />
          </label>
          <button
            className="primary"
            type="submit"
            disabled={
              loading ||
              (!keywordQuery.trim() && !/^BN-\d+$/.test(patientCode.trim()))
            }
          >
            {loading ? "Đang tìm kiếm..." : "Tìm kiếm"}
          </button>
        </form>
        {detail ? (
          <div className="detail-grid">
            <div>
              <div className="detail-label">Bệnh nhân</div>
              <div className="detail-value">{detail.fullName}</div>
            </div>
            <div>
              <div className="detail-label">Mã bệnh nhân</div>
              <div className="detail-value">{detail.patientCode}</div>
            </div>
            <div>
              <div className="detail-label">Ngày sinh</div>
              <div className="detail-value">{detail.dob || "-"}</div>
            </div>
            <div>
              <div className="detail-label">Giới tính</div>
              <div className="detail-value">{detail.gender || "-"}</div>
            </div>
          </div>
        ) : null}
        <DataTable
          rows={records}
          onView={async (recordId) => {
            updateStatus("Đang tải chi tiết hồ sơ...");
            try {
              const data = await getDoctorRecordDetail(recordId);
              setRecordDetail(data);
              updateStatus("Đã tải chi tiết hồ sơ.");
              setShowDetailModal(true);
            } catch (err) {
              setRecordDetail(null);
              updateStatus("Không thể tải chi tiết hồ sơ.");
            }
          }}
          onEdit={handleLoadEdit}
          onDelete={handleDelete}
        />
        {totalRecords > pageSize ? (
          <div className="pagination">
            <button
              className="ghost small"
              type="button"
              onClick={() => handleSearch(Math.max(1, currentPageSafe - 1))}
              disabled={loading || currentPageSafe === 1}
            >
              Trước
            </button>
            <span>
              Trang {currentPageSafe} / {totalPages} | Tổng {totalRecords}
            </span>
            <button
              className="ghost small"
              type="button"
              onClick={() => handleSearch(Math.min(totalPages, currentPageSafe + 1))}
              disabled={loading || currentPageSafe === totalPages}
            >
              Sau
            </button>
          </div>
        ) : null}
      </section>

      {showDetailModal ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal">
            <div className="modal-header">
              <div>
                <h3>Chi tiết hồ sơ</h3>
                <p className="subtle">Bác sĩ xem toàn bộ chi tiết hồ sơ.</p>
              </div>
            </div>
            {!recordDetail ? (
              <div className="empty">Không có dữ liệu.</div>
            ) : (
              <>
                <div className="meta-box">
                  <div className="section-title">Thông tin định danh</div>
                  <div className="meta-grid">
                    <div>
                      <div className="detail-label">Mã hồ sơ</div>
                      <div className="detail-value">#{recordDetail.recordId}</div>
                    </div>
                    <div>
                      <div className="detail-label">Mức độ bảo mật</div>
                      <div className="detail-value">
                        {formatSecurityLevel(recordDetail.securityLevel)}
                      </div>
                    </div>
                    <div>
                      <div className="detail-label">Trạng thái</div>
                      <div
                        className={getStatusTone(recordDetail.status)}
                        title={getStatusHint(recordDetail.status)}
                      >
                        {getStatusLabel(recordDetail.status)} ({recordDetail.status})
                      </div>
                    </div>
                    <div>
                      <div className="detail-label">Ngày tạo hồ sơ</div>
                      <div className="detail-value">
                        {formatDateTime(recordDetail.createdAt)}
                      </div>
                    </div>
                    <div>
                      <div className="detail-label">Cập nhật lần cuối</div>
                      <div className="detail-value">
                        {formatDateTime(recordDetail.updatedAt)}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="content-box">
                  <div className="section-title">Nội dung y khoa chính</div>
                  <div className="detail-block">
                    <div className="detail-label">Chẩn đoán</div>
                    <div className="detail-strong">
                      {recordDetail.diagnosis || "Chưa có dữ liệu"}
                    </div>
                  </div>
                  <div className="detail-block">
                    <div className="detail-label">Tóm tắt</div>
                    <div className="detail-value">
                      {recordDetail.summary || "Chưa có dữ liệu"}
                    </div>
                  </div>
                </div>

                <div className="meta-box">
                  <div className="section-title">Tìm kiếm & phân loại</div>
                  <div className="detail-label">Từ khóa</div>
                  {renderKeywordTags(recordDetail.keywords)}
                </div>

                <div className="meta-box">
                  <div className="section-title">Lịch sử thao tác</div>
                  <div className="audit-list">
                    <div className="audit-item">
                      {formatDateTime(recordDetail.createdAt)} – doctor01 – Tạo hồ sơ
                    </div>
                    <div className="audit-item">
                      {formatDateTime(recordDetail.updatedAt)} – doctor01 – Cập nhật trạng thái
                    </div>
                  </div>
                </div>
              </>
            )}
            <div className="modal-actions">
              <button className="ghost" onClick={() => setShowDetailModal(false)}>
                Đóng
              </button>
              {recordDetail ? (
                <button
                  className="primary"
                  onClick={() => {
                    setShowDetailModal(false);
                    handleLoadEdit(recordDetail.recordId);
                  }}
                >
                  Chỉnh sửa hồ sơ
                </button>
              ) : null}
            </div>
          </div>
        </div>
      ) : null}

      {showEditModal ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal">
            <div className="modal-header">
              <div>
                <h3>Chỉnh sửa hồ sơ</h3>
                <p className="subtle">Cập nhật nội dung hoặc trạng thái hồ sơ.</p>
              </div>
            </div>
            <div className="modal-section">
              <div className="section-title">Thông tin hồ sơ</div>
              <div className="meta-grid">
                <div>
                  <div className="detail-label">Mã hồ sơ</div>
                  <div className="detail-value">#{editRecord?.recordId || "-"}</div>
                </div>
                <div>
                  <div className="detail-label">Trạng thái hiện tại</div>
                  <div className={currentStatusTone}>{currentStatusLabel}</div>
                </div>
                <div>
                  <div className="detail-label">Cập nhật lần cuối</div>
                  <div className="detail-value">
                    {editRecord?.updatedAt ? formatDateTime(editRecord.updatedAt) : "-"}
                  </div>
                </div>
                <div>
                  <div className="detail-label">Thực hiện bởi</div>
                  <div className="detail-value">-</div>
                </div>
              </div>
            </div>
            <div className="form-grid compact">
              <label>
                Trạng thái
                <select
                  className="select"
                  value={editForm.status}
                  onChange={(e) =>
                    setEditForm({ ...editForm, status: e.target.value as StatusOption })
                  }
                >
                  <option value="PROCESSING">ĐANG XỬ LÝ</option>
                  <option value="COMPLETED">HOÀN TẤT</option>
                </select>
              </label>
              <label>
                Mức độ bảo mật
                <select
                  className="select"
                  value={editForm.securityLevel}
                  onChange={(e) =>
                    setEditForm({ ...editForm, securityLevel: e.target.value })
                  }
                >
                  <option value="PUBLIC">{securityLevelLabels.PUBLIC}</option>
                  <option value="CONFIDENTIAL">{securityLevelLabels.CONFIDENTIAL}</option>
                  <option value="RESTRICTED">{securityLevelLabels.RESTRICTED}</option>
                </select>
              </label>
              <div className="helper">
                {`Mới: Hồ sơ vừa được tạo. Đang xử lý: ${statusDescriptions.PROCESSING}. Hoàn tất: ${statusDescriptions.COMPLETED}. Lưu trữ: Chỉ đọc, không chỉnh sửa.`}
              </div>
              <label>
                Chẩn đoán (tùy chọn)
                <input
                  value={editForm.diagnosis}
                  onChange={(e) => setEditForm({ ...editForm, diagnosis: e.target.value })}
                  placeholder="VD: Viêm đường hô hấp trên"
                />
              </label>
              <div className="helper">Chỉ cập nhật khi đã có kết luận y khoa.</div>
              <label>
                Từ khóa
                <input
                  value={editForm.keywords}
                  onChange={(e) => setEditForm({ ...editForm, keywords: e.target.value })}
                  placeholder="sốt, ho"
                />
              </label>
              <div className="helper">Phân tách bằng dấu phẩy. Ví dụ: sốt, ho</div>
              <label>
                Tóm tắt
                <input
                  value={editForm.summary}
                  onChange={(e) => setEditForm({ ...editForm, summary: e.target.value })}
                />
              </label>
            </div>
            <div className="modal-actions">
              <button className="ghost" onClick={() => setShowEditModal(false)}>
                Đóng
              </button>
              <button
                className="danger-soft"
                onClick={() => (editRecord ? handleDelete(editRecord.recordId) : null)}
                disabled={actionLoading || !editRecord}
                title="Hành động không thể hoàn tác"
              >
                Xóa hồ sơ
              </button>
              <button
                className="primary"
                onClick={handleUpdate}
                disabled={actionLoading || !editRecord}
              >
                Lưu thay đổi
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
