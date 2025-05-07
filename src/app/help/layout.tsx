"use client";

import "./help.css";

export default function HelpLayout({ children }: { children: React.ReactNode }) {
    return (
      <div className="help-layout">
        <main>{children}</main>
      </div>
    );
}