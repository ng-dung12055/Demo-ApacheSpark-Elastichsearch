import { useEffect, useState } from "react";
import {
  createAdminUser,
  deleteAdminUser,
  getAdminRoles,
  getAdminUsers,
  setAdminUserActive,
  updateAdminUserRole
} from "../api";
import type { AdminRole, AdminUser } from "../types";

type AdminUserManagePageProps = {
  onStatusChange?: (status: string) => void;
};

const emptyForm = {
  username: "",
  password: "",
  fullName: "",
  role: "",
  email: "",
  phone: ""
};

export default function AdminUserManagePage({ onStatusChange }: AdminUserManagePageProps) {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [roles, setRoles] = useState<AdminRole[]>([]);
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [roleTarget, setRoleTarget] = useState<AdminUser | null>(null);
  const [roleValue, setRoleValue] = useState("");
  const [showRoleModal, setShowRoleModal] = useState(false);

  useEffect(() => {
    onStatusChange?.(status);
  }, [status, onStatusChange]);

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const [roleData, userData] = await Promise.all([getAdminRoles(), getAdminUsers()]);
        setRoles(roleData);
        setUsers(userData);
        setForm((prev) => ({
          ...prev,
          role: prev.role || roleData[0]?.name || ""
        }));
        setStatus("");
      } catch (err) {
        setStatus(
          err instanceof Error ? err.message : "Không thể tải dữ liệu quản trị."
        );
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  const refreshUsers = async () => {
    try {
      const data = await getAdminUsers();
      setUsers(data);
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Không thể tải danh sách tài khoản.");
    }
  };

  const handleCreateUser = async () => {
    if (
      !form.username.trim() ||
      !form.password.trim() ||
      !form.fullName.trim() ||
      !form.role.trim()
    ) {
      setStatus("Vui lòng nhập đầy đủ tên đăng nhập, mật khẩu, họ tên và vai trò.");
      return;
    }
    setActionLoading(true);
    setStatus("Đang tạo tài khoản...");
    try {
      await createAdminUser({
        username: form.username.trim(),
        password: form.password.trim(),
        fullName: form.fullName.trim(),
        role: form.role.trim(),
        email: form.email.trim() || undefined,
        phone: form.phone.trim() || undefined
      });
      setStatus("Tạo tài khoản thành công.");
      setForm((prev) => ({
        ...emptyForm,
        role: prev.role || roles[0]?.name || ""
      }));
      await refreshUsers();
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Tạo tài khoản thất bại.");
    } finally {
      setActionLoading(false);
    }
  };

  const openRoleModal = (user: AdminUser) => {
    const currentRole = user.roles[0] || roles[0]?.name || "";
    setRoleTarget(user);
    setRoleValue(currentRole);
    setShowRoleModal(true);
  };

  const handleUpdateRole = async () => {
    if (!roleTarget || !roleValue.trim()) {
      setStatus("Vui lòng chọn vai trò hợp lệ.");
      return;
    }
    setActionLoading(true);
    setStatus("Đang cập nhật vai trò...");
    try {
      await updateAdminUserRole(roleTarget.accountId, roleValue.trim());
      setStatus("Cập nhật vai trò thành công.");
      setShowRoleModal(false);
      setRoleTarget(null);
      setRoleValue("");
      await refreshUsers();
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Cập nhật vai trò thất bại.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleToggleActive = async (user: AdminUser) => {
    const nextActive = !user.active;
    const actionLabel = nextActive ? "mở khóa" : "khóa";
    if (!window.confirm(`Bạn có chắc muốn ${actionLabel} tài khoản ${user.username}?`)) {
      setStatus(`Đã hủy ${actionLabel} tài khoản.`);
      return;
    }
    setActionLoading(true);
    setStatus(`Đang ${actionLabel} tài khoản...`);
    try {
      await setAdminUserActive(user.accountId, nextActive);
      setStatus(`${actionLabel[0].toUpperCase()}${actionLabel.slice(1)} tài khoản thành công.`);
      await refreshUsers();
    } catch (err) {
      setStatus(err instanceof Error ? err.message : `${actionLabel} tài khoản thất bại.`);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteUser = async (user: AdminUser) => {
    if (!window.confirm(`Bạn có chắc muốn xóa tài khoản ${user.username}?`)) {
      setStatus("Đã hủy xóa tài khoản.");
      return;
    }
    setActionLoading(true);
    setStatus("Đang xóa tài khoản...");
    try {
      await deleteAdminUser(user.accountId);
      setStatus("Xóa tài khoản thành công.");
      await refreshUsers();
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Xóa tài khoản thất bại.");
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="page">
      <section className="panel manage-section">
        <div className="section-head">
          <div>
            <h3>Quản lý người dùng &amp; phân quyền</h3>
            <p className="subtle">
              Cấp tài khoản, phân quyền và khóa hoặc mở khóa nhân viên y tế.
            </p>
          </div>
          <span className="chip">Tài khoản</span>
        </div>

        <div className="form-grid">
          <label>
            Tên đăng nhập
            <input
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              placeholder="doctor01"
            />
          </label>
          <label>
            Mật khẩu
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="••••••••"
            />
          </label>
          <label>
            Họ và tên
            <input
              value={form.fullName}
              onChange={(e) => setForm({ ...form, fullName: e.target.value })}
              placeholder="Nguyễn Văn A"
            />
          </label>
          <label>
            Vai trò
            <select
              className="select"
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}
              disabled={roles.length === 0}
            >
              {roles.length === 0 ? (
                <option value="">Chưa có vai trò</option>
              ) : (
                roles.map((role) => (
                  <option key={role.name} value={role.name}>
                    {role.name}
                  </option>
                ))
              )}
            </select>
          </label>
          <label>
            Email
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="doctor@hospital.local"
            />
          </label>
          <label>
            Số điện thoại
            <input
              value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
              placeholder="0901..."
            />
          </label>
          <div className="action-row">
            <button
              className="primary"
              type="button"
              onClick={handleCreateUser}
              disabled={actionLoading || loading}
            >
              Tạo tài khoản
            </button>
            {status ? <div className="status">{status}</div> : null}
          </div>
        </div>
      </section>

      <section className="panel manage-section">
        <div className="section-head">
          <div>
            <h3>Danh sách tài khoản</h3>
            <p className="subtle">Theo dõi vai trò và trạng thái sử dụng.</p>
          </div>
          <span className="chip">Quản trị</span>
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Tên đăng nhập</th>
                <th>Họ tên</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Liên hệ</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan={7} className="empty">
                    {loading ? "Đang tải..." : "Chưa có tài khoản."}
                  </td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.accountId}>
                    <td>#{user.accountId}</td>
                    <td>{user.username}</td>
                    <td>{user.fullName || "-"}</td>
                    <td>
                      {user.roles.length === 0 ? (
                        <span className="muted-inline">-</span>
                      ) : (
                        <div className="tag-list">
                          {user.roles.map((role) => (
                            <span key={`${user.accountId}-${role}`} className="tag">
                              {role}
                            </span>
                          ))}
                        </div>
                      )}
                    </td>
                    <td>
                      <span className={user.active ? "badge success" : "badge neutral"}>
                        {user.active ? "Đang hoạt động" : "Đã khóa"}
                      </span>
                    </td>
                    <td>
                      <div>{user.email || "-"}</div>
                      <div className="muted-inline">{user.phone || "-"}</div>
                    </td>
                    <td>
                      <button
                        className="icon-btn"
                        type="button"
                        onClick={() => openRoleModal(user)}
                        disabled={actionLoading}
                      >
                        Phân quyền
                      </button>
                      {" "}
                      <button
                        className="icon-btn"
                        type="button"
                        onClick={() => handleToggleActive(user)}
                        disabled={actionLoading}
                      >
                        {user.active ? "Khóa" : "Mở khóa"}
                      </button>
                      {" "}
                      <button
                        className="icon-btn"
                        type="button"
                        onClick={() => handleDeleteUser(user)}
                        disabled={actionLoading}
                      >
                        Xóa
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      {showRoleModal && roleTarget ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal">
            <div className="modal-header">
              <div>
                <h3>Phân quyền tài khoản</h3>
                <p className="subtle">
                  Chọn vai trò mới cho {roleTarget.username}.
                </p>
              </div>
            </div>
            <div className="modal-section">
              <div className="section-title">Thông tin tài khoản</div>
              <div className="meta-grid">
                <div>
                  <div className="detail-label">Họ tên</div>
                  <div className="detail-value">{roleTarget.fullName || "-"}</div>
                </div>
                <div>
                  <div className="detail-label">Trạng thái</div>
                  <div className="detail-value">
                    {roleTarget.active ? "Đang hoạt động" : "Đã khóa"}
                  </div>
                </div>
              </div>
            </div>
            <form
              className="form-grid"
              onSubmit={(e) => {
                e.preventDefault();
                handleUpdateRole();
              }}
            >
              <label>
                Vai trò
                <select
                  className="select"
                  value={roleValue}
                  onChange={(e) => setRoleValue(e.target.value)}
                >
                  {roles.length === 0 ? (
                    <option value="">Chưa có vai trò</option>
                  ) : (
                    roles.map((role) => (
                      <option key={role.name} value={role.name}>
                        {role.name}
                      </option>
                    ))
                  )}
                </select>
              </label>
              <div className="modal-actions">
                <button
                  type="button"
                  className="ghost"
                  onClick={() => {
                    setShowRoleModal(false);
                    setRoleTarget(null);
                    setRoleValue("");
                  }}
                >
                  Đóng
                </button>
                <button className="primary" type="submit" disabled={actionLoading}>
                  Cập nhật vai trò
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  );
}
