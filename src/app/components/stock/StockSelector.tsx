"use client";

import { useState, useEffect, useMemo } from "react";
import styles from "@/app/styles/components/stock/StockSelector.module.css";
import FilterMenu from "./FilterMenu";
import SearchIcon from "@mui/icons-material/Search";
import MiniStockList from "./MiniStockList";
import { getTopStocks, Stock, getWatchlist, StockPriceResponse } from "@/lib/api/stock"; 
import { useStockPriceMultiSocket } from "@/hooks/useStockPriceMultiSocket";
import { useAuth } from "@/context/AuthContext";

type Props = {
  onSelect: (stock: Stock) => void;
};

const StockSelector = ({ onSelect }: Props) => {
  const { user, accessToken } = useAuth();
  const isLoggedIn = !!user;

  const [selectedFilter, setSelectedFilter] = useState('전체종목');
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [stockPrices, setStockPrices] = useState<StockPriceResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const availableFilters = isLoggedIn
    ? ['전체종목', '인기종목', '관심종목']
    : ['전체종목', '인기종목'];

  // stockPrices 배열을 Map 형태로 변환 (ticker -> StockPriceResponse)
  const stockPriceMap = useMemo(() => {
    return new Map(stockPrices.map(p => [p.ticker, p]));
  }, [stockPrices]);

  // WebSocket 구독 (accessToken 없으면 구독 안함)
  useStockPriceMultiSocket(
    stocks.map(s => s.stockTicker),
    (data) => {
      setStockPrices(prev => {
        const exists = prev.find(p => p.ticker === data.ticker);
        if (exists) {
          return prev.map(p => (p.ticker === data.ticker ? data : p));
        }
        return [...prev, data];
      });

      // stocks에는 가격정보를 넣지 않고 고정 정보만 유지
    },
    accessToken ?? ""
  );

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
            marketType: "UNKNOWN",
            // 가격 관련 필드 제거, stocks는 가격 정보를 가지지 않음
          }));
          setStocks(converted);
        } else {
          const res = await getTopStocks();
          if (res.success) {
            // 가격 관련 필드 제거 또는 무시하고 종목 정보만 유지
            const converted = res.stocks.map(s => ({
              stockId: s.stockId,
              stockTicker: s.stockTicker,
              stockName: s.stockName,
              marketType: s.marketType,
            }));
            setStocks(converted);
          } else {
            setError("종목 리스트를 불러오는 데 실패했습니다.");
            setStocks([]);
          }
        }
      } catch {
        setError("서버 요청 중 오류가 발생했습니다.");
        setStocks([]);
      } finally {
        setLoading(false);
      }
    };

    fetchStocks();
  }, [selectedFilter, isLoggedIn]);

  // 검색어로 필터링된 종목 목록
  const filteredStocks = useMemo(() => {
    if (!searchTerm) return stocks;
    return stocks.filter(s =>
      s.stockName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.stockTicker.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [stocks, searchTerm]);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div className={styles.error}>{error}</div>;

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
          <input
            type="text"
            placeholder="종목명 또는 코드 검색"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button>
            <SearchIcon style={{ color: '#999999' }} />
          </button>
        </div>
      </div>
      <div className={styles.miniStockList}>
        <MiniStockList
          stocks={filteredStocks}
          stockPricesMap={stockPriceMap}  // Map 형태로 전달
          onSelect={onSelect}
        />
      </div>
    </div>
  );
};

export default StockSelector;
