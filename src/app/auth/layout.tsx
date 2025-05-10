"use client";

import "@/app/auth/auth.css";
import { SocialSignupProvider } from "@/context/SocialSignupContext";

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <SocialSignupProvider>
      <div className="auth-layout">
        <main className="auth-content">{children}</main>
      </div>
    </SocialSignupProvider>
  );
}
