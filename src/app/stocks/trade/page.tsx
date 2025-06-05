"use client";

import { useState, useEffect } from "react";
import "./trade.css";
import StockOrder from "@/app/components/StockOrder";
import StockSelector from "@/app/components/StockSelector";
import StockChart from "@/app/components/StockChart";
import Community from "@/app/components/Community";
import { useAuth } from "@/context/AuthContext"; // 예시: 직접 만든 인증 Context
import { Stock, getWatchlist, addWatchlist, deleteWatchlist } from "@/lib/api/stock";

export default function Trade() {
  const { user } = useAuth(); // 로그인 정보 가져오기
  const isLoggedIn = !!user;  // user가 있으면 로그인된 상태

  const [activeTab, setActiveTab] = useState<"chart" | "community">("chart");
  const [isFavorite, setIsFavorite] = useState(false);
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);
  
  // 대비 계산
  const diff = selectedStock && selectedStock.currentPrice != null && selectedStock.prevPrice != null
    ? selectedStock.currentPrice - selectedStock.prevPrice
    : null;

  
  useEffect(() => {
    const checkIsFavorite = async () => {
      if (!selectedStock) return;

      try {
        const watchlist = await getWatchlist();
        const found = watchlist.find(item => item.StockTicker === selectedStock.stockTicker);
        setIsFavorite(!!found);
      } catch (err) {
        console.error("관심 종목 확인 중 오류:", err);
      }
    };

    checkIsFavorite();
  }, [selectedStock]);

      // ⭐ 관심 종목 토글 처리 함수
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
        const watchlist = await getWatchlist();
        const target = watchlist.find(item => item.StockTicker === selectedStock.stockTicker);
        if (target) {
          await deleteWatchlist(target.watchlistId);
          setIsFavorite(false);
          alert("관심 종목에서 제거되었습니다.");
        }
      }
    } catch (err) {
      console.error(err);
      alert("관심 종목 등록 중 오류가 발생했습니다.");
    }
  };

  useEffect(() => {
    setIsFavorite(false); // 새 종목 선택 시 초기화
  }, [selectedStock]);

  return (
    <div className="container">
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
            <span>{selectedStock?.currentPrice?.toLocaleString() ?? "종목가격"}</span>
            <h2>{selectedStock?.priceChange ?? "등락률"}</h2>
            <h2>{diff != null ? `${diff > 0 ? "+" : ""}${diff.toLocaleString()}원` : "대비"}</h2>
            <h1>거래량</h1>
            <h3>거래량 얼마</h3>
            <h1>거래대금</h1>
            <h3>거래대금 얼마</h3>
          </div>
        </div>
        {activeTab === "chart" ? (<StockChart activeTab={activeTab} setActiveTab={setActiveTab} />) : (<Community ticker={selectedStock?.stockTicker ?? ""}/>)}
      </div>
      
      <div className="stockOrder_section">
        {!isLoggedIn && (
          <div className="loginOverlay">
            <p>로그인이 필요합니다.</p>
            <a href="/auth/login" className="loginBtn">로그인하러 가기</a>
          </div>
        )}

        <div className={`${!isLoggedIn ? "blur" : ""}`}>
          <StockOrder stockName="삼성전자" currentPrice={75000} />
        </div>
      </div>
    </div>
  );
}