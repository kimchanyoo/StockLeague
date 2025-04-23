"use client";

import React, { useEffect, useState } from "react";
import styles from "@/app/styles/page.module.css";
import DownIcon from '@mui/icons-material/ArrowDropDown';
import RightIcon from '@mui/icons-material/ChevronRight';
import SignUpIcon from '@mui/icons-material/PersonAdd';
import SignInIcon from '@mui/icons-material/Login';
import TabMenu from "@/app/components/TabMenu";
import StockItem from "@/app/components/StockItem";
import { useRouter } from "next/navigation";

// 랜덤 주식 데이터 생성 함수
const generateDummyStockData = () => {
  return Array.from({ length: 100 }, (_, i) => ({
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
};

export default function Home() {
  const [activeTab, setActiveTab] = useState("전체");
  const tabList = ["전체", "인기", "관심"];
  const router = useRouter();

  const [visibleCount, setVisibleCount] = useState(20);
  const [stockData, setStockData] = useState<any[]>([]);

  useEffect(() => {
    const data = generateDummyStockData();
    setStockData(data);
  }, []);

  const handleShowMore = () => {
    setVisibleCount((prev) => prev + 20);
  };

  const visibleStocks = stockData.slice(0, visibleCount);

  return (
    <div className={styles.container}>
      <div className={styles.topSection}>
        <div className={styles.chartContainer}>
          <div className={styles.chart}></div>
          <div className={styles.chartTitle}>
            <h1 className={styles.content}>
              <span className={styles.highlight}>스톡리그</span>에서 투자를<br/>경험하다
            </h1>
            <button className={styles.gotoBtn}>종목시세 보러가기</button>
          </div>
        </div>

        <div className={styles.loginContainer}>
          <div className={styles.signSection}>
            <button className={styles.signBtn}><SignUpIcon sx={{ fontSize: "3.75rem", marginBottom: "28px" }}/>회원가입</button>
            <button className={styles.signBtn} onClick={() => router.push("/auth/login")}><SignInIcon sx={{ fontSize: "3.75rem", marginBottom: "28px" }}/>로그인</button>
          </div>
          <div className={styles.announcement}>
            <h1 className={styles.announcementTitle}>공지사항<RightIcon/></h1>
            <div className={styles.announcementContent}>이것은 첫 번째 공지사항 예시입니다.</div>
            <div className={styles.announcementContent}>이것은 두 번째 공지사항 예시입니다.</div>
            <div className={styles.announcementContent}>이것은 세 번째 공지사항 예시입니다.</div>
          </div>
        </div>
      </div>

      <h1 className={styles.stockTitle}>📈 오늘의 시세 📉</h1>
      <div className={styles.stockListContainer}>
        <TabMenu
          tabs={tabList}
          activeTab={activeTab}
          onTabChange={(tab) => setActiveTab(tab)}
          tabTextSize="2rem"
        />
        <div className={styles.categorie}>
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
        <div className={styles.list}>
          {visibleStocks.map((stock) => (
            <StockItem key={stock.code} {...stock} />
          ))}
        </div>
      </div>
      {visibleCount < stockData.length && (
        <button className={styles.moreBtn} onClick={handleShowMore}>
          더보기<DownIcon fontSize="large"/>
        </button>
      )}
    </div>
  );
}
