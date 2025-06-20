"use client";

import { useEffect, useRef, useState } from "react";
import "./rank.css";
import DownIcon from '@mui/icons-material/ArrowDropDown';
import { useAuth } from "@/context/AuthContext";

const dummyRankData = Array.from({ length: 100 }, (_, i) => ({
  rank: i + 1,
  nickname: `유저${i + 1}`,
  totalAssets: 10000000 + i * 10000,
  returnRate: (Math.random() * 20 - 5).toFixed(2),
}));

const myRank = {
  rank: 87,
  nickname: "김석환",
  totalAssets: 12345678,
  returnRate: "8.34",
};

export default function Rank() {
  const { user } = useAuth();
  const isLoggedIn = !!user;

  const [visibleCount, setVisibleCount] = useState(20);
  const [myRankVisible, setMyRankVisible] = useState(true);

  const myRankRef = useRef<HTMLDivElement>(null);

  const handleLoadMore = () => {
    setVisibleCount((prev) => prev + 20);
  };

  const visibleRanks = dummyRankData.slice(0, visibleCount);

  // 내 순위가 보이면 고정된 박스 숨기기
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        setMyRankVisible(!entry.isIntersecting);
      },
      { threshold: 0.1 }
    );

    if (myRankRef.current) observer.observe(myRankRef.current);
    return () => {
      if (myRankRef.current) observer.unobserve(myRankRef.current);
    };
  }, [visibleCount]);

  const formatRank = (rank: number) => {
    if (rank === 1) return "1st 🥇";
    if (rank === 2) return "2nd 🥈";
    if (rank === 3) return "3rd 🥉";
    return `${rank}`;
  };

  return (
    <div className="rank_container">
      <h1 className="title">👑 랭킹 👑</h1>
      <div className="rankBox">
        <div className="rankCategory">
          <h1>순위</h1>
          <h1>닉네임</h1>
          <h1>총자산</h1>
          <h1>수익률</h1>
        </div> 
        <div className="rankList">
          {visibleRanks.map((user, index) => {
            const isMyRank = user.rank === myRank.rank;
            return (
              <div
                className={`rankItem ${isMyRank ? "highlight" : ""}`}
                key={user.rank}
                ref={isMyRank ? myRankRef : null}
              >
                <div>{formatRank(index + 1)}</div>
                <div>{user.nickname}</div>
                <div>{user.totalAssets.toLocaleString()}원</div>
                <div>{user.returnRate}%</div>
              </div>
            );
          })}
        </div>
        {isLoggedIn ? (
          myRankVisible && (
            <div className="floatingMyRank">
              <div className="rankItem highlight">
                <div>{myRank.rank}</div>
                <div>{myRank.nickname}</div>
                <div>{myRank.totalAssets.toLocaleString()}원</div>
                <div>{myRank.returnRate}%</div>
              </div>
            </div>
          )
        ) : (
          <div className="floatingMyRank">
            <div className="Non-login">
              <div>
                🔒 로그인 시, 랭킹이 표시됩니다.
              </div>
            </div>
          </div>
        )}
      </div>
      {visibleCount < dummyRankData.length && (
        <button className="loadMoreBtn" onClick={handleLoadMore}>
          더보기<DownIcon fontSize="large"/>
        </button>
      )}
    </div>
  );
}