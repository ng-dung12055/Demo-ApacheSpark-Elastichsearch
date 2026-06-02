import { useEffect, useState } from "react";
import { getDoctorProfile, updateDoctorProfile } from "../api";
import type { DoctorProfile } from "../types";

const initials = (name: string) => {
  const parts = name.trim().split(/\s+/);
  if (parts.length === 0) {
    return "DR";
  }
  return parts
    .slice(0, 2)
    .map((p) => p[0])
    .join("")
    .toUpperCase();
};

type FormState = {
  fullName: string;
  gender: string;
  dob: string;
  specialty: string;
  title: string;
  licenseNumber: string;
  department: string;
  facility: string;
  shift: string;
  employmentStatus: string;
  internalPhone: string;
  workAddress: string;
  phone: string;
  email: string;
  avatarUrl: string;
};

export default function DoctorProfilePage() {
  const [profile, setProfile] = useState<DoctorProfile | null>(null);
  const [status, setStatus] = useState("");
  const [form, setForm] = useState<FormState>({
    fullName: "",
    gender: "",
    dob: "",
    specialty: "",
    title: "",
    licenseNumber: "",
    department: "",
    facility: "",
    shift: "",
    employmentStatus: "",
    internalPhone: "",
    workAddress: "",
    phone: "",
    email: "",
    avatarUrl: ""
  });

  useEffect(() => {
    getDoctorProfile()
      .then((data) => {
        setProfile(data);
        setForm({
          fullName: data.fullName || "",
          gender: data.gender || "",
          dob: data.dob || "",
          specialty: data.specialty || "",
          title: data.title || "",
          licenseNumber: data.licenseNumber || "",
          department: data.department || "",
          facility: data.facility || "",
          shift: data.shift || "",
          employmentStatus: data.employmentStatus || "",
          internalPhone: data.internalPhone || "",
          workAddress: data.workAddress || "",
          phone: data.phone || "",
          email: data.email || "",
          avatarUrl: data.avatarUrl || ""
        });
      })
      .catch(() => setStatus("Tải hồ sơ bác sĩ thất bại."));
  }, []);

  const handleSave = async () => {
    setStatus("Đang lưu hồ sơ...");
    try {
      const updated = await updateDoctorProfile(form);
      setProfile(updated);
      setStatus("Cập nhật hồ sơ thành công.");
    } catch {
      setStatus("Cập nhật thất bại.");
    }
  };

  const displayName = profile?.fullName || form.fullName || "Bác sĩ";
  const heroSpecialty = form.specialty || profile?.specialty || "Chuyên khoa";

  return (
    <div className="page">
      <div className="panel profile-hero">
        <div className="profile-hero-left">
          <div className="avatar">
            {form.avatarUrl ? (
              <img src={form.avatarUrl} alt="Ảnh đại diện" />
            ) : (
              <span>{initials(displayName)}</span>
            )}
          </div>
          <div>
            <h2>{displayName}</h2>
            <p className="subtle">{heroSpecialty}</p>
          </div>
        </div>
        <button className="primary" onClick={handleSave}>
          Lưu thay đổi
        </button>
      </div>

      <div className="panel profile-section">
        <h3>A. Thông tin định danh</h3>
        <div className="profile-form">
          <label>
            Ảnh đại diện (URL)
            <input
              value={form.avatarUrl}
              onChange={(e) => setForm({ ...form, avatarUrl: e.target.value })}
              placeholder="https://..."
            />
          </label>
          <label>
            Mã bác sĩ
            <input value={profile?.staffCode || "-"} readOnly />
          </label>
          <label>
            Họ và tên
            <input
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            />
          </label>
          <label>
            Giới tính
            <input
              value={form.gender}
              onChange={(e) => setForm({ ...form, gender: e.target.value })}
              placeholder="Nam / Nữ"
            />
          </label>
          <label>
            Ngày sinh (YYYY-MM-DD)
            <input
              value={form.dob}
              onChange={(e) => setForm({ ...form, dob: e.target.value })}
            />
          </label>
        </div>
      </div>

      <div className="panel profile-section">
        <h3>B. Thông tin chuyên môn</h3>
        <div className="profile-form">
          <label>
            Chuyên khoa
            <input
              value={form.specialty}
              onChange={(e) => setForm({ ...form, specialty: e.target.value })}
            />
          </label>
          <label>
            Chức danh
            <input
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
            />
          </label>
          <label>
            Số chứng chỉ hành nghề
            <input
              value={form.licenseNumber}
              onChange={(e) => setForm({ ...form, licenseNumber: e.target.value })}
            />
          </label>
        </div>
      </div>

      <div className="panel profile-section">
        <h3>C. Thông tin công tác</h3>
        <div className="profile-form">
          <label>
            Khoa / phòng ban
            <input
              value={form.department}
              onChange={(e) => setForm({ ...form, department: e.target.value })}
            />
          </label>
          <label>
            Cơ sở y tế
            <input
              value={form.facility}
              onChange={(e) => setForm({ ...form, facility: e.target.value })}
            />
          </label>
          <label>
            Ca làm việc
            <input
              value={form.shift}
              onChange={(e) => setForm({ ...form, shift: e.target.value })}
              placeholder="Sáng / Chiều / Trực"
            />
          </label>
          <label>
            Trạng thái làm việc
            <input
              value={form.employmentStatus}
              onChange={(e) => setForm({ ...form, employmentStatus: e.target.value })}
              placeholder="Đang công tác / Nghỉ phép"
            />
          </label>
        </div>
      </div>

      <div className="panel profile-section">
        <h3>D. Thông tin liên hệ</h3>
        <div className="profile-form">
          <label>
            Email công vụ
            <input
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </label>
          <label>
            Số điện thoại nội bộ
            <input
              value={form.internalPhone}
              onChange={(e) => setForm({ ...form, internalPhone: e.target.value })}
            />
          </label>
          <label>
            Địa chỉ làm việc
            <input
              value={form.workAddress}
              onChange={(e) => setForm({ ...form, workAddress: e.target.value })}
            />
          </label>
          <label>
            Số điện thoại
            <input
              value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
            />
          </label>
        </div>
      </div>

      <div className="panel profile-section">
        <h3>E. Tài khoản & bảo mật</h3>
        <div className="profile-form">
          <label>
            Tên đăng nhập
            <input value={profile?.username || "-"} readOnly />
          </label>
          <label>
            Vai trò
            <input value={profile?.role || "DOCTOR"} readOnly />
          </label>
          <label>
            Trạng thái tài khoản
            <input value={profile?.accountStatus || "ACTIVE"} readOnly />
          </label>
        </div>
      </div>

      {status ? <div className="status">{status}</div> : null}
    </div>
  );
}
