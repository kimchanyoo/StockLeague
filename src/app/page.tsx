"use client";

import { useEffect, useState } from "react";
import styles from "@/app/styles/page.module.css";
import DownIcon from '@mui/icons-material/ArrowDropDown';
import RightIcon from '@mui/icons-material/ChevronRight';
import SignUpIcon from '@mui/icons-material/PersonAdd';
import SignInIcon from '@mui/icons-material/Login';
import TabMenu from "@/app/components/utills/TabMenu";
import StockItem from "@/app/components/stock/StockItem";
import { useRouter } from "next/navigation";
import Portfolio from "./components/user/Portfolio";
import { useAuth } from "@/context/AuthContext";
import { getNotices, Notice } from "@/lib/api/notice";
import { getTopStocks, StockPriceResponse, getPopularStocks, getWatchlist } from "@/lib/api/stock";

export default function Home() {
  const [notices, setNotices] = useState<Notice[]>([]);
  const [activeTab, setActiveTab] = useState("전체");
  const router = useRouter();

  const [visibleCount, setVisibleCount] = useState(20);
  const [stockData, setStockData] = useState<any[]>([]);
  const { user } = useAuth();
  const isLoggedIn = !!user;

  const tabList = isLoggedIn ? ["전체", "인기", "관심"] : ["전체", "인기"];
  const filteredStocks = stockData.filter(stock => {
    if (activeTab === "전체") return true;
    if (activeTab === "인기") return stock.isPopular;
    if (activeTab === "관심") return stock.isFavorite;
    return true;
  });

  const visibleStocks = filteredStocks.slice(0, visibleCount);

  useEffect(() => {
    console.log("✅ useEffect 시작됨");
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

    // 상위 종목 데이터 가져오기
    const loadStocks = async () => {
      try {
        const res = await getTopStocks(1, 100); // 100개 정도 불러오기
        if (res.success) {
          setStockData(res.stocks);
        }
      } catch (err) {
        console.error("❌ 종목 데이터 불러오기 실패:", err);
      }
    };

    loadNotices();
    loadStocks();
  }, []);

  useEffect(() => {
    async function fetchStocks() {
      const topRes = await getTopStocks(1, 200);
      let data = topRes.stocks.map(stock => ({ ...stock, isPopular: false, isFavorite: false }));

      // 인기 종목
      const popRes = await getPopularStocks();
      const popIds = new Set(popRes.stocks.map(s => s.stockId));
      data = data.map(stock => ({
        ...stock,
        isPopular: popIds.has(stock.stockId)
      }));

      // 관심 종목 (로그인 시에만)
      if (isLoggedIn) {
        const favRes = await getWatchlist();
        const favIds = new Set(favRes.watchlists.map(w => w.stockId));
        data = data.map(stock => ({
          ...stock,
          isFavorite: favIds.has(stock.stockId)
        }));
      }

      setStockData(data);
    }

    fetchStocks();
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

  return (
    <div className={styles.container}>
      <div className={styles.topSection}>
        <div className={styles.chartContainer}>   
          <img src="/images/main.avif" className={styles.mainImg}></img>
          <div className={styles.chartTitle}>
            <h1 className={styles.content}>
              <span className={styles.highlight}>스톡리그</span>에서 투자를<br/>경험하다
            </h1>
            <button className={styles.gotoBtn} onClick={handleGotoStockList}>거래 하러가기</button>
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
          <h1>전일 대비</h1>
          <h1>금일 등락률</h1>
          <h1>시가</h1>
          <h1>고가</h1>
          <h1>저가</h1>
          <h1>거래량</h1>
        </div>
        <div className={styles.list}>
          {visibleStocks.map((stock) => (
            <StockItem
              key={stock.stockTicker}
              ticker={stock.stockTicker}
              name={stock.stockName}
            />
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
