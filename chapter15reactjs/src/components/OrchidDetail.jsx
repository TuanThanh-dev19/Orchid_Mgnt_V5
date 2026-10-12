import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getOrchid } from "../services/orchidApi";

export default function OrchidDetail() {
  const { id } = useParams();
  const [detailState, setDetailState] = useState(null);

  useEffect(() => {
    let active = true;
    if (id && /^\d+$/.test(id)) {
      getOrchid(id)
        .then(response => { if (active) setDetailState({ id, status: "loaded", orchid: response.data }); })
        .catch(error => { if (active) setDetailState({ id, status: "error", error: `Không thể tải chi tiết hoa lan. ${error.message}` }); });
    }
    return () => { active = false; };
  }, [id]);

  const invalidId = !id || !/^\d+$/.test(id);
  const currentState = detailState?.id === id ? detailState : null;
  const orchid = currentState?.status === "loaded" ? currentState.orchid : null;

  return (
    <section>
      <Link className="btn btn-outline-secondary mb-3" to="/">← Quay lại bộ sưu tập</Link>
      {invalidId ? (
        <div className="alert alert-danger" role="alert">Mã hoa lan không hợp lệ.</div>
      ) : !currentState ? (
        <p role="status">Đang tải chi tiết hoa lan…</p>
      ) : currentState.status === "error" ? (
        <div className="alert alert-danger" role="alert">{currentState.error}</div>
      ) : orchid ? (
        <article className="detail-card">
          <div className="detail-image">
            {orchid.orchidURL ? <img src={orchid.orchidURL} alt={orchid.orchidName} onError={e => { e.currentTarget.hidden = true; }} /> : <span>Chưa có hình ảnh</span>}
          </div>
          <div className="detail-content">
            <p className="text-secondary">CHI TIẾT HOA LAN · #{orchid.orchidId}</p>
            <h1>{orchid.orchidName}</h1>
            <p>{orchid.orchidCategory || "Chưa phân loại"}</p>
            <div className="d-flex gap-2 mb-4">
              <span className="badge text-bg-success">{orchid.natural ? "Tự nhiên" : "Nhân giống"}</span>
              <span className="badge text-bg-secondary">{orchid.attractive ? "Nổi bật" : "Thông thường"}</span>
            </div>
            <h2 className="h5">Mô tả</h2>
            <p className="detail-description">{orchid.orchidDescription || "Chưa có mô tả cho hoa lan này."}</p>
            <Link className="btn btn-warning" to={`/orchids/${orchid.orchidId}/edit`}>Edit</Link>
          </div>
        </article>
      ) : null}
    </section>
  );
}
