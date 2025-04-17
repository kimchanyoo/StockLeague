"use client";

import React, {useState} from "react";
import "./trade.css";
import StockOrder from "@/app/components/StockOrder";
import StockSelector from "@/app/components/StockSelector";
import StockChart from "@/app/components/StockChart";
import Community from "@/app/components/Community";

export default function Trade() {
  const isLoggedIn = true; // 나중에 실제 로그인 여부로 변경

  const [activeTab, setActiveTab] = useState<"chart" | "community">("chart");
  const [isFavorite, setIsFavorite] = useState(false);

  return (
    <div className="container">
      <div className="stockList_section">
        <StockSelector/>
      </div>
      
      <div className="stockChart_section">
        <div className="topSection">
          <div className="chartTitle">
            <label>여기 종목 이름</label>
            <div className="btnGroup">
              <button className="bigBtn" onClick={() => setActiveTab('chart')}>차트</button>
              <button className="bigBtn" onClick={() => setActiveTab('community')}>커뮤니티</button>
              <button className={`smallBtn ${isFavorite ? "active" : ''}`} onClick={() => setIsFavorite((prev) => !prev)}>★</button>
            </div>
          </div>
          <div className="titleCantent">
            <h1>종목번호</h1>
            <h1>KOSPI</h1>
            <span>종목가격</span>
            <h2>등락률</h2>
            <h2>대비</h2>
            <h1>거래량</h1>
            <h3>거래량 얼마</h3>
            <h1>거래대금</h1>
            <h3>거래대금 얼마</h3>
          </div>
        </div>
        {activeTab === "chart" ? (<StockChart activeTab={activeTab} setActiveTab={setActiveTab} />) : (<Community/>)}
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