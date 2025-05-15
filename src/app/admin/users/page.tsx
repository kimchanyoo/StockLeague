"use client";

import "./users.css";
import { useState } from "react";

interface User {
  id: number;
  nickname: string;
  joinedAt: string;
  isDeactivated?: boolean;
  isSuspended?: boolean;
  suspensionUntil?: string | null;
}

export default function Users() {
  const [users, setUsers] = useState<User[]>([
    {
      id: 1,
      nickname: "홍길동",
      joinedAt: "2025-05-01",
    },
    {
      id: 2,
      nickname: "김철수",
      joinedAt: "2025-04-15",
    },
  ]);

  const suspendUser = (id: number, days: number) => {
    const until = days === -1 ? "무기한" : new Date(Date.now() + days * 24 * 60 * 60 * 1000).toISOString().split("T")[0];
    const updated = users.map(user =>
      user.id === id ? { ...user, isSuspended: true, suspensionUntil: until } : user
    );
    setUsers(updated);
    console.log(`사용자 ${id} 정지 (${until})`);
  };

  const unsuspendUser = (id: number) => {
    const updated = users.map(user =>
      user.id === id ? { ...user, isSuspended: false, suspensionUntil: null } : user
    );
    setUsers(updated);
    console.log(`사용자 ${id} 정지 해제`);
  };

  const handleDeactivate = (id: number) => {
    const updated = users.map(user =>
      user.id === id ? { ...user, isDeactivated: true } : user
    );
    setUsers(updated);
    console.log(`사용자 ${id} 강제 탈퇴`);
  };

  return (
    <div className="users-container">
      <div className="users-list">
        <h1>회원 목록</h1>
        {users.map(user => (
          <div
            key={user.id}
            className={`users-item ${user.isDeactivated ? "deactivated" : ""}`}
          >
            <div className="nickname">{user.nickname}</div>
            <div className="joinedAt">가입일자: {user.joinedAt}</div>
            <div className="status">
              {user.isSuspended ? (
                <span className="suspended">⛔ 정지됨 (until {user.suspensionUntil})</span>
              ) : (
                <span className="active">✅ 활동중</span>
              )}
            </div>
            <div className="user-actions">
              {!user.isDeactivated && (
                <>
                  {!user.isSuspended ? (
                    <>
                      <select
                        onChange={(e) => {
                          const days = parseInt(e.target.value);
                          if (!isNaN(days)) suspendUser(user.id, days);
                        }}
                        defaultValue=""
                      >
                        <option value="" disabled>정지 기간 선택</option>
                        <option value={3}>3일</option>
                        <option value={7}>7일</option>
                        <option value={30}>30일</option>
                        <option value={-1}>영구정지</option>
                      </select>
                    </>
                  ) : (
                    <button onClick={() => unsuspendUser(user.id)}>정지 해제</button>
                  )}
                  <button onClick={() => handleDeactivate(user.id)}>강제 탈퇴</button>
                </>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
