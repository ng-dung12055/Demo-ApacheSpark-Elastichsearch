import { useEffect, useState } from "react";
import { createRecord } from "../api";

type DoctorRecordCreatePageProps = {
  onStatusChange?: (status: string) => void;
};

const securityLevelLabels: Record<string, string> = {
  PUBLIC: "Công khai",
  CONFIDENTIAL: "Bảo mật",
  RESTRICTED: "Hạn chế"
};

export default function DoctorRecordCreatePage({ onStatusChange }: DoctorRecordCreatePageProps) {
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    patientCode: "",
    diagnosis: "",
    keywords: "",
    summary: "",
    securityLevel: "CONFIDENTIAL"
  });

  useEffect(() => {
    onStatusChange?.(status);
  }, [status, onStatusChange]);

  const updateStatus = (nextStatus: string) => {
    setStatus(nextStatus);
  };

  const ensureDoctorAuth = () => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");
    if (!token) {
      updateStatus("Bạn chưa đăng nhập. Vui lòng đăng nhập tài khoản bác sĩ.");
      return false;
    }
    if (role !== "DOCTOR") {
      updateStatus("Tài khoản hiện tại không có quyền tạo hồ sơ.");
      return false;
    }
    return true;
  };

  const handleAdd = async () => {
    if (!/^BN-\d+$/.test(form.patientCode) || !form.diagnosis.trim()) {
      updateStatus("Dữ liệu không hợp lệ. Kiểm tra mã bệnh nhân và chẩn đoán.");
      return;
    }
    if (!ensureDoctorAuth()) {
      return;
    }
    setLoading(true);
    updateStatus("Đang tạo hồ sơ...");
    try {
      await createRecord({
        patientCode: form.patientCode,
        diagnosis: form.diagnosis,
        keywords: form.keywords || undefined,
        summary: form.summary || undefined,
        securityLevel: form.securityLevel || undefined
      });
      updateStatus("Tạo hồ sơ thành công.");
      setForm({
        patientCode: "",
        diagnosis: "",
        keywords: "",
        summary: "",
        securityLevel: "CONFIDENTIAL"
      });
    } catch (err) {
      updateStatus(err instanceof Error ? err.message : "Tạo hồ sơ thất bại.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <section className="panel manage-section">
        <div className="section-head">
          <div>
            <h3>Thêm hồ sơ mới</h3>
            <p className="subtle">Tạo hồ sơ y tế mới cho bệnh nhân.</p>
          </div>
          <span className="chip">Bắt buộc</span>
        </div>
        <div className="form-grid compact">
          <label>
            Mã bệnh nhân
            <input
              value={form.patientCode}
              onChange={(e) => setForm({ ...form, patientCode: e.target.value })}
              placeholder="BN-3"
            />
            <span className="helper">Định dạng: BN-{"<số>"}</span>
          </label>
          <label>
            Chẩn đoán
            <input
              value={form.diagnosis}
              onChange={(e) => setForm({ ...form, diagnosis: e.target.value })}
            />
          </label>
          <label>
            Mức độ bảo mật
            <select
              className="select"
              value={form.securityLevel}
              onChange={(e) => setForm({ ...form, securityLevel: e.target.value })}
            >
              <option value="PUBLIC">{securityLevelLabels.PUBLIC}</option>
              <option value="CONFIDENTIAL">{securityLevelLabels.CONFIDENTIAL}</option>
              <option value="RESTRICTED">{securityLevelLabels.RESTRICTED}</option>
            </select>
          </label>
          <label>
            Từ khóa
            <input
              value={form.keywords}
              onChange={(e) => setForm({ ...form, keywords: e.target.value })}
              placeholder="sốt, ho"
            />
          </label>
          <label>
            Tóm tắt
            <input
              value={form.summary}
              onChange={(e) => setForm({ ...form, summary: e.target.value })}
            />
          </label>
        </div>
        <div className="action-row">
          <button
            className="primary"
            onClick={handleAdd}
            disabled={loading || !/^BN-\d+$/.test(form.patientCode)}
          >
            Tạo hồ sơ
          </button>
        </div>
      </section>
    </div>
  );
}
