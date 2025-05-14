"use client";

import "./admin.css";
import Sidebar from "@/app/components/Sidebar";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  const router = useRouter();
  
  /*
  useEffect(() => {
    if (!user) router.push("/auth/login");
    else if (user.role !== "ADMIN") {
      alert("접근 권한이 없습니다.");
      router.push("/");
    }
  }, [user]);

  if (!user || user.role !== "ADMIN") return null;
  */
  return (
    <html lang="ko">
      <body>
        <div className="admin-layout">
          <div className="admin-container">
            <Sidebar /> {/* 사이드바 추가 */}
            <main className="admin-content">
              {children}
            </main>
          </div>
        </div>
      </body>
    </html>
  );
}
