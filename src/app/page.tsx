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
import MainStockChart from "./components/MainStockChart";
import Portfolio from "./components/Portfolio";
import { useAuth } from "@/context/AuthContext";
import { getNotices, Notice } from "@/lib/api/notice";

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
  const [notices, setNotices] = useState<Notice[]>([]);
  const [activeTab, setActiveTab] = useState("전체");
  const tabList = ["전체", "인기", "관심"];
  const router = useRouter();

  const [visibleCount, setVisibleCount] = useState(20);
  const [stockData, setStockData] = useState<any[]>([]);
  const { user } = useAuth();
  const isLoggedIn = !!user;


  useEffect(() => {
    const data = generateDummyStockData();
    setStockData(data);

    // 공지사항 가져오기
    const loadNotices = async () => {
      try {
        const res = await getNotices(1, 3); // 최신 공지 3개
        if (res.success) {
          setNotices(res.notices);
        }
      } catch (err) {
        console.error("❌ 공지사항 불러오기 실패:", err);
      }
    };

    loadNotices();
  }, []);

  const handleGotoStockList = () => {
    router.push("/stocks/stockList");
  };
  const handleGotoAccount = () => {
    router.push("/user/account");
  };
  const handleShowMore = () => {
    setVisibleCount((prev) => prev + 20);
  };

  const visibleStocks = stockData.slice(0, visibleCount);

  return (
    <div className={styles.container}>
      <div className={styles.topSection}>
        <div className={styles.chartContainer}>
          <div className={styles.chart}>
            <MainStockChart/>
          </div>
          <div className={styles.chartTitle}>
            <h1 className={styles.content}>
              <span className={styles.highlight}>스톡리그</span>에서 투자를<br/>경험하다
            </h1>
            <button className={styles.gotoBtn} onClick={handleGotoStockList}>종목시세 보러가기</button>
          </div>
        </div>

        {/* 로그인 여부에 따라 다르게 표시 */}
        {!isLoggedIn ? (
          <div className={styles.loginContainer}>
            <div className={styles.signSection}>
              <button className={styles.signBtn} onClick={() => router.push("/auth/login")}><SignUpIcon sx={{ fontSize: "3.75rem", marginBottom: "28px" }}/>회원가입</button>
              <button className={styles.signBtn} onClick={() => router.push("/auth/login")}><SignInIcon sx={{ fontSize: "3.75rem", marginBottom: "28px" }}/>로그인</button>
            </div>
            <div className={styles.announcement}>
              <h1 className={styles.announcementTitle} onClick={() => router.push("/help/notice")}>
                공지사항<RightIcon/>
              </h1>
              {notices.length === 0 ? (
                <div className={styles.announcementContent}>공지사항이 없습니다.</div>
              ) : (
                notices.map((notice) => (
                  <div
                    key={notice.noticeId}
                    className={styles.announcementContent}
                    onClick={() => router.push(`/help/notice/${notice.noticeId}`)}
                  >
                    - {notice.title}
                  </div>
                ))
              )}
            </div>
          </div>
        ) : (
          <div className={styles.portfolioContainer} onClick={handleGotoAccount}>
            <h1>보유자산</h1>
            <Portfolio /> {/* 로그인하면 보유자산 컴포넌트 표시 */}
          </div>
        )}
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
