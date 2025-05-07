"use client";

import Header from "@/app/components/Header";
import Footer from "@/app/components/Footer";
import "@/app/styles/globals.css";
import { AuthProvider } from "@/context/AuthContext"; // AuthProvider를 임포트

export default function RootLayout({children,}: Readonly<{children: React.ReactNode;}>) {

  return (
    <html lang="ko">
      <body>
        <AuthProvider>
          <div className="layout">
            <Header />
            <main className="content">{children}</main>
            <Footer />
          </div>
        </AuthProvider>
      </body>
    </html>
  );
}
