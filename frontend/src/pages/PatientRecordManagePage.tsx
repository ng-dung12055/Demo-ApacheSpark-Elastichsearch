import { useCallback, useEffect, useMemo, useState } from "react";
import { getPatientHistoryPage, getPatientProfile, getPatientRecordDetail } from "../api";
import DataTable from "../components/DataTable";
import type {
  MedicalRecord,
  MetadataRecord,
  PatientProfile,
  PatientRecordDetail
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

type PatientRecordManagePageProps = {
  patientCode: string | null;
  onStatusChange?: (status: string) => void;
};

export default function PatientRecordManagePage({
  patientCode,
  onStatusChange
}: PatientRecordManagePageProps) {
  const [profile, setProfile] = useState<PatientProfile | null>(null);
  const [history, setHistory] = useState<MedicalRecord[]>([]);
  const [totalRecords, setTotalRecords] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<MedicalRecord | null>(null);
  const [recordDetail, setRecordDetail] = useState<PatientRecordDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const pageSize = 50;
  const totalPages = Math.max(1, Math.ceil(totalRecords / pageSize));
  const currentPageSafe = Math.min(currentPage, totalPages);

  useEffect(() => {
    onStatusChange?.(status);
  }, [status, onStatusChange]);

  const updateStatus = useCallback((nextStatus: string) => {
    setStatus(nextStatus);
  }, []);

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
    return `${day}/${month}/${year} - ${hours}:${minutes}:${seconds}`;
  };

  const getStatusLabel = (value?: string | null) => {
    if (!value) {
      return "Không xác định";
    }
    if (value === "PROCESSING" || value === "COMPLETED") {
      return `${statusLabels[value]} (${value})`;
    }
    return value;
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

  const loadRecords = useCallback(async (page = 1) => {
    if (!patientCode) {
      updateStatus("Không tìm thấy mã bệnh nhân trong phiên đăng nhập.");
      return;
    }
    setLoading(true);
    updateStatus("Đang tải hồ sơ...");
    try {
      const pageResult = await getPatientHistoryPage(Math.max(0, page - 1), pageSize);
      const items = pageResult.items ?? [];
      setHistory(items);
      setTotalRecords(pageResult.total ?? 0);
      setCurrentPage(page);
      updateStatus(items.length === 0 ? "Chưa có hồ sơ." : "Tải hồ sơ thành công.");
    } catch (err) {
      setHistory([]);
      setTotalRecords(0);
      setCurrentPage(1);
      updateStatus("Tải hồ sơ thất bại.");
    }
    setLoading(false);
  }, [patientCode, pageSize, updateStatus]);

  useEffect(() => {
    if (!patientCode) {
      setProfile(null);
      return;
    }
    getPatientProfile()
      .then((data) => setProfile(data))
      .catch(() => updateStatus("Tải thông tin bệnh nhân thất bại."));
  }, [patientCode, updateStatus]);

  useEffect(() => {
    loadRecords(1);
  }, [loadRecords]);

  const tableRows = useMemo<MetadataRecord[]>(
    () =>
      history.map((item) => {
        const metadata = item.metadata ?? null;
        return {
          id: metadata?.id ?? item.id,
          recordId: metadata?.recordId ?? item.id,
          patientCode: metadata?.patientCode ?? patientCode ?? "-",
          patientName: metadata?.patientName ?? "Bệnh nhân",
          keywords: metadata?.keywords ?? null,
          summary: metadata?.summary ?? null,
          updatedAt: metadata?.updatedAt ?? item.updatedAt
        };
      }),
    [history, patientCode]
  );

  const handleView = async (recordId: number) => {
    const record = history.find(
      (item) => (item.metadata?.recordId ?? item.id) === recordId
    );
    if (!record) {
      updateStatus("Không tìm thấy hồ sơ để xem.");
      return;
    }
    setSelectedRecord(record);
    setRecordDetail(null);
    setShowDetailModal(true);
    setDetailLoading(true);
    updateStatus("Đang tải chi tiết hồ sơ...");
    try {
      const detail = await getPatientRecordDetail(recordId);
      setRecordDetail(detail);
      updateStatus("Đã tải chi tiết hồ sơ.");
    } catch (err) {
      setRecordDetail(null);
      updateStatus("Không thể tải chi tiết hồ sơ.");
    } finally {
      setDetailLoading(false);
    }
  };

  const recordMeta = selectedRecord?.metadata ?? null;

  return (
    <div className="page">
      <section className="panel manage-section">
        <div className="section-head">
          <div>
            <h3>Tra cứu hồ sơ</h3>
            <p className="subtle">
              Bạn có thể xem hồ sơ của mình nhưng không có quyền sửa hoặc xóa.
            </p>
          </div>
          <span className="chip">Tra cứu</span>
        </div>
        <form
          className="search-row"
          onSubmit={(e) => {
            e.preventDefault();
            loadRecords(1);
          }}
        >
          <label className="search-field">
            Mã bệnh nhân
            <input value={patientCode || ""} readOnly />
          </label>
          <button className="primary" type="submit" disabled={loading || !patientCode}>
            {loading ? "Đang tải..." : "Tải hồ sơ"}
          </button>
        </form>
        <div className="detail-grid">
          <div>
            <div className="detail-label">Bệnh nhân</div>
            <div className="detail-value">{profile?.fullName || "Tài khoản của bạn"}</div>
          </div>
          <div>
            <div className="detail-label">Mã bệnh nhân</div>
            <div className="detail-value">{patientCode || "-"}</div>
          </div>
          <div>
            <div className="detail-label">Ngày sinh</div>
            <div className="detail-value">{profile?.dob || "-"}</div>
          </div>
          <div>
            <div className="detail-label">Giới tính</div>
            <div className="detail-value">{profile?.gender || "-"}</div>
          </div>
          <div className="detail-wide">
            <div className="detail-label">Địa chỉ</div>
            <div className="detail-value">{profile?.address || "-"}</div>
          </div>
        </div>
        <DataTable rows={tableRows} onView={handleView} />
        {totalRecords > pageSize ? (
          <div className="pagination">
            <button
              className="ghost small"
              type="button"
              onClick={() => loadRecords(Math.max(1, currentPageSafe - 1))}
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
              onClick={() => loadRecords(Math.min(totalPages, currentPageSafe + 1))}
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
                <p className="subtle">Xem thông tin hồ sơ của bạn.</p>
              </div>
            </div>
            {!selectedRecord ? (
              <div className="empty">Không có dữ liệu.</div>
            ) : (
              <>
                <div className="meta-box">
                  <div className="section-title">Thông tin định danh</div>
                  <div className="meta-grid">
                    <div>
                      <div className="detail-label">Mã hồ sơ</div>
                      <div className="detail-value">
                        #{recordDetail?.recordId ?? recordMeta?.recordId ?? selectedRecord.id}
                      </div>
                    </div>
                    <div>
                      <div className="detail-label">Mức độ bảo mật</div>
                      <div className="detail-value">
                        {formatSecurityLevel(recordDetail?.securityLevel)}
                      </div>
                    </div>
                    <div>
                      <div className="detail-label">Trạng thái</div>
                      <div
                        className={getStatusTone(recordDetail?.status ?? selectedRecord.status)}
                        title={recordDetail?.status ?? selectedRecord.status}
                      >
                        {getStatusLabel(recordDetail?.status ?? selectedRecord.status)}
                      </div>
                    </div>
                    <div>
                      <div className="detail-label">Ngày tạo</div>
                      <div className="detail-value">
                        {formatDateTime(recordDetail?.createdAt ?? selectedRecord.createdAt)}
                      </div>
                    </div>
                    <div>
                      <div className="detail-label">Cập nhật lần cuối</div>
                      <div className="detail-value">
                        {formatDateTime(recordDetail?.updatedAt ?? selectedRecord.updatedAt)}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="content-box">
                  <div className="section-title">Tóm tắt hồ sơ</div>
                  <div className="detail-block">
                    <div className="detail-label">Chẩn đoán</div>
                    <div className="detail-strong">
                      {detailLoading
                        ? "Đang tải..."
                        : recordDetail?.diagnosis || "Chưa có dữ liệu"}
                    </div>
                  </div>
                  <div className="detail-block">
                    <div className="detail-label">Tóm tắt</div>
                    <div className="detail-value">
                      {recordDetail?.summary ||
                        recordMeta?.summary ||
                        "Chưa có dữ liệu"}
                    </div>
                  </div>
                </div>

                <div className="meta-box">
                  <div className="section-title">Từ khóa</div>
                  {renderKeywordTags(recordDetail?.keywords ?? recordMeta?.keywords)}
                </div>
              </>
            )}
            <div className="modal-actions">
              <button className="ghost" onClick={() => setShowDetailModal(false)}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}





