"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import styles from "@/app/styles/components/stock/StockItem.module.css";
import { useMainStockPriceSocket } from "@/socketHooks/useMainStockPriceSocket";
import { StockPriceResponse, getStockPrice } from "@/lib/api/stock";

type StockItemProps = {
  name: string;
  ticker: string;
};

const formatNumber = (num: number) => num.toLocaleString("ko-KR");
const formatChange = (num: number) =>
  num > 0 ? `+${num.toLocaleString("ko-KR")}` : num.toLocaleString("ko-KR");
const formatRate = (rate: number) =>
  rate > 0 ? `+${rate.toFixed(2)}%` : `${rate.toFixed(2)}%`;

export default function StockItem({ ticker, name }: StockItemProps) {
  const [stock, setStock] = useState<StockPriceResponse | null>(null);

  // 초기 데이터 로딩 + 실시간 구독 훅을 사용
  useEffect(() => {
    let isMounted = true;

    // 초기 시세 API 호출 (선택)
    const fetchInitialPrice = async () => {
      try {
        const data = await getStockPrice(ticker);
        if (isMounted) setStock(data);
      } catch (e) {
        console.error("초기 시세 조회 실패", e);
      }
    };

    fetchInitialPrice();

    return () => {
      isMounted = false;
    };
  }, [ticker]);

  useMainStockPriceSocket(ticker, (data) => {
    setStock(data);
  });

  if (!stock) {
    return <div>로딩중...</div>;
  }

  const {
    closePrice,
    priceChange,
    pricePercent,
    openPrice,
    highPrice,
    lowPrice,
    accumulatedVolume,
  } = stock;

  const changeClass =
    priceChange > 0
      ? styles.stock_up
      : priceChange < 0
      ? styles.stock_down
      : styles.stock_same;

  return (
    <Link href={{ pathname: "/stocks/trade"}}>
      <div className={styles.stock_item}>
        <div>{name}</div>
        <div>{formatNumber(closePrice)}</div>
        <div className={changeClass}>{formatChange(priceChange)}</div>
        <div className={changeClass}>{formatRate(pricePercent)}</div>
        <div>{formatNumber(openPrice)}</div>
        <div>{formatNumber(highPrice)}</div>
        <div>{formatNumber(lowPrice)}</div>
        <div>{formatNumber(accumulatedVolume)}</div>
      </div>
    </Link>
  );
}
