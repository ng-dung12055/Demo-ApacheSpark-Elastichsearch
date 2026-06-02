import { useEffect, useMemo, useState } from "react";
import { getPatientHistoryPage } from "../api";
import StatCard from "../components/StatCard";
import type { MedicalRecord } from "../types";

type PatientDashboardProps = {
  patientCode: string;
};

type PatientHistoryCache = {
  items: MedicalRecord[];
  total?: number;
};

export default function PatientDashboard({ patientCode }: PatientDashboardProps) {
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
  const [totalRecords, setTotalRecords] = useState<number>(() => {
    const cached = readCache(cacheKey);
    return typeof cached?.total === "number" ? cached.total : 0;
  });
  const [status, setStatus] = useState("");

  useEffect(() => {
    const cached = readCache(cacheKey);
    if (cached) {
      setHistory(cached.items ?? []);
      setTotalRecords(typeof cached.total === "number" ? cached.total : cached.items.length);
    }
    getPatientHistoryPage(0, 50, "COMPLETED")
      .then((pageResult) => {
        const items = pageResult.items ?? [];
        const total =
          typeof pageResult.total === "number" ? pageResult.total : items.length;
        setHistory(items);
        setTotalRecords(total);
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

  const lastUpdated = useMemo(() => {
    if (completedHistory.length === 0) {
      return "-";
    }
    const sorted = [...completedHistory].sort(
      (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    );
    return new Date(sorted[0].updatedAt).toLocaleString();
  }, [completedHistory]);

  return (
    <div className="page">
      <div className="panel patient-hero">
        <div>
          <p className="eyebrow">Bảng điều khiển bệnh nhân</p>
          <h2>Chào mừng quay lại</h2>
          <p className="subtle">Theo dõi hồ sơ và dòng thời gian khám.</p>
        </div>
        <div className="patient-code">
          Mã bệnh nhân
          <span>{patientCode || "Không có"}</span>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard label="Tổng số hồ sơ" value={`${totalRecords}`} trend="Đã mã hóa" />
        <StatCard label="Cập nhật gần nhất" value={lastUpdated} trend="Chỉ metadata" />
        <StatCard label="Mức truy cập" value="Bệnh nhân" trend="RBAC" />
        <StatCard label="Bảo mật" value="AES-GCM" trend="HMAC toàn vẹn" />
      </div>

      <div className="panel">
        <div className="panel-head">
          <h2>Tóm tắt gần đây</h2>
          <p className="subtle">Tóm tắt lần khám mới nhất dành cho bạn.</p>
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
        {status ? <div className="status">{status}</div> : null}
      </div>
    </div>
  );
}
