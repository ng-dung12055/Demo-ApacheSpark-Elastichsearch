type StatCardProps = {
  label: string;
  value: string;
  trend?: string;
};

export default function StatCard({ label, value, trend }: StatCardProps) {
  return (
    <div className="stat-card">
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value}</div>
      {trend ? <div className="stat-trend">{trend}</div> : null}
    </div>
  );
}
