"use client";

import { useState } from "react";
import AccountIcon from "@mui/icons-material/AccountCircle";
import DeleteIcon from "@mui/icons-material/Delete";
import AddIcon from "@mui/icons-material/Add";
import "./admin.css";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";

// 더미 데이터
const data = [
  { date: "04-28", visitors: 12 },
  { date: "04-29", visitors: 98 },
  { date: "04-30", visitors: 150 },
  { date: "05-01", visitors: 130 },
  { date: "05-02", visitors: 180 },
  { date: "05-03", visitors: 160 },
  { date: "05-04", visitors: 200 },
];
const initialAdmins = [
  { id: 1, name: "관리자1" },
  { id: 2, name: "관리자2" },
  { id: 3, name: "관리자3" },
  { id: 4, name: "관리자4" },
  { id: 5, name: "관리자5" },
];

export default function Admin() {

  const [admins, setAdmins] = useState(initialAdmins);

  const handleAddAdmin = () => {
    const newId = Date.now();
    const newName = `관리자${admins.length + 1}`;
    setAdmins([...admins, { id: newId, name: newName }]);
  };

  const handleDelete = (id: number) => {
    setAdmins(admins.filter((admin) => admin.id !== id));
  };

  return (
    <div className="dashboard-container">
      
      {/* 방문자 수, 관리자 관리 */}
      <div className="top-section">
        <div className="visitor-section">
          <h1>방문자 현황</h1>
          <ResponsiveContainer width="100%" height="85%">
            <LineChart data={data} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Line type="monotone" dataKey="visitors" stroke="#006ADD" strokeWidth={2} activeDot={{ r: 6 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="admin-section">
          <div className="add-top">
            관리자 설정
            <button className="add-admin-btn" onClick={handleAddAdmin}>
              <AddIcon fontSize="small" />
            </button>
          </div>
          <div className="admin-list">
          {admins.map((admin) => (
            <div key={admin.id} className="admin-card">
              <AccountIcon style={{ fontSize: 30, color: "#666" }} />
              <div className="admin-name">{admin.name}</div>
              <button className="delete-btn" onClick={() => handleDelete(admin.id)}>
                <DeleteIcon />
              </button>
            </div>
          ))}
        </div>
        </div>
      </div>

      {/* 새 회원 */}
      <div className="bottom-section">
        <h1>새 회원</h1>
      </div>    
    </div>
  );
}