"use client";

import "./stocks.css";

export default function StocksLayout({ children }: { children: React.ReactNode }) {
    return (
      <div className="stocks-layout">
        <main>{children}</main>
      </div>
    );
}