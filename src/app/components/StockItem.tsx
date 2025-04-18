"use client";

import React from "react";
import Link from "next/link";
import styles from "@/app/styles/components/StockItem.module.css";

type StockItemProps = {
  code: string;
  name: string;
  close: number;
  change: number;
  rate: number;
  open: number;
  high: number;
  low: number;
  volume: number;
  marketCap: number;
};

export default function StockItem({
  code,
  name,
  close,
  change,
  rate,
  open,
  high,
  low,
  volume,
  marketCap,
}: StockItemProps) {
  const changeClass =
    change > 0
      ? styles.stock_up
      : change < 0
      ? styles.stock_down
      : styles.stock_same;

  return (
    <Link
      href={{
        pathname: "/trade",
        query: { code, name },
      }}
    >
      <div className={styles.stock_item}>
        <div>{name}</div>
        <div>{close.toLocaleString()}</div>
        <div className={changeClass}>
          {change > 0 ? `+${change}` : change}
        </div>
        <div className={changeClass}>
          {rate > 0 ? `+${rate}%` : `${rate}%`}
        </div>
        <div>{open.toLocaleString()}</div>
        <div>{high.toLocaleString()}</div>
        <div>{low.toLocaleString()}</div>
        <div>{volume.toLocaleString()}</div>
        <div>{marketCap.toLocaleString()}</div>
      </div>
    </Link>
  );
}
