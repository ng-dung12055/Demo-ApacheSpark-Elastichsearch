import { useEffect, useMemo, useState } from "react";
import TopBar from "./components/TopBar";
import SideNav from "./components/SideNav";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import PatientPage from "./pages/PatientPage";
import PatientDashboard from "./pages/PatientDashboard";
import AdminSecurityPage from "./pages/AdminSecurityPage";
import AdminUserManagePage from "./pages/AdminUserManagePage";
import DoctorPatientSearchPage from "./pages/DoctorPatientSearchPage";
import DoctorProfilePage from "./pages/DoctorProfilePage";
import DoctorRecordCreatePage from "./pages/DoctorRecordCreatePage";
import DoctorRecordManagePage from "./pages/DoctorRecordManagePage";
import PatientRecordManagePage from "./pages/PatientRecordManagePage";
import type { AuthResponse, MetadataRecord } from "./types";
import { changeDoctorPassword, changePatientPassword } from "./api";

type View =
  | "adminSecurity"
  | "adminUsers"
  | "dashboard"
  | "doctorCreate"
  | "doctorPatients"
  | "doctorProfile"
  | "doctorRecords"
  | "patient"
  | "patientRecords";

export default function App() {
  const [auth, setAuth] = useState<AuthResponse | null>(() => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");
    const patientCode = localStorage.getItem("patientCode") || undefined;
    return token && role ? { token, role, patientCode } : null;
  });
  const [view, setView] = useState<View>("dashboard");
  const [timeline, setTimeline] = useState<MetadataRecord[]>([]);
  const [topStatus, setTopStatus] = useState<string>("");
  const [patientCode, setPatientCode] = useState<string | null>(
    localStorage.getItem("patientCode")
  );
  const [showRegister, setShowRegister] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordStatus, setPasswordStatus] = useState("");
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: ""
  });

  useEffect(() => {
    if (timeline.length === 0) {
      setTimeline([
        {
          id: 1,
          patientCode: "BN-3",
          patientName: "Bệnh nhân mẫu",
          keywords: "sốt,ho",
          summary: "Khám ban đầu và phân loại.",
          updatedAt: new Date().toISOString()
        }
      ]);
    }
  }, [timeline.length]);

  const handleLogin = (data: AuthResponse) => {
    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role);
    if (data.patientCode) {
      localStorage.setItem("patientCode", data.patientCode);
      setPatientCode(data.patientCode);
    }
    setAuth(data);
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("patientCode");
    setPatientCode(null);
    setAuth(null);
    setShowPasswordModal(false);
    setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
    setPasswordStatus("");
  };

  const allowedViews = useMemo<View[]>(() => {
    if (auth?.role === "ADMIN") {
      return ["dashboard", "adminUsers", "adminSecurity"];
    }
    if (auth?.role === "DOCTOR") {
      return ["dashboard", "doctorPatients", "doctorCreate", "doctorRecords", "doctorProfile"];
    }
    if (auth?.role === "PATIENT") {
      return ["dashboard", "patientRecords", "patient"];
    }
    return ["dashboard"];
  }, [auth?.role]);

  useEffect(() => {
    if (auth?.role) {
      setView("dashboard");
    }
  }, [auth?.role]);

  useEffect(() => {
    setTopStatus("");
  }, [view]);

  useEffect(() => {
    if (auth?.role !== "PATIENT") {
      setShowPasswordModal(false);
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      setPasswordStatus("");
    }
  }, [auth?.role]);

  const handleOpenPasswordModal = () => {
    setPasswordStatus("");
    setShowPasswordModal(true);
  };

  const handleClosePasswordModal = () => {
    setShowPasswordModal(false);
    setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
    setPasswordStatus("");
  };

  const passwordMismatch =
    passwordForm.confirmPassword.length > 0 &&
    passwordForm.newPassword !== passwordForm.confirmPassword;
  const canSubmitPassword =
    passwordForm.currentPassword.trim().length > 0 &&
    passwordForm.newPassword.trim().length > 0 &&
    passwordForm.confirmPassword.trim().length > 0 &&
    !passwordMismatch;

  const handleChangePassword = async () => {
    if (auth?.role !== "PATIENT" && auth?.role !== "DOCTOR") {
      setPasswordStatus("Tài khoản hiện tại không hỗ trợ đổi mật khẩu.");
      return;
    }
    if (
      !passwordForm.currentPassword.trim() ||
      !passwordForm.newPassword.trim() ||
      !passwordForm.confirmPassword.trim()
    ) {
      setPasswordStatus("Vui lòng nhập đầy đủ thông tin mật khẩu.");
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordStatus("Xác nhận mật khẩu không khớp.");
      return;
    }
    setPasswordStatus("Đang đổi mật khẩu...");
    try {
      if (auth?.role === "DOCTOR") {
        await changeDoctorPassword(passwordForm);
      } else {
        await changePatientPassword(passwordForm);
      }
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      setShowPasswordModal(false);
      setTopStatus("Đổi mật khẩu thành công.");
    } catch (err) {
      const message =
        err instanceof Error && err.message ? err.message : "Đổi mật khẩu thất bại.";
      setPasswordStatus(message);
      setTopStatus(message);
    }
  };

  const activeTitle = useMemo(() => {
    if (view === "patient") {
      return "Cổng bệnh nhân";
    }
    if (view === "patientRecords") {
      return "Hồ sơ của tôi";
    }
    if (view === "adminUsers") {
      return "Quản lý người dùng";
    }
    if (view === "adminSecurity") {
      return "Quản trị bảo mật & khóa";
    }
    if (view === "doctorRecords") {
      return "Tra cứu hồ sơ";
    }
    if (view === "doctorPatients") {
      return "Tra cứu bệnh nhân";
    }
    if (view === "doctorCreate") {
      return "Thêm hồ sơ mới";
    }
    if (view === "doctorProfile") {
      return "Hồ sơ bác sĩ";
    }
    if (auth?.role === "PATIENT") {
      return "Bảng điều khiển bệnh nhân";
    }
    return "Bảng tổng quan bảo mật";
  }, [view, auth?.role]);

  if (!auth) {
    if (showRegister) {
      return (
        <RegisterPage
          onRegister={handleLogin}
          onBackToLogin={() => setShowRegister(false)}
        />
      );
    }
    return <LoginPage onLogin={handleLogin} onShowRegister={() => setShowRegister(true)} />;
  }

  return (
    <div className="app-shell">
      <SideNav active={view} onNavigate={setView} allowed={allowedViews} />
      <main className="main">
        <TopBar
          title={activeTitle}
          subtitle={
            auth?.role === "PATIENT" && patientCode
              ? `Mã bệnh nhân: ${patientCode}`
              : undefined
          }
          status={topStatus}
          onChangePassword={
            auth?.role === "PATIENT" || auth?.role === "DOCTOR"
              ? handleOpenPasswordModal
              : undefined
          }
          onLogout={handleLogout}
        />
        {view === "dashboard" ? (
          auth?.role === "PATIENT" ? (
            <PatientDashboard patientCode={patientCode || ""} />
          ) : (
            <DashboardPage timeline={timeline} />
          )
        ) : view === "adminUsers" ? (
          <AdminUserManagePage onStatusChange={setTopStatus} />
        ) : view === "adminSecurity" ? (
          <AdminSecurityPage onStatusChange={setTopStatus} />
        ) : view === "doctorCreate" ? (
          <DoctorRecordCreatePage onStatusChange={setTopStatus} />
        ) : view === "doctorRecords" ? (
          <DoctorRecordManagePage onStatusChange={setTopStatus} />
        ) : view === "doctorPatients" ? (
          <DoctorPatientSearchPage onStatusChange={setTopStatus} />
        ) : view === "doctorProfile" ? (
          <DoctorProfilePage />
        ) : view === "patientRecords" ? (
          <PatientRecordManagePage
            patientCode={patientCode}
            onStatusChange={setTopStatus}
          />
        ) : (
          <PatientPage patientCode={patientCode} />
        )}
      </main>
      {showPasswordModal ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal">
            <div className="modal-header">
              <div>
                <h3>Đổi mật khẩu</h3>
              </div>
            </div>
            <form
              className="form-grid"
              onSubmit={(e) => {
                e.preventDefault();
                handleChangePassword();
              }}
            >
              <label>
                Mật khẩu hiện tại
                <input
                  type="password"
                  value={passwordForm.currentPassword}
                  onChange={(e) =>
                    setPasswordForm({ ...passwordForm, currentPassword: e.target.value })
                  }
                />
              </label>
              <label>
                Mật khẩu mới
                <input
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={(e) =>
                    setPasswordForm({ ...passwordForm, newPassword: e.target.value })
                  }
                />
              </label>
              <label>
                Xác nhận mật khẩu
                <input
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(e) =>
                    setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })
                  }
                />
              </label>
              {passwordMismatch ? (
                <div className="muted-inline">Xác nhận mật khẩu không khớp.</div>
              ) : null}
              {passwordStatus ? <div className="status">{passwordStatus}</div> : null}
              <div className="modal-actions">
                <button type="button" className="ghost" onClick={handleClosePasswordModal}>
                  Đóng
                </button>
                <button className="primary" type="submit" disabled={!canSubmitPassword}>
                  Cập nhật mật khẩu
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  );
}

