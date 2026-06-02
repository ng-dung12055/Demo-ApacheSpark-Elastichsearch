import { useState } from "react";
import { register } from "../api";
import type { AuthResponse, RegisterPayload } from "../types";

type RegisterPageProps = {
  onRegister: (auth: AuthResponse) => void;
  onBackToLogin: () => void;
};

export default function RegisterPage({ onRegister, onBackToLogin }: RegisterPageProps) {
  const [form, setForm] = useState<RegisterPayload>({
    username: "",
    password: "",
    fullName: "",
    email: "",
    phone: "",
    cccd: "",
    dob: "",
    gender: "",
    address: ""
  });
  const [patientCode, setPatientCode] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const auth = await register(form);
      if (auth.patientCode) {
        setPatientCode(auth.patientCode);
      }
      onRegister(auth);
    } catch {
      setError("Đăng ký thất bại. Kiểm tra dữ liệu hoặc máy chủ.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <div className="auth-header">
          <p className="eyebrow">Tiếp nhận bệnh nhân</p>
          <h1>Đăng ký bệnh nhân</h1>
          <p className="subtle">Chỉ tạo tài khoản bệnh nhân tại đây.</p>
        </div>
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Họ và tên
            <input
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              required
            />
          </label>
          <label>
            Tên đăng nhập
            <input
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              required
            />
          </label>
          <label>
            Mật khẩu
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </label>
          <label>
            CCCD
            <input
              value={form.cccd}
              onChange={(e) => setForm({ ...form, cccd: e.target.value })}
              required
            />
          </label>
          <label>
            Số điện thoại
            <input
              value={form.phone || ""}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
            />
          </label>
          <label>
            Email
            <input
              value={form.email || ""}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </label>
          <label>
            Ngày sinh (YYYY-MM-DD)
            <input
              value={form.dob || ""}
              onChange={(e) => setForm({ ...form, dob: e.target.value })}
            />
          </label>
          <label>
            Giới tính
            <input
              value={form.gender || ""}
              onChange={(e) => setForm({ ...form, gender: e.target.value })}
            />
          </label>
          <label>
            Địa chỉ
            <input
              value={form.address || ""}
              onChange={(e) => setForm({ ...form, address: e.target.value })}
            />
          </label>
          {error ? <div className="error">{error}</div> : null}
          <button className="primary" type="submit" disabled={loading}>
            {loading ? "Đang đăng ký..." : "Đăng ký"}
          </button>
          <button className="ghost" type="button" onClick={onBackToLogin}>
            Quay lại đăng nhập
          </button>
          {patientCode ? (
            <div className="status">Mã bệnh nhân đã tạo: {patientCode}</div>
          ) : null}
        </form>
      </div>
      <div className="auth-panel">
        <div className="panel-card">
          <h2>Mã bệnh nhân tự động</h2>
          <p>
            Mỗi lần đăng ký sẽ tạo mã bệnh nhân mới (BN-4, BN-5, ...). Chỉ tạo tài khoản
            bệnh nhân trong luồng này.
          </p>
          <div className="panel-grid">
            <div>
              <div className="panel-label">Chiến lược ID</div>
              <div className="panel-value">BN-{`{userId}`}</div>
            </div>
            <div>
              <div className="panel-label">Bảo mật</div>
              <div className="panel-value">CCCD băm + mã hóa</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
