"use client";

import { useState, useEffect, useMemo, useCallback, useRef } from "react";
import styles from "@/app/styles/components/stock/StockSelector.module.css";
import FilterMenu from "./FilterMenu";
import SearchIcon from "@mui/icons-material/Search";
import MiniStockList from "./MiniStockList";
import { getTopStocks, Stock, getWatchlist, StockPriceResponse, getPopularStocks } from "@/lib/api/stock"; 
import { useStockPriceMultiSocket } from "@/socketHooks/useStockPriceMultiSocket";
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

  const [visibleCount, setVisibleCount] = useState(20);
  const observer = useRef<IntersectionObserver | null>(null);

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
          }));
          setStocks(converted);
          if (converted.length > 0) onSelect(converted[0]);
        } else if (selectedFilter === "인기종목") {
          const res = await getPopularStocks(1, 200); // page 1, size 100 (필요시 조절 가능)
          if (res.success) {
            const converted = res.stocks.map((s) => ({
              stockId: s.stockId,
              stockTicker: s.stockTicker,
              stockName: s.stockName,
              marketType: s.marketType,
            }));
            setStocks(converted);
            // 첫 번째 종목 자동 선택
            if (converted.length > 0) { onSelect(converted[0]); }
          } else {
            setError("인기 종목을 불러오는 데 실패했습니다.");
            setStocks([]);
          }
        } else {
          const res = await getTopStocks(1, 200);
          if (res.success) {
            const converted = res.stocks.map((s) => ({
              stockId: s.stockId,
              stockTicker: s.stockTicker,
              stockName: s.stockName,
              marketType: s.marketType,
            }));
            setStocks(converted);
            if (converted.length > 0) onSelect(converted[0]);
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

  // 보여줄 종목 (스크롤 갯수 제한 반영)
  const visibleStocks = useMemo(() => {
    return filteredStocks.slice(0, visibleCount);
  }, [filteredStocks, visibleCount]);

  const loadMoreRef = useCallback((node: HTMLDivElement | null) => {
    if (observer.current) observer.current.disconnect();

    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting) {
        setVisibleCount(prev => {
          // 조건 추가: 더 이상 보여줄 게 없으면 증가시키지 않음
          if (prev >= filteredStocks.length) return prev;
          return prev + 20;
        });
      }
    });

    if (node) observer.current.observe(node);
  }, [filteredStocks.length]);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div className={styles.error}>{error}</div>;

  return (
    <div className={styles.stockSelector_container}>
      <div className={styles.stockSelector_header}>
        <div className={styles.stockSelector_left}>
          <FilterMenu selected={selectedFilter} onChange={setSelectedFilter} options={availableFilters} />
        </div>
        <h1 className={styles.stockSelector_center}>{selectedFilter}</h1>
      </div>
      <div className={styles.stockSelector_search}>
        <div className={styles.stockSelector_searchBox}>
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
      <div className={styles.stockSelector_miniStockList}>
        <MiniStockList
          stocks={visibleStocks}
          stockPricesMap={stockPriceMap}  // Map 형태로 전달
          onSelect={onSelect}
          lastElementRef={loadMoreRef}
        />
      </div>
    </div>
  );
};

export default StockSelector;
