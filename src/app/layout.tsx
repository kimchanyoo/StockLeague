"use client";

import Header from "@/app/components/Header";
import AdminHeader from "@/app/components/AdminHeader";
import Footer from "@/app/components/Footer";
import "@/app/styles/globals.css";
import { AuthProvider } from "@/context/AuthContext"; // AuthProvider를 임포트
import { usePathname } from "next/navigation";

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const pathname = usePathname();
  const isAdminPage = pathname.startsWith("/admin");

  return (
    <html lang="ko">
      <body>
        <AuthProvider>
          <div className="layout">
            {isAdminPage ? <AdminHeader /> : <Header />}
            <main className="content">{children}</main>
            <Footer />
          </div>
        </AuthProvider>
      </body>
    </html>
  );
}
