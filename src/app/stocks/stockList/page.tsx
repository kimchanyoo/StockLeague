"use client";

import { useState } from "react";
import TabMenu from "@/app/components/TabMenu";
import "./stockList.css";
import StockItem from "@/app/components/StockItem";
import DownIcon from '@mui/icons-material/ArrowDropDown';

// 예시용 더미 데이터 생성
const dummyStockData = Array.from({ length: 100 }, (_, i) => ({
  code: `STK${i + 1}`,
  name: `종목 ${i + 1}`,
  close: 10000 + i * 10,
  change: parseFloat((Math.random() * 20 - 10).toFixed(2)),
  rate: parseFloat((Math.random() * 4 - 2).toFixed(2)),
  open: 10000 + i * 8,
  high: 10000 + i * 12,
  low: 10000 + i * 6,
  volume: 1000000 + i * 1000,
  marketCap: 500000000 + i * 500000,
}));

export default function StockList() {

  const [activeTab, setActiveTab] = useState("전체"); // 상단 탭탭
  const [activeSubCategory, setActiveSubCategory] = useState("전체"); // 서브 탭

  const tabList = ["전체", "인기", "관심"];
  const subCategories = ["전체", "IT", "바이오", "에너지", "금융", "반도체", "게임"];

  const [visibleCount, setVisibleCount] = useState(20);

  const handleShowMore = () => {
    setVisibleCount((prev) => prev + 20);
  };

  const visibleStocks = dummyStockData.slice(0, visibleCount);

  return (
    <div className="container">
      <div className="containerBox">
        {/* 상단 탭 */}
         <TabMenu
          tabs={tabList}
          activeTab={activeTab}
          onTabChange={(tab) => setActiveTab(tab)}
          tabTextSize="2rem"
        />

        {/* 서브 카테고리 탭 */}
        <div className="subCategory">
          {subCategories.map((cat) => (
            <button
            key={cat}
            onClick={() => setActiveSubCategory(cat)}
            className={`subCategoryItems ${
              activeSubCategory === cat ? "active" : ""
            }`}
            >
              {cat}
            </button>
          ))}
        </div>
        <div className="categorie">
          <h1>종목명</h1>
          <h1>종가</h1>
          <h1>대비</h1>
          <h1>등락률</h1>
          <h1>시가</h1>
          <h1>고가</h1>
          <h1>저가</h1>
          <h1>거래량</h1>
          <h1>시가총액</h1>
        </div> 
        {/* 확인용 출력 */}
        <div className="list">
          {visibleStocks.map((stock) => (
            <StockItem key={stock.code} {...stock} />
          ))}

          
        </div>
      </div>
      {visibleCount < dummyStockData.length && (
            <button className="moreBtn" onClick={handleShowMore}>
              더보기<DownIcon fontSize="large"/>
            </button>
          )}
    </div>
  );
}