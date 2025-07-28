"use client";

import { useState, useEffect } from "react";
import "./trade.css";
import StockOrder from "@/app/components/stock/StockOrder";
import StockSelector from "@/app/components/stock/StockSelector";
import StockChart from "@/app/components/stock/StockChart";
import Community from "@/app/components/stock/Community";
import { useAuth } from "@/context/AuthContext";
import { Stock, getWatchlist, addWatchlist, deleteWatchlist, StockPriceResponse } from "@/lib/api/stock";
import { useStockPriceMultiSocket } from "@/hooks/useStockPriceMultiSocket";

export default function Trade() {
  const { user, accessToken } = useAuth(); // 로그인 정보 가져오기
  const isLoggedIn = !!user;  // user가 있으면 로그인된 상태

  const [activeTab, setActiveTab] = useState<"chart" | "community">("chart");
  const [isFavorite, setIsFavorite] = useState(false);
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);
  const [realTimeData, setRealTimeData] = useState<StockPriceResponse | null>(null);

  const [currentPrice, setCurrentPrice] = useState<number>(0);
  
  // 대비 계산
  const diff = realTimeData
    ? realTimeData.currentPrice - realTimeData.openPrice
    : null;

  useStockPriceMultiSocket(
    selectedStock ? [selectedStock.stockTicker] : [],
    (data) => {
      setRealTimeData(data);
    },
    accessToken ?? ""
  );

  
  // 관심종목 상태 초기화와 체크 통합 예시
  useEffect(() => {
    if (!selectedStock) {
      setIsFavorite(false);
      return;
    }
    const checkIsFavorite = async () => {
      try {
        const { watchlists } = await getWatchlist();
        const found = watchlists.find(item => item.StockTicker === selectedStock.stockTicker);
        setIsFavorite(!!found);
      } catch (err) {
        console.error("관심 종목 확인 중 오류:", err);
        setIsFavorite(false); // 오류 시 안전하게 false 처리
      }
    };
    checkIsFavorite();
  }, [selectedStock]);

  // 관심종목 토글 예시: 상태 일관성 유지 위해 try-catch 내 상태 변경 유지
  const handleFavoriteToggle = async () => {
    if (!selectedStock) {
      alert("선택된 종목이 없습니다.");
      return;
    }

    try {
      if (!isFavorite) {
        await addWatchlist(selectedStock.stockTicker);
        setIsFavorite(true);
        alert("관심 종목에 추가되었습니다.");
      } else {
        const { watchlists } = await getWatchlist();
        const target = watchlists.find(item => item.StockTicker === selectedStock.stockTicker);
        if (target) {
          await deleteWatchlist(target.watchlistId);
          setIsFavorite(false);
          alert("관심 종목에서 제거되었습니다.");
        }
      }
    } catch (err) {
      console.error(err);
      alert("관심 종목 등록 중 오류가 발생했습니다.");
      // 상태 변경 없이 유지
    }
  };

  useEffect(() => {
    setIsFavorite(false); // 새 종목 선택 시 초기화
  }, [selectedStock]);

  return (
    <div className="trade_container">
      <div className="stockList_section">
        <StockSelector onSelect={setSelectedStock}/>
      </div>
      
      <div className="stockChart_section">
        <div className="topSection">
          <div className="chartTitle">
            <label>{selectedStock?.stockName ?? "종목 선택"}</label>
            <div className="btnGroup">
              <button className="bigBtn" onClick={() => setActiveTab('chart')}>차트</button>
              <button className="bigBtn" onClick={() => setActiveTab('community')}>커뮤니티</button>
              {isLoggedIn && (
                <button className={`smallBtn ${isFavorite ? "active" : ''}`} onClick={handleFavoriteToggle}>★</button>
              )}
            </div>
          </div>
          <div className="titleCantent">
            <h1>{selectedStock?.stockTicker ?? "종목번호"}</h1>
            <h1>{selectedStock?.marketType ?? "KOSPI"}</h1>
            <span>{realTimeData?.currentPrice?.toLocaleString() ?? "종목가격"}</span>
            <h2 className={realTimeData?.changeSign === 1 ? "up" : realTimeData?.changeSign === 2 ? "down" : "neutral"}>
              {realTimeData?.changeSign === 1 ? "+" : realTimeData?.changeSign === 2 ? "-" : ""}
              {realTimeData?.pricePercent.toFixed(2)}%
            </h2>
            <h2>{diff != null ? `${diff > 0 ? "+" : ""}${diff.toLocaleString()}원` : "대비"}</h2>
            <h1>거래량</h1>
            <h3>{realTimeData?.accumulatedVolume?.toLocaleString() ?? "거래량 정보 없음"}</h3>
            <h1>거래대금</h1>
            <h3>
              {realTimeData && realTimeData.accumulatedVolume && realTimeData.currentPrice
                ? (realTimeData.accumulatedVolume * realTimeData.currentPrice).toLocaleString()
                : "거래대금 정보 없음"}
            </h3>
          </div>
        </div>
        {activeTab === "chart" ? (<StockChart activeTab={activeTab} setActiveTab={setActiveTab} ticker={selectedStock?.stockTicker ?? ""} onCurrentPriceChange={setCurrentPrice}/>) : (<Community ticker={selectedStock?.stockTicker ?? ""}/>)}
      </div>
      
      <div className="stockOrder_section">
        {!isLoggedIn && (
          <div className="loginOverlay">
            <p>로그인이 필요합니다.</p>
            <a href="/auth/login" className="loginBtn">로그인하러 가기</a>
          </div>
        )}

        <div className={`${!isLoggedIn ? "blur" : ""}`}>
          <StockOrder
            stockName={selectedStock?.stockName ?? ""}
            currentPrice={currentPrice}
            ticker={selectedStock?.stockTicker ?? ""}
          />
        </div>
      </div>
    </div>
  );
}