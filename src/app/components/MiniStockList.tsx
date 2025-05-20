"use client";

import { Stock } from "@/lib/api/stock";
import styles from "@/app/styles/components/MiniStockList.module.css";

type Props = {
  stocks: Stock[];
  onSelect: (stock: Stock) => void;
};

const MiniStockList = ({ stocks, onSelect }:Props) => {

  // 등락률에 따라 색상 변경 함수
  const getPriceChangeColor = (priceChange: number) => {
    if (priceChange > 0) return 'red'; // 상승: 빨간색
    if (priceChange < 0) return 'blue'; // 하락: 파란색
    return 'gray'; // 변화 없음: 회색
  };

  return (
    <div className={styles.stockList}>
      {stocks.map((stock) => (
        <div key={stock.stockId} className={styles.stockCard} onClick={() => onSelect(stock)}>
          <div className={styles.row}>
            <span className={styles.name}>{stock.stockName}</span>
            <span className={`${styles.price} ${styles[getPriceChangeColor(stock.priceChange ?? 0)]}`}>
              {(stock.currentPrice ?? 0).toLocaleString()} 원
            </span>
          </div>
          <div className={styles.row}>
            <span className={styles.code}>{stock.stockTicker}</span>
            <span className={`${styles.change} ${styles[getPriceChangeColor(stock.priceChange ?? 0)]}`}>
              {stock.priceChange && stock.priceChange > 0 ? `+${stock.priceChange}%` : `${stock.priceChange ?? 0}%`}
            </span>
          </div>
        </div>
      ))}
    </div>
  );
};

export default MiniStockList;