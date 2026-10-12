import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { createOrchid, getOrchid, updateOrchid } from "../services/orchidApi";

const emptyForm = {
  orchidName: "", natural: false, orchidDescription: "",
  orchidCategory: "", attractive: false, orchidURL: ""
};

export default function EditOrchid({ createMode = false }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");
  const [loadedId, setLoadedId] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let active = true;
    if (!createMode && id) {
      getOrchid(id).then(r => {
        if (active) { setForm(r.data); setLoadedId(id); setError(""); }
      }).catch(e => { if (active) setError(e.message); });
    }
    return () => { active = false; };
  }, [createMode, id]);

  const change = (e) => {
    const { name, value, type, checked } = e.target;
    setForm(f => ({ ...f, [name]: type === "checkbox" ? checked : value }));
  };

  const submit = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError("");
      if (createMode) await createOrchid(form);
      else await updateOrchid(id, form);
      navigate("/");
    } catch (e) { setError(e.message); }
    finally { setSaving(false); }
  };

  return (
    <form className="orchid-form" onSubmit={submit}>
      <h2>{createMode ? "Create Orchid" : "Edit Orchid"}</h2>
      {error && <div className="alert alert-danger" role="alert">{error}</div>}
      {!createMode && loadedId !== id && !error && <p role="status">Loading...</p>}
      <fieldset disabled={saving || (!createMode && loadedId !== id)}>
        <label className="form-label" htmlFor="orchidName">Name</label>
        <input id="orchidName" className="form-control mb-2" name="orchidName" value={form.orchidName ?? ""} onChange={change} maxLength={150} required />
        <label className="form-label" htmlFor="orchidCategory">Category</label>
        <input id="orchidCategory" className="form-control mb-2" name="orchidCategory" value={form.orchidCategory ?? ""} onChange={change} maxLength={100} />
        <label className="form-label" htmlFor="orchidDescription">Description</label>
        <textarea id="orchidDescription" className="form-control mb-2" name="orchidDescription" value={form.orchidDescription ?? ""} onChange={change} maxLength={1000} />
        <label className="form-label" htmlFor="orchidURL">Image URL</label>
        <input id="orchidURL" className="form-control mb-2" name="orchidURL" value={form.orchidURL ?? ""} onChange={change} maxLength={1000} />
        <div className="form-check mb-2"><input id="natural" className="form-check-input" type="checkbox" name="natural" checked={!!form.natural} onChange={change} /><label className="form-check-label" htmlFor="natural">Natural</label></div>
        <div className="form-check mb-3"><input id="attractive" className="form-check-input" type="checkbox" name="attractive" checked={!!form.attractive} onChange={change} /><label className="form-check-label" htmlFor="attractive">Attractive</label></div>
        <button className="btn btn-success me-2" type="submit">{saving ? "Saving..." : "Save"}</button>
      </fieldset>
      <Link className="btn btn-outline-secondary mt-3" to="/">Back to list</Link>
    </form>
  );
}
