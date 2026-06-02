import { useEffect, useMemo, useState } from "react";
import { getPatientHistoryPage, getPatientProfile, updatePatientProfile } from "../api";
import type { MedicalRecord, PatientProfile } from "../types";

type PatientPageProps = {
  patientCode: string | null;
};

type PatientHistoryCache = {
  items: MedicalRecord[];
  total?: number;
};

export default function PatientPage({ patientCode }: PatientPageProps) {
  const [profile, setProfile] = useState<PatientProfile | null>(null);
  const cacheKey = `patient-history-completed-${patientCode || "unknown"}`;
  const readCache = (key: string): PatientHistoryCache | null => {
    if (typeof window === "undefined") {
      return null;
    }
    try {
      const raw = sessionStorage.getItem(key);
      if (!raw) {
        return null;
      }
      const parsed = JSON.parse(raw) as { items?: MedicalRecord[]; total?: number };
      if (!parsed || !Array.isArray(parsed.items)) {
        return null;
      }
      return { items: parsed.items, total: parsed.total };
    } catch {
      return null;
    }
  };
  const [history, setHistory] = useState<MedicalRecord[]>(() => {
    const cached = readCache(cacheKey);
    return cached?.items ?? [];
  });
  const [completedTotal, setCompletedTotal] = useState<number>(() => {
    const cached = readCache(cacheKey);
    return typeof cached?.total === "number" ? cached.total : 0;
  });
  const [status, setStatus] = useState("");
  const [editForm, setEditForm] = useState({
    fullName: "",
    cccd: "",
    phone: "",
    email: "",
    gender: "",
    dob: "",
    address: ""
  });
  const [showEditModal, setShowEditModal] = useState(false);

  useEffect(() => {
    getPatientProfile()
      .then((data) => {
        setProfile(data);
        setEditForm({
          fullName: data.fullName || "",
          cccd: data.cccd || "",
          phone: data.phone || "",
          email: data.email || "",
          gender: data.gender || "",
          dob: data.dob || "",
          address: data.address || ""
        });
      })
      .catch(() => setStatus("Tải hồ sơ thất bại."));

    const cached = readCache(cacheKey);
    if (cached) {
      setHistory(cached.items ?? []);
      setCompletedTotal(typeof cached.total === "number" ? cached.total : cached.items.length);
    }
    getPatientHistoryPage(0, 50, "COMPLETED")
      .then((pageResult) => {
        const items = pageResult.items ?? [];
        const total =
          typeof pageResult.total === "number" ? pageResult.total : items.length;
        setHistory(items);
        setCompletedTotal(total);
        if (typeof window !== "undefined") {
          sessionStorage.setItem(
            cacheKey,
            JSON.stringify({ items, total, savedAt: Date.now() })
          );
        }
      })
      .catch(() => setStatus("Tải lịch sử thất bại."));
  }, [cacheKey]);

  const completedHistory = useMemo(() => {
    const normalize = (value: unknown) => String(value ?? "").trim().toUpperCase();
    return history.filter((item) => normalize(item.status) === "COMPLETED");
  }, [history]);
  const lastUpdate = useMemo(() => {
    if (completedHistory.length === 0) {
      return "-";
    }
    const sorted = [...completedHistory].sort(
      (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    );
    return new Date(sorted[0].updatedAt).toLocaleString();
  }, [completedHistory]);
  const recordStatus = completedTotal > 0 ? "Đã hoàn tất" : "Chưa có hồ sơ hoàn tất";
  const profileStatus = profile ? "Đã xác minh" : "Chưa xác minh";
  const displayedPatientCode = profile?.patientCode || patientCode || "-";

  const handleUpdateProfile = async () => {
    setStatus("Đang cập nhật hồ sơ...");
    try {
      const payload: Record<string, string> = {};
      if (editForm.fullName.trim()) {
        payload.fullName = editForm.fullName.trim();
      }
      if (editForm.cccd.trim()) {
        payload.cccd = editForm.cccd.trim();
      }
      if (editForm.phone.trim()) {
        payload.phone = editForm.phone.trim();
      }
      if (editForm.email.trim()) {
        payload.email = editForm.email.trim();
      }
      if (editForm.gender.trim()) {
        payload.gender = editForm.gender.trim();
      }
      if (editForm.dob.trim()) {
        payload.dob = editForm.dob.trim();
      }
      if (editForm.address.trim()) {
        payload.address = editForm.address.trim();
      }
      const updated = await updatePatientProfile(payload);
      setProfile(updated);
      setEditForm({
        fullName: updated.fullName || "",
        cccd: updated.cccd || "",
        phone: updated.phone || "",
        email: updated.email || "",
        gender: updated.gender || "",
        dob: updated.dob || "",
        address: updated.address || ""
      });
      setShowEditModal(false);
      setStatus("Cập nhật hồ sơ thành công.");
    } catch {
      setStatus("Cập nhật thất bại.");
    }
  };

  const handleOpenEditModal = () => {
    setEditForm({
      fullName: profile?.fullName || "",
      cccd: profile?.cccd || "",
      phone: profile?.phone || "",
      email: profile?.email || "",
      gender: profile?.gender || "",
      dob: profile?.dob || "",
      address: profile?.address || ""
    });
    setShowEditModal(true);
  };

  const handleCloseEditModal = () => {
    setShowEditModal(false);
    setEditForm({
      fullName: profile?.fullName || "",
      cccd: profile?.cccd || "",
      phone: profile?.phone || "",
      email: profile?.email || "",
      gender: profile?.gender || "",
      dob: profile?.dob || "",
      address: profile?.address || ""
    });
  };

  return (
    <div className="page">
      <section className="panel patient-hero">
        <div>
          <p className="eyebrow">Cổng bệnh nhân</p>
          <h2>Thông tin bệnh nhân</h2>
          <p className="subtle">Tổng quan hồ sơ và thông tin định danh.</p>
        </div>
        <div className="patient-code">
          Mã bệnh nhân
          <span>{displayedPatientCode}</span>
        </div>
      </section>

      <section className="panel info-grid">
        <div className="info-card">
          <div className="detail-label">Số lượng hồ sơ</div>
          <div className="detail-value">{completedTotal}</div>
        </div>
        <div className="info-card">
          <div className="detail-label">Cập nhật gần nhất</div>
          <div className="detail-value">{lastUpdate}</div>
        </div>
        <div className="info-card">
          <div className="detail-label">Trạng thái</div>
          <div className="detail-value">{recordStatus}</div>
        </div>
        <div className="info-card">
          <div className="detail-label">Hồ sơ</div>
          <div className="detail-value">{profileStatus}</div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-head">
          <div>
            <h3>Thông tin bệnh nhân</h3>
            <p className="subtle">Thông tin cơ bản, không bao gồm dữ liệu bệnh.</p>
          </div>
          <button
            className="ghost"
            type="button"
            onClick={handleOpenEditModal}
            disabled={!profile}
          >
            Chỉnh sửa
          </button>
        </div>
        <div className="detail-grid">
          <div>
            <div className="detail-label">Họ và tên</div>
            <div className="detail-value">{profile?.fullName || "-"}</div>
          </div>
          <div>
            <div className="detail-label">CCCD</div>
            <div className="detail-value">{profile?.cccd || "Chưa cập nhật"}</div>
          </div>
          <div>
            <div className="detail-label">Số điện thoại</div>
            <div className="detail-value">{profile?.phone || "-"}</div>
          </div>
          <div>
            <div className="detail-label">Email</div>
            <div className="detail-value">{profile?.email || "-"}</div>
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
      </section>

      <div className="panel">
        <div className="panel-head">
          <h2>Lịch sử khám</h2>
          <p className="subtle">Các lần khám gần nhất và tóm tắt metadata.</p>
        </div>
        <div className="history-list">
          {completedHistory.length === 0 ? (
            <div className="empty">Chưa có hồ sơ hoàn tất.</div>
          ) : (
            completedHistory.map((item) => (
              <div key={item.id} className="history-card">
                <div className="history-title">
                  Hồ sơ #{item.id} - {item.status}
                </div>
                <div className="history-meta">
                  {new Date(item.updatedAt).toLocaleString()}
                </div>
                <div className="history-summary">
                  {item.metadata?.summary || "Chưa có tóm tắt."}
                </div>
                <div className="history-tags">
                  {item.metadata?.keywords || "Chưa có từ khóa"}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {showEditModal ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal">
            <div className="modal-header">
              <div>
                <h3>Chỉnh sửa thông tin bệnh nhân</h3>
                <p className="subtle">Cập nhật thông tin cơ bản và lưu hồ sơ.</p>
              </div>
            </div>
            <form
              className="form-grid"
              onSubmit={(e) => {
                e.preventDefault();
                handleUpdateProfile();
              }}
            >
              <label>
                Họ và tên
                <input
                  value={editForm.fullName}
                  onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                  placeholder="Nhập họ và tên"
                />
              </label>
              <label>
                CCCD
                <input
                  value={editForm.cccd}
                  onChange={(e) => setEditForm({ ...editForm, cccd: e.target.value })}
                  placeholder="Nhập CCCD"
                />
              </label>
              <label>
                Số điện thoại
                <input
                  value={editForm.phone}
                  onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
                  placeholder="Nhập số điện thoại"
                />
              </label>
              <label>
                Email
                <input
                  type="email"
                  value={editForm.email}
                  onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                  placeholder="Nhập email"
                />
              </label>
              <label>
                Giới tính
                <input
                  value={editForm.gender}
                  onChange={(e) => setEditForm({ ...editForm, gender: e.target.value })}
                  placeholder="Nam / Nữ"
                />
              </label>
              <label>
                Ngày sinh
                <input
                  type="date"
                  value={editForm.dob}
                  onChange={(e) => setEditForm({ ...editForm, dob: e.target.value })}
                />
              </label>
              <label>
                Địa chỉ
                <input
                  value={editForm.address}
                  onChange={(e) => setEditForm({ ...editForm, address: e.target.value })}
                  placeholder="Nhập địa chỉ"
                />
              </label>
              <div className="modal-actions">
                <button type="button" className="ghost" onClick={handleCloseEditModal}>
                  Đóng
                </button>
                <button className="primary" type="submit">
                  Lưu hồ sơ
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {status ? <div className="status">{status}</div> : null}
    </div>
  );
}
