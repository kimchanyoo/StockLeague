"use client";

import "@/app/user/user.css";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function UserLayout({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !user) {
      router.replace("/auth/login");
    }
  }, [loading, user, router]);
  
      // 아직 user 정보 로딩 중
  if (user === undefined) return null;

  return (
    <div className="user-layout">
      <main>{children}</main>
    </div>
  );
}