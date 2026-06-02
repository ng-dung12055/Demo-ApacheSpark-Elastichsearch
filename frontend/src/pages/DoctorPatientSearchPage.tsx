import { useEffect, useState } from "react";
import { getDoctorPatientBasic } from "../api";
import type { DoctorPatientBasic } from "../types";

type DoctorPatientSearchPageProps = {
  onStatusChange?: (status: string) => void;
};

export default function DoctorPatientSearchPage({
  onStatusChange
}: DoctorPatientSearchPageProps) {
  const [patientCode, setPatientCode] = useState("BN-3");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<DoctorPatientBasic | null>(null);

  useEffect(() => {
    onStatusChange?.(status);
  }, [status, onStatusChange]);

  const handleSearch = async () => {
    if (!/^BN-\d+$/.test(patientCode)) {
      setStatus("Định dạng mã bệnh nhân không hợp lệ. Dùng BN-<số>.");
      setDetail(null);
      return;
    }
    setStatus("Đang tìm kiếm bệnh nhân...");
    setLoading(true);
    try {
      const data = await getDoctorPatientBasic(patientCode);
      setDetail(data);
      setStatus("Tìm kiếm hoàn tất.");
    } catch (err) {
      setDetail(null);
      setStatus(err instanceof Error ? err.message : "Không tìm thấy bệnh nhân.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <section className="panel manage-section">
        <div className="section-head">
          <div>
            <h3>Tra cứu bệnh nhân</h3>
            <p className="subtle">Tra cứu thông tin cơ bản, không hiển thị bệnh án.</p>
          </div>
          <span className="chip">Bệnh nhân</span>
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
            />
          </label>
          <button
            className="primary"
            type="submit"
            disabled={loading || !/^BN-\d+$/.test(patientCode)}
          >
            {loading ? "Đang tìm..." : "Tìm kiếm"}
          </button>
        </form>
        {status ? <div className="status">{status}</div> : null}

        {detail ? (
          <div className="detail-grid">
            <div>
              <div className="detail-label">Họ và tên</div>
              <div className="detail-value">{detail.fullName || "-"}</div>
            </div>
            <div>
              <div className="detail-label">Mã bệnh nhân</div>
              <div className="detail-value">{detail.patientCode}</div>
            </div>
            <div>
              <div className="detail-label">Số điện thoại</div>
              <div className="detail-value">{detail.phone || "-"}</div>
            </div>
            <div>
              <div className="detail-label">Email</div>
              <div className="detail-value">{detail.email || "-"}</div>
            </div>
            <div>
              <div className="detail-label">Ngày sinh</div>
              <div className="detail-value">{detail.dob || "-"}</div>
            </div>
            <div>
              <div className="detail-label">Giới tính</div>
              <div className="detail-value">{detail.gender || "-"}</div>
            </div>
            <div className="detail-wide">
              <div className="detail-label">Địa chỉ</div>
              <div className="detail-value">{detail.address || "-"}</div>
            </div>
          </div>
        ) : (
          <div className="empty">Chưa có dữ liệu bệnh nhân.</div>
        )}
      </section>
    </div>
  );
}
