"use client";

import { useState, useEffect, ReactNode } from "react";
import PeopleIcon from "@mui/icons-material/People";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import VisibilityIcon from "@mui/icons-material/Visibility";
import ReportProblemIcon from "@mui/icons-material/ReportProblem";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import ErrorIcon from "@mui/icons-material/Error";
import "./admin.css";

export default function AdminMainPage() {
  const [stats, setStats] = useState({
    visitorsToday: 0,
    newUsers: 0,
    activeUsers: 0,
    pendingReports: 0,
  });

  const [recentLogs, setRecentLogs] = useState<string[]>([]);
  const [systemStatus, setSystemStatus] = useState({
    api: true,
    websocket: true,
    database: true,
  });

  useEffect(() => {
    // 🚀 여기에 API 연동 로직 작성
    setStats({
      visitorsToday: 152,
      newUsers: 12,
      activeUsers: 87,
      pendingReports: 3,
    });
    setRecentLogs([
      "[11:02] 홍길동 회원가입",
      "[11:15] 김철수 게시물 등록",
      "[11:30] 신고 접수 #2025",
      "[12:00] 관리자1 로그인",
    ]);
  }, []);

  return (
    <div className="admin-dashboard">
      {/* 상단 통계 카드 */}
      <div className="stats-grid">
        <StatCard icon={<VisibilityIcon />} label="오늘 방문자" value={stats.visitorsToday} color="#007bff" />
        <StatCard icon={<PersonAddIcon />} label="신규 가입자" value={stats.newUsers} color="#28a745" />
        <StatCard icon={<PeopleIcon />} label="활성 유저" value={stats.activeUsers} color="#17a2b8" />
        <StatCard icon={<ReportProblemIcon />} label="미해결 신고" value={stats.pendingReports} color="#ffc107" />
      </div>

      {/* 최근 활동 */}
      <div className="panel">
        <h2>최근 활동 로그</h2>
        <ul className="log-list">
          {recentLogs.map((log, idx) => (
            <li key={idx}>{log}</li>
          ))}
        </ul>
      </div>

      {/* 시스템 상태 */}
      <div className="panel">
        <h2>시스템 상태</h2>
        <div className="status-list">
          <StatusItem label="API 서버" status={systemStatus.api} />
          <StatusItem label="WebSocket" status={systemStatus.websocket} />
          <StatusItem label="데이터베이스" status={systemStatus.database} />
        </div>
      </div>
    </div>
  );
}

function StatCard({
  icon,
  label,
  value,
  color,
}: {
  icon: ReactNode;
  label: string;
  value: number;
  color: string;
}) {
  return (
    <div className="stat-card" style={{ borderTop: `4px solid ${color}` }}>
      <div className="icon" style={{ color }}>{icon}</div>
      <div className="stat-info">
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
      </div>
    </div>
  );
}

function StatusItem({ label, status }: { label: string; status: boolean }) {
  return (
    <div className="status-item">
      {status ? <CheckCircleIcon style={{ color: "#28a745" }} /> : <ErrorIcon style={{ color: "#dc3545" }} />}
      <span>{label}</span>
    </div>
  );
}
