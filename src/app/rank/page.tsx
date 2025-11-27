"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import "./rank.css";
import DownIcon from "@mui/icons-material/ArrowDropDown";
import { useAuth } from "@/context/AuthContext";
import { useRankingSocket } from "@/socketHooks/useRankingSocket";
import { UserRanking, RankingMode } from "@/lib/api/rank";

export default function Rank() {
  const { user } = useAuth();
  const isLoggedIn = !!user;

  const [mode, setMode] = useState<RankingMode>("profit");
  const [visibleCount, setVisibleCount] = useState(20);
  const [myRankVisible, setMyRankVisible] = useState(true);
  const [rankingData, setRankingData] = useState<UserRanking[]>([]);
  const [myRanking, setMyRanking] = useState<UserRanking | null>(null);

  const myRankRef = useRef<HTMLDivElement>(null);

  const handleLoadMore = () => setVisibleCount((prev) => prev + 20);

  // 내 순위 화면 표시 여부
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => setMyRankVisible(!entry.isIntersecting),
      { threshold: 0.1 }
    );
    if (myRankRef.current) observer.observe(myRankRef.current);
    return () => {
      if (myRankRef.current) observer.unobserve(myRankRef.current);
    };
  }, [visibleCount, rankingData]);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const onUpdateGlobal = useCallback((data: any) => {
    setRankingData(data.rankingList);
  }, []);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const onUpdateMe = useCallback((data: any) => {
    setMyRanking(data.myRanking);
  }, []);

  useRankingSocket({ mode, onUpdateGlobal, onUpdateMe });

  const visibleRanks = rankingData.slice(0, visibleCount);

  const formatRank = (rank: number) => {
    if (rank === 1) return "1st 🥇";
    if (rank === 2) return "2nd 🥈";
    if (rank === 3) return "3rd 🥉";
    return `${rank}`;
  };

  return (
    <div className="rank_container">
      <h1 className="title">👑 랭킹 👑</h1>

      {/* 모드 토글 버튼 */}
      <div className="ranking-toggle">
        <button
          className={mode === "profit" ? "active" : ""}
          onClick={() => setMode("profit")}
        >
          수익률 기준
        </button>
        <button
          className={mode === "asset" ? "active" : ""}
          onClick={() => setMode("asset")}
        >
          총자산 기준
        </button>
      </div>

      <div className="rankBox">
        <div className="rankCategory">
          <h1>순위</h1>
          <h1>닉네임</h1>
          <h1>총자산</h1>
          <h1>수익률</h1>
        </div>
        <div className="rankList">
          {visibleRanks.map((user) => {
            const isMyRank = user.userId === myRanking?.userId;
            return (
              <div
                className={`rankItem ${isMyRank ? "highlight" : ""}`}
                key={user.userId}
                ref={isMyRank ? myRankRef : null}
              >
                <div>{formatRank(user.ranking)}</div>
                <div>{user.nickname}</div>
                <div>{Number(user.totalAsset).toLocaleString()}원</div>
                <div>{user.profitRate}%</div>
              </div>
            );
          })}
        </div>

        {isLoggedIn ? (
          myRanking && myRankVisible && (
            <div className="floatingMyRank">
              <div className="rankItem highlight">
                <div>{formatRank(myRanking.ranking)}</div>
                <div>{myRanking.nickname}</div>
                <div>{Number(myRanking.totalAsset).toLocaleString()}원</div>
                <div>{myRanking.profitRate}%</div>
              </div>
            </div>
          )
        ) : (
          <div className="floatingMyRank">
            <div className="Non-login">🔒 로그인 시, 랭킹이 표시됩니다.</div>
          </div>
        )}
      </div>

      {visibleCount < rankingData.length && (
        <button className="loadMoreBtn" onClick={handleLoadMore}>
          더보기 <DownIcon fontSize="large" />
        </button>
      )}
    </div>
  );
}
