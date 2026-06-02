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

type SideNavProps = {
  active: View;
  onNavigate: (view: View) => void;
  allowed: View[];
};

export default function SideNav({ active, onNavigate, allowed }: SideNavProps) {
  return (
    <aside className="sidenav">
      <div className="brand">
        <span className="brand-mark">HS</span>
        <div>
          <div className="brand-title">Bệnh viện</div>
        </div>
      </div>
      <nav>
        {allowed.includes("dashboard") ? (
          <button
            className={active === "dashboard" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("dashboard")}
          >
            Tổng quan
          </button>
        ) : null}
        {allowed.includes("adminUsers") ? (
          <button
            className={active === "adminUsers" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("adminUsers")}
          >
            Quản lý người dùng
          </button>
        ) : null}
        {allowed.includes("adminSecurity") ? (
          <button
            className={active === "adminSecurity" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("adminSecurity")}
          >
            Quản trị bảo mật &amp; khóa
          </button>
        ) : null}
        {allowed.includes("doctorCreate") ? (
          <button
            className={active === "doctorCreate" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("doctorCreate")}
          >
            Thêm hồ sơ mới
          </button>
        ) : null}
        {allowed.includes("doctorPatients") ? (
          <button
            className={active === "doctorPatients" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("doctorPatients")}
          >
            Tra cứu bệnh nhân
          </button>
        ) : null}
        {allowed.includes("doctorRecords") ? (
          <button
            className={active === "doctorRecords" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("doctorRecords")}
          >
            Tra cứu hồ sơ
          </button>
        ) : null}
        {allowed.includes("doctorProfile") ? (
          <button
            className={active === "doctorProfile" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("doctorProfile")}
          >
            Hồ sơ bác sĩ
          </button>
        ) : null}
        {allowed.includes("patient") ? (
          <button
            className={active === "patient" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("patient")}
          >
            Cổng bệnh nhân
          </button>
        ) : null}
        {allowed.includes("patientRecords") ? (
          <button
            className={active === "patientRecords" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("patientRecords")}
          >
            Hồ sơ của tôi
          </button>
        ) : null}
      </nav>
      <div className="nav-footer">
        <div className="pill">JWT + RBAC</div>
        <div className="pill">AES-GCM</div>
      </div>
    </aside>
  );
}
