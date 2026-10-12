import { Routes, Route } from "react-router-dom";
import NavBar from "./components/NavBar";
import ListOfOrchids from "./components/ListOfOrchids";
import EditOrchid from "./components/EditOrchid";
import OrchidDetail from "./components/OrchidDetail";
import "./App.css";

export default function App() {
  return (
    <>
      <NavBar />
      <div className="container py-4">
        <Routes>
          <Route path="/" element={<ListOfOrchids />} />
          <Route path="/orchids/:id/edit" element={<EditOrchid key="edit" />} />
          <Route path="/orchids/new" element={<EditOrchid key="create" createMode />} />
          <Route path="/orchids/:id" element={<OrchidDetail />} />
          <Route path="*" element={<div className="alert alert-warning">Page not found.</div>} />
        </Routes>
      </div>
    </>
  );
}
