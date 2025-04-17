"use client";

import "@/app/user/user.css";

export default function UserLayout({ children }: { children: React.ReactNode }) {
    return (
      <div className="user-layout">
        <main>{children}</main>
      </div>
    );
}