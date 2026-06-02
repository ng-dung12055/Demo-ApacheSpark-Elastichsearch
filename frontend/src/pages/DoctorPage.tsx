import { useState } from "react";
import { getDoctorPatientDetail, getDoctorRecordDetail, searchByPatientCode } from "../api";
import DataTable from "../components/DataTable";
import type { DoctorPatientDetail, DoctorRecordDetail, MetadataRecord } from "../types";

export default function DoctorPage() {
  const [patientCode, setPatientCode] = useState("BN-3");
  const [records, setRecords] = useState<MetadataRecord[]>([]);
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<DoctorPatientDetail | null>(null);
  const [recordDetail, setRecordDetail] = useState<DoctorRecordDetail | null>(null);

  const handleSearch = async () => {
    if (!/^BN-\d+$/.test(patientCode)) {
      setStatus("Định dạng mã bệnh nhân không hợp lệ. Dùng BN-<số>.");
      setRecords([]);
      setDetail(null);
      return;
    }
    setStatus("Đang tìm kiếm...");
    setLoading(true);
    try {
      const data = await searchByPatientCode(patientCode);
      setRecords(data);
      setStatus(data.length === 0 ? "Không tìm thấy hồ sơ." : "Tìm kiếm hoàn tất.");
      if (data.length > 0) {
        const full = await getDoctorPatientDetail(patientCode);
        setDetail(full);
      } else {
        setDetail(null);
      }
      setRecordDetail(null);
    } catch (err) {
      setStatus("Không tìm thấy mã bệnh nhân.");
      setRecords([]);
      setDetail(null);
      setRecordDetail(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="panel">
        <div className="panel-head">
          <h2>Tìm hồ sơ bệnh nhân</h2>
          <p className="subtle">Tìm theo mã bệnh nhân (chỉ metadata).</p>
        </div>
        <form
          className="search-row"
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch();
          }}
        >
          <label className="search-field">
            Mã bệnh nhân
            <input
              value={patientCode}
              onChange={(e) => setPatientCode(e.target.value)}
              placeholder="BN-3"
              aria-describedby="patient-code-help"
            />
          </label>
          <button
            className="primary"
            type="submit"
            disabled={loading || !/^BN-\d+$/.test(patientCode)}
          >
            {loading ? "Đang tìm kiếm..." : "Tìm kiếm"}
          </button>
        </form>
        <div id="patient-code-help" className="helper">
          Định dạng: BN-{"<số>"}. Ví dụ: BN-3
        </div>
        {status ? <div className="status">{status}</div> : null}
        <DataTable
          rows={records}
          onView={async (recordId) => {
            setStatus("Đang tải chi tiết hồ sơ...");
            try {
              const data = await getDoctorRecordDetail(recordId);
              setRecordDetail(data);
              setStatus("Đã tải chi tiết hồ sơ.");
            } catch (err) {
              setRecordDetail(null);
              setStatus("Không thể tải chi tiết hồ sơ.");
            }
          }}
        />
      </div>

      <div className="panel">
        <div className="panel-head">
          <h2>Chi tiết hồ sơ</h2>
          <p className="subtle">Bác sĩ xem toàn bộ chi tiết hồ sơ.</p>
        </div>
        {!recordDetail ? (
          <div className="empty">Chọn hồ sơ để xem chi tiết.</div>
        ) : (
          <div className="detail-grid">
            <div>
              <div className="detail-label">Mã hồ sơ</div>
              <div className="detail-value">{recordDetail.recordId}</div>
            </div>
            <div>
              <div className="detail-label">Trạng thái</div>
              <div className="detail-value">{recordDetail.status}</div>
            </div>
            <div>
              <div className="detail-label">Tạo lúc</div>
              <div className="detail-value">
                {new Date(recordDetail.createdAt).toLocaleString()}
              </div>
            </div>
            <div>
              <div className="detail-label">Cập nhật lúc</div>
              <div className="detail-value">
                {new Date(recordDetail.updatedAt).toLocaleString()}
              </div>
            </div>
            <div className="detail-wide">
              <div className="detail-label">Chẩn đoán</div>
              <div className="detail-value">{recordDetail.diagnosis || "-"}</div>
            </div>
            <div className="detail-wide">
              <div className="detail-label">Tóm tắt</div>
              <div className="detail-value">{recordDetail.summary || "-"}</div>
            </div>
            <div className="detail-wide">
              <div className="detail-label">Từ khóa</div>
              <div className="detail-value">{recordDetail.keywords || "-"}</div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
