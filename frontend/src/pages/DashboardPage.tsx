import StatCard from "../components/StatCard";
import Timeline from "../components/Timeline";
import type { MetadataRecord } from "../types";

type DashboardPageProps = {
  timeline: MetadataRecord[];
};

export default function DashboardPage({ timeline }: DashboardPageProps) {
  return (
    <div className="page">
      <div className="stats-grid">
        <StatCard label="Độ trễ tìm kiếm" value="45 ms" trend="-12% so với DB" />
        <StatCard label="Phiên hoạt động" value="18" trend="+4 hôm nay" />
        <StatCard label="Hồ sơ đã mã hóa" value="1.2k" trend="+38 tuần này" />
        <StatCard label="Kiểm tra toàn vẹn" value="99.9%" trend="Ổn định" />
      </div>
      <div className="panel">
        <div className="panel-head">
          <h2>Hoạt động gần đây</h2>
          <p className="subtle">Cập nhật metadata và tóm tắt mới nhất.</p>
        </div>
        <Timeline items={timeline} />
      </div>
    </div>
  );
}
