"use client";

import "./admin.css";
import Sidebar from "@/app/components/Sidebar";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  const router = useRouter();
  
  useEffect(() => {
    // 아직 user 정보가 로드되지 않은 상태는 무시
    if (user === undefined) return;

    // 로그인 안 된 경우
    if (!user) {
      router.replace("/auth/login");
      return;
    }

    // 일반 유저인 경우
    if (user.role !== "ADMIN") {
      alert("접근 권한이 없습니다.");
      router.push("/");
    }
  }, [user, router]);

  // 아직 user 정보 로딩 중
  if (user === undefined) return null;

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
