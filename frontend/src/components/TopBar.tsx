type TopBarProps = {
  title: string;
  subtitle?: string;
  onLogout?: () => void;
  onChangePassword?: () => void;
  status?: string;
};

export default function TopBar({
  title,
  subtitle,
  onLogout,
  onChangePassword,
  status
}: TopBarProps) {
  return (
    <div className="topbar">
      <div>
        <h1>{title}</h1>
        {subtitle ? <p className="subtle">{subtitle}</p> : null}
      </div>
      <div className="topbar-actions">
        {status ? <div className="topbar-status">{status}</div> : null}
        {onChangePassword || onLogout ? (
          <div className="topbar-buttons">
            {onChangePassword ? (
              <button className="ghost small" type="button" onClick={onChangePassword}>
                Đổi mật khẩu
              </button>
            ) : null}
            {onLogout ? (
              <button className="ghost" type="button" onClick={onLogout}>
                Đăng xuất
              </button>
            ) : null}
          </div>
        ) : null}
      </div>
    </div>
  );
}
