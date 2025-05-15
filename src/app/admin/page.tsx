"use client";

import { useState } from "react";
import AccountIcon from "@mui/icons-material/AccountCircle";
import DeleteIcon from "@mui/icons-material/Delete";
import AddIcon from "@mui/icons-material/Add";
import "./admin.css";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

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

const initialUsers = [
  { id: 1, name: "홍길동", joinedAt: "2025-05-10" },
  { id: 2, name: "김철수", joinedAt: "2025-05-11" },
  { id: 3, name: "이영희", joinedAt: "2025-05-12" },
];

export default function Admin() {
  const [admins, setAdmins] = useState(initialAdmins);
  const [users] = useState(initialUsers);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newAdminName, setNewAdminName] = useState("");

  const handleAddAdminClick = () => {
    setIsModalOpen(true);
    setNewAdminName("");
  };

  const handleAddAdmin = () => {
    const trimmedName = newAdminName.trim();
    if (!trimmedName) {
      alert("닉네임을 입력해주세요.");
      return;
    }
    const newId = Date.now();
    setAdmins([...admins, { id: newId, name: trimmedName }]);
    setIsModalOpen(false);
  };

  const handleDelete = (id: number) => {
    if (confirm("관리자를 삭제하시겠습니까?")) {
      setAdmins(admins.filter((admin) => admin.id !== id));
    }
  };

  return (
    <div className="dashboard-container">
      {/* 방문자 수, 관리자 관리 */}
      <div className="top-section">
        <div className="visitor-section">
          <h1>방문자 현황</h1>
          <ResponsiveContainer width="100%" height="85%">
            <LineChart
              data={data}
              margin={{ top: 20, right: 30, left: 0, bottom: 0 }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Line
                type="monotone"
                dataKey="visitors"
                stroke="#006ADD"
                strokeWidth={2}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="admin-section">
          <div className="add-top">
            관리자 설정
            <button className="add-admin-btn" onClick={handleAddAdminClick}>
              <AddIcon fontSize="small" />
            </button>
          </div>
          <div className="admin-list">
            {admins.map((admin) => (
              <div key={admin.id} className="admin-card">
                <AccountIcon style={{ fontSize: 30, color: "#666" }} />
                <div className="admin-name">{admin.name}</div>
                <button
                  className="delete-btn"
                  onClick={() => handleDelete(admin.id)}
                >
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
        <div className="user-list">
          {users.map((user) => (
            <div key={user.id} className="user-card">
              <AccountIcon style={{ fontSize: 28, color: "#444" }} />
              <div className="user-info">
                <div className="user-name">{user.name}</div>
                <div className="user-joined">가입일: {user.joinedAt}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 모달 */}
      {isModalOpen && (
        <div className="modal-backdrop">
          <div className="modal">
            <h2>관리자 추가</h2>
            <input
              type="text"
              value={newAdminName}
              onChange={(e) => setNewAdminName(e.target.value)}
              placeholder="닉네임을 입력하세요"
              autoFocus
            />
            <div className="modal-buttons">
              <button onClick={handleAddAdmin}>추가</button>
              <button onClick={() => setIsModalOpen(false)}>취소</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
