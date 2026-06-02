import type { MetadataRecord } from "../types";

type TimelineProps = {
  items: MetadataRecord[];
};

export default function Timeline({ items }: TimelineProps) {
  return (
    <div className="timeline">
      {items.length === 0 ? (
        <div className="timeline-empty">Chưa có mục trong dòng thời gian.</div>
      ) : (
        items.map((item) => (
          <div key={item.id} className="timeline-item">
            <div className="timeline-dot" />
            <div className="timeline-content">
              <div className="timeline-title">{item.summary || "Cập nhật hồ sơ"}</div>
              <div className="timeline-meta">
                {item.patientCode} - {new Date(item.updatedAt).toLocaleString()}
              </div>
              {item.keywords ? <div className="timeline-tags">{item.keywords}</div> : null}
            </div>
          </div>
        ))
      )}
    </div>
  );
}
