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

// 숫자를 'ko-KR' 형식으로 포맷
const formatNumber = (num: number) => num.toLocaleString("ko-KR");

// 상승/하락에 따라 + 또는 그대로 표시
const formatChange = (num: number) =>
  num > 0 ? `+${num.toLocaleString("ko-KR")}` : num.toLocaleString("ko-KR");

// 비율 % 형식 처리
const formatRate = (rate: number) =>
  rate > 0 ? `+${rate.toFixed(2)}%` : `${rate.toFixed(2)}%`;

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
    <Link href={{pathname: "/trade", query: { code, name },}}>
      <div className={styles.stock_item}>
        <div>{name}</div>
        <div>{formatNumber(close)}</div>
        <div className={changeClass}>{formatChange(change)}</div>
        <div className={changeClass}>{formatRate(rate)}</div>
        <div>{formatNumber(open)}</div>
        <div>{formatNumber(high)}</div>
        <div>{formatNumber(low)}</div>
        <div>{formatNumber(volume)}</div>
        <div>{formatNumber(marketCap)}</div>
      </div>
    </Link>
  );
}
