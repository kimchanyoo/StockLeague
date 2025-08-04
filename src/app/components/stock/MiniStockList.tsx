import { Stock, StockPriceResponse } from "@/lib/api/stock";
import styles from "@/app/styles/components/stock/MiniStockList.module.css";

type Props = {
  stocks: Stock[];
  stockPricesMap: Map<string, StockPriceResponse>;
  onSelect: (stock: Stock) => void;
  lastElementRef?: (node: HTMLDivElement | null) => void;
};

const MiniStockList = ({ stocks, stockPricesMap, onSelect, lastElementRef }: Props) => {
  const getPriceChangeColor = (priceChange: number) => {
    if (priceChange > 0) return 'red';
    if (priceChange < 0) return 'blue';
    return 'gray';
  };

  return (
    <div className={styles.stockList}>
      {stocks.map((stock, index) => {
        // 각 stock에 해당하는 실시간 가격 데이터 찾기
        const priceInfo = stockPricesMap.get(stock.stockTicker);
        const isLast = index === stocks.length - 1;

        return (
          <div 
            key={stock.stockId} 
            ref={isLast ? lastElementRef ?? null : null} // 마지막 카드에만 ref 연결
            className={styles.stockCard} 
            onClick={() => onSelect(stock)}
          >
            <div className={styles.row}>
              <span className={styles.name}>{stock.stockName}</span>
              <span className={`${styles.price} ${styles[getPriceChangeColor(priceInfo?.priceChange ?? 0)]}`}>
                {(priceInfo?.currentPrice ?? 0).toLocaleString()} 원
              </span>
            </div>
            <div className={styles.row}>
              <span className={styles.code}>{stock.stockTicker}</span>
              <span className={`${styles.change} ${styles[getPriceChangeColor(priceInfo?.priceChange ?? 0)]}`}>
                {(priceInfo?.priceChange ?? 0) > 0
                  ? `+${priceInfo?.pricePercent.toFixed(2)}%`
                  : `${priceInfo?.pricePercent?.toFixed(2) ?? "0.00"}%`}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default MiniStockList;
