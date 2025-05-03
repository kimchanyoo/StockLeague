"use client";

import "./admin.css";
import Sidebar from "@/app/components/Sidebar";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
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
