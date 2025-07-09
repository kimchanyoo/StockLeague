"use client";

import "./admin.css";
import Sidebar from "@/app/components/admin/Sidebar";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();
  
  useEffect(() => {
    if (!loading) {
      if (!user) {
        // 로그인 안 됨
        router.replace("/auth/login");
        return;
      }
      if (user.role !== "ADMIN") {
        alert("접근 권한이 없습니다.");
        router.push("/");
      }
    }
  }, [user, loading, router]);

  if (loading) {
    return <div style={{textAlign: "center"}}>로딩 중...</div>;
  }

  // 유저는 존재하지만 ADMIN이 아닌 경우에도 리턴 null (잠깐 깜빡임 방지용)
  if (!user || user.role !== "ADMIN") return null;

  return (
    <div className="admin-layout">
      <div className="admin-container">
        <Sidebar /> {/* 사이드바 추가 */}
        <main className="admin-content">
          {children}
        </main>
      </div>
    </div>
  );
}
