"use client";

import Header from "@/app/components/Header";
import AdminHeader from "@/app/components/admin/AdminHeader";
import Footer from "@/app/components/Footer";
import { AuthProvider } from "@/context/AuthContext";
import { NotificationProvider } from "@/context/NotificationContext";
import { usePathname } from "next/navigation";

export default function ClientLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isAdminPage = pathname.startsWith("/admin");

  return (
    <AuthProvider>
      <NotificationProvider>
        <div className="layout">
          {isAdminPage ? <AdminHeader /> : <Header />}
          <main className="content">{children}</main>
          <Footer />
        </div>
      </NotificationProvider>
    </AuthProvider>
  );
}
