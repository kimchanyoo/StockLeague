"use client";

import {useState, useEffect} from "react";
import styles from "@/app/styles/components/StockSelector.module.css";
import FilterMenu from "./FilterMenu";
import SearchIcon from "@mui/icons-material/Search";
import MiniStockList from "./MiniStockList";
import { getTopStocks, Stock } from "@/lib/api/stock"; 

type Props = {
  onSelect: (stock: Stock) => void;
};


const StockSelector = ({onSelect}: Props) => {
  const [selectedFilter, setSelectedFilter] = useState('전체종목');
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStocks = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await getTopStocks();
        if (res.success) {
          setStocks(res.stocks);
        } else {
          setError("종목 리스트를 불러오는 데 실패했습니다.");
        }
      } catch (err) {
        setError("서버 요청 중 오류가 발생했습니다.");
      }
      setLoading(false);
    };

    fetchStocks();
  }, []);
  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;
  
  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.left}>
          <FilterMenu selected={selectedFilter} onChange={setSelectedFilter} />
        </div>
        <h1 className={styles.center}>{selectedFilter}</h1>
      </div>
      <div className={styles.search}>
        <div className={styles.searchBox}>
          <input/>
          <button>
            <SearchIcon style={{color: '#999999'}}/>
          </button>
        </div>
      </div>
      <div className={styles.miniStockList}>
        <MiniStockList stocks={stocks} onSelect={onSelect}/>
      </div>
    </div>
  );
};

export default StockSelector;