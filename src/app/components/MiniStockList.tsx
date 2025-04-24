"use client";

import styles from "@/app/styles/components/MiniStockList.module.css";

type StockType = {
  id: number;
  name: string;
  currentPrice: number;
  stockCode: string;
  priceChange: number;
};

type StockListProps = {
  stocks: StockType[];
};  

const MiniStockList = ({ stocks }:StockListProps) => {

  // 등락률에 따라 색상 변경 함수
  const getPriceChangeColor = (priceChange: number) => {
    if (priceChange > 0) return 'red'; // 상승: 빨간색
    if (priceChange < 0) return 'blue'; // 하락: 파란색
    return 'gray'; // 변화 없음: 회색
  };

  return (
    <div className={styles.stockList}>
      {stocks.map((stock) => (
        <div key={stock.id} className={styles.stockCard}>
          <div className={styles.row}>
            <span className={styles.name}>{stock.name}</span>
            <span className={`${styles.price} ${styles[getPriceChangeColor(stock.priceChange)]}`}>
              {stock.currentPrice.toLocaleString()} 원
            </span>
          </div>
          <div className={styles.row}>
            <span className={styles.code}>{stock.stockCode}</span>
            <span
              className={`${styles.change} ${styles[getPriceChangeColor(stock.priceChange)]}`}
            >
              {stock.priceChange > 0 ? `+${stock.priceChange}%` : `${stock.priceChange}%`}
            </span>
          </div>
        </div>
      ))}
    </div>
  );
};

export default MiniStockList;