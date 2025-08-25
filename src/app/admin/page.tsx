"use client";

import { useState, useEffect, ReactNode } from "react";
import PeopleIcon from "@mui/icons-material/People";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import ReportProblemIcon from "@mui/icons-material/ReportProblem";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import ErrorIcon from "@mui/icons-material/Error";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import "./admin.css";

import { getNewUserCount, getActiveUserCount } from "@/lib/api/user";
import { fetchReports } from "@/lib/api/comment";
import { useWebSocketHealth } from "@/socketHooks/useWebSocketHealth";
import { checkApiHealth, checkDbHealth } from "@/lib/api/health";

export default function AdminMainPage() {
  const socketUrl = process.env.NEXT_PUBLIC_SOCKET_URL || "";
  const websocketHealthy = useWebSocketHealth(socketUrl);

  const [stats, setStats] = useState({
    newUsers: 0,
    activeUsers: 0,
    pendingReports: 0,
  });

  const [systemStatus, setSystemStatus] = useState({
    api: null as boolean | null,
    websocket: null as boolean | null,
    database: null as boolean | null,
  });

  const [currentTime, setCurrentTime] = useState(new Date());

  // 시계 업데이트
  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  // 통계 조회
  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [newUsersRes, activeUsersRes, waitingReports] = await Promise.all([
          getNewUserCount(),
          getActiveUserCount(),
          fetchReports(1, 1, "WAITING"),
        ]);

        setStats({
          newUsers: newUsersRes.success ? newUsersRes.userCount : 0,
          activeUsers: activeUsersRes.success ? activeUsersRes.userCount : 0,
          pendingReports: waitingReports.success ? waitingReports.totalCount : 0,
        });
      } catch (err) {
        console.error("관리자 통계 조회 실패:", err);
      }
    };

    fetchStats();
  }, []);

  // WebSocket 상태 반영
  useEffect(() => {
    setSystemStatus(prev => ({ ...prev, websocket: websocketHealthy }));
  }, [websocketHealthy]);

  // API / DB 상태 조회
  useEffect(() => {
    const fetchSystemHealth = async () => {
      try {
        const [apiOk, dbOk] = await Promise.all([checkApiHealth(), checkDbHealth()]);
        setSystemStatus(prev => ({
          ...prev,
          api: apiOk,
          database: dbOk,
        }));
      } catch (err) {
        console.error("시스템 상태 조회 실패:", err);
        setSystemStatus(prev => ({ ...prev, api: false, database: false }));
      }
    };

    fetchSystemHealth();
  }, []);

  return (
    <div className="admin-dashboard">
      {/* 상단 시계 */}
      <div className="clock-panel" style={{ marginBottom: 10 }}>
        <AccessTimeIcon style={{ marginRight: 6 }} />
        <span>{currentTime.toLocaleString()}</span>
      </div>

      {/* 상단 통계 카드 */}
      <div className="stats-grid">
        <StatCard icon={<PersonAddIcon />} label="신규 가입자" value={stats.newUsers} color="#28a745" />
        <StatCard icon={<PeopleIcon />} label="활성 유저" value={stats.activeUsers} color="#17a2b8" />
        <StatCard icon={<ReportProblemIcon />} label="미해결 신고" value={stats.pendingReports} color="#ffc107" />
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

function StatCard({ icon, label, value, color }: { icon: ReactNode; label: string; value: number; color: string; }) {
  return (
    <div
      className="stat-card card-hover"
      style={{
        borderTop: `5px solid ${color}`,
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
        borderRadius: "10px",
        transition: "all 0.2s ease",
        cursor: "pointer",
        backgroundColor: "#fff",
      }}
    >
      <div className="icon" style={{ color, fontSize: '2.5rem' }}>{icon}</div>
      <div className="stat-info">
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
      </div>
    </div>
  );
}

function StatusItem({ label, status }: { label: string; status: boolean | null }) {
  if (status === null)
    return <div className="status-item" style={{ color: "#666" }}>{label}: 확인 중...</div>;

  return (
    <div
      className="status-item"
      style={{
        display: "flex",
        alignItems: "center",
        padding: "6px 12px",
        margin: "4px 0",
        borderRadius: "6px",
        backgroundColor: status ? "#e6f4ea" : "#fcebea",
      }}
    >
      {status
        ? <CheckCircleIcon style={{ color: "#28a745", marginRight: '6px' }} />
        : <ErrorIcon style={{ color: "#dc3545", marginRight: '6px' }} />}
      <span>{label}</span>
    </div>
  );
}
