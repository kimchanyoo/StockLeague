"use client";

import {useState, useEffect} from "react";
import styles from "@/app/styles/components/StockSelector.module.css";
import FilterMenu from "./FilterMenu";
import SearchIcon from "@mui/icons-material/Search";
import MiniStockList from "./MiniStockList";
import { getTopStocks, Stock, getWatchlist } from "@/lib/api/stock"; 
import { useAuth } from "@/context/AuthContext"; 

type Props = {
  onSelect: (stock: Stock) => void;
};


const StockSelector = ({onSelect}: Props) => {
  const { user } = useAuth(); // 로그인 정보 가져오기
  const isLoggedIn = !!user;  // user가 있으면 로그인된 상태
  
  const [selectedFilter, setSelectedFilter] = useState('전체종목');
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const availableFilters = isLoggedIn ? ['전체종목', '인기종목', '관심종목'] : ['전체종목', '인기종목'];

  useEffect(() => {
    const fetchStocks = async () => {
      setLoading(true);
      setError(null);

      try {
        if (selectedFilter === "관심종목") {
          if (!isLoggedIn) {
            setError("로그인이 필요합니다.");
            setStocks([]);
            setLoading(false);
            return;
          }
          const watchlist = await getWatchlist();
          const converted: Stock[] = watchlist.watchlists.map((item) => ({
            stockId: item.stockId,
            stockTicker: item.StockTicker,
            stockName: item.StockName,
            marketType: "UNKNOWN", // 백엔드 응답에 없다면 임시값
            currentPrice: 0,
            prevPrice: 0,
            priceChange: 0,
          }));
          setStocks(converted);
        } else {
          const res = await getTopStocks();
          if (res.success) {
            setStocks(res.stocks);
          } else {
            setError("종목 리스트를 불러오는 데 실패했습니다.");
          }
        }
      } catch (err) {
        setError("서버 요청 중 오류가 발생했습니다.");
      }
      setLoading(false);
    };

    fetchStocks();
  }, [selectedFilter, isLoggedIn]);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;
  
  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.left}>
          <FilterMenu selected={selectedFilter} onChange={setSelectedFilter} options={availableFilters} />
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