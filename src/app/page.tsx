"use client";

import React, {useState} from "react";
import styles from "@/app/styles/page.module.css";
import DownIcon from '@mui/icons-material/ArrowDropDown';
import RightIcon from '@mui/icons-material/ChevronRight';
import SignUpIcon from '@mui/icons-material/PersonAdd';
import SignInIcon from '@mui/icons-material/Login';
import TabMenu from "@/app/components/TabMenu";
import { useRouter } from "next/navigation";

export default function Home() {

  const [activeTab, setActiveTab] = useState("전체");
  const tabList = ["전체", "인기", "관심"];
  const router = useRouter();

  return (
    <div className={styles.container}>
      <div className={styles.topSection}>
        <div className={styles.chartContainer}>
          <div className={styles.chart}>
          {/* 차트 컴포넌트 들어갈 부분 */}
          </div>
          <div className={styles.chartTitle}>
            <h1 className={styles.content}>
              <span className={styles.highlight}>스톡리그</span>에서 투자를<br/>경험하다
            </h1>
            <button className={styles.gotoBtn}>종목시세 보러가기</button>
          </div>
        </div>

        <div className={styles.loginContainer}>
          <div className={styles.signSection}>
            <button className={styles.signBtn} ><SignUpIcon sx={{ fontSize: "3.75rem", marginBottom: "28px" }}/>회원가입</button>
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
        <div className={styles.kategorie}>
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
          {/* 종목 */}
      </div>
      <h1 className={styles.moreBtn}>더보기<DownIcon fontSize="large"/></h1>
    </div>
     
  );
}
