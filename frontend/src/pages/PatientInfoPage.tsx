import { useEffect, useMemo, useState } from "react";
import { getPatientHistory, getPatientProfile } from "../api";
import type { MedicalRecord, PatientProfile } from "../types";

type PatientInfoPageProps = {
  patientCode: string | null;
};

export default function PatientInfoPage({ patientCode }: PatientInfoPageProps) {
  const [profile, setProfile] = useState<PatientProfile | null>(null);
  const [history, setHistory] = useState<MedicalRecord[]>([]);
  const [status, setStatus] = useState("");

  useEffect(() => {
    getPatientProfile()
      .then((data) => setProfile(data))
      .catch(() => setStatus("Tải hồ sơ thất bại."));
    getPatientHistory()
      .then((data) => setHistory(data))
      .catch(() => setStatus("Tải lịch sử thất bại."));
  }, []);

  const totalRecords = useMemo(() => history.length, [history]);
  const lastUpdate = useMemo(() => {
    if (history.length === 0) {
      return "-";
    }
    const sorted = [...history].sort(
      (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    );
    return new Date(sorted[0].updatedAt).toLocaleString();
  }, [history]);

  return (
    <div className="page">
      <section className="panel patient-hero">
        <div>
          <p className="eyebrow">Thông tin bệnh nhân</p>
          <h2>Tổng quan bệnh nhân</h2>
          <p className="subtle">Thông tin định danh và tổng quan trạng thái hồ sơ.</p>
        </div>
        <div className="patient-code">
          Mã bệnh nhân
          <span>{patientCode || "Không có"}</span>
        </div>
      </section>

      <section className="panel info-grid">
        <div className="info-card">
          <div className="detail-label">Số lượng hồ sơ</div>
          <div className="detail-value">{totalRecords}</div>
        </div>
        <div className="info-card">
          <div className="detail-label">Cập nhật gần nhất</div>
          <div className="detail-value">{lastUpdate}</div>
        </div>
        <div className="info-card">
          <div className="detail-label">Trạng thái</div>
          <div className="detail-value">{totalRecords > 0 ? "Đang hoạt động" : "Chưa có hồ sơ"}</div>
        </div>
        <div className="info-card">
          <div className="detail-label">Hồ sơ</div>
          <div className="detail-value">{profile ? "Đã xác minh" : "Chưa xác minh"}</div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h3>Thông tin định danh bệnh nhân</h3>
          <p className="subtle">Chỉ metadata. Không hiển thị dữ liệu nhạy cảm.</p>
        </div>
        <div className="detail-grid">
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
      </section>

      <section className="panel">
        <div className="panel-head">
          <h3>Danh sách trạng thái hồ sơ</h3>
          <p className="subtle">Theo dõi theo mã hồ sơ và trạng thái.</p>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Mã hồ sơ</th>
                <th>Trạng thái</th>
                <th>Cập nhật</th>
                <th>Tóm tắt</th>
              </tr>
            </thead>
            <tbody>
              {history.length === 0 ? (
                <tr>
                  <td colSpan={4} className="empty">
                    Không tìm thấy hồ sơ.
                  </td>
                </tr>
              ) : (
                history.map((item) => (
                  <tr key={item.id}>
                    <td>{item.id}</td>
                    <td>{item.status}</td>
                    <td>{new Date(item.updatedAt).toLocaleString()}</td>
                    <td>{item.metadata?.summary || "-"}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        {status ? <div className="status">{status}</div> : null}
      </section>
    </div>
  );
}
