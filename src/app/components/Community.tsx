"use client";

import { useState, useEffect } from "react";
import styles from "@/app/styles/components/Community.module.css";
import Comment from "./Comment";
import { getCommentsByTicker, postComment, Comment as CommentType } from "@/lib/api/comment";
import { getTopStocks } from "@/lib/api/stock"
import { useAuth } from '@/context/AuthContext'; // 추가

type Props = {
  ticker: string;
};
const noticesPerPage = 20;
const maxPageButtons = 10;

const Community = ({ ticker }: Props) => {
  const [comments, setComments] = useState<CommentType[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [newComment, setNewComment] = useState("");
  const [stockName, setStockName] = useState<string>(ticker); // 기본값은 ticker
  
  const { user } = useAuth(); // 로그인 정보 가져오기
  const isLoggedIn = !!user;
  
  const fetchStockName = async () => {
    try {
      const res = await getTopStocks();
      const matchedStock = res.stocks.find((stock) => stock.stockTicker === ticker);
      if (matchedStock) {
        setStockName(matchedStock.stockName);
      }
    } catch (err) {
      console.error("종목 이름 가져오기 실패", err);
    }
  };

  const fetchComments = async () => {
    setLoading(true);
    try {
      const res = await getCommentsByTicker(ticker, currentPage, noticesPerPage);
      if (res.success) {
        setComments(res.comments);
        setTotalCount(res.totalCount);
      } else {
        setError("댓글을 불러오지 못했습니다.");
      }
    } catch (err) {
      setError("서버 요청 오류가 발생했습니다.");
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchStockName();
    fetchComments();
  }, [ticker, currentPage]);

  const handlePostComment = async () => {
    if (!isLoggedIn) { 
      alert("댓글 작성을 위해 로그인이 필요합니다.");
      return;
    }
    if (!newComment.trim()) return alert("댓글을 입력하세요");
    try {
      await postComment(ticker, newComment);
      setNewComment(""); // 입력창 비우기
      setCurrentPage(1); // 첫 페이지로 이동 (선택사항)
      fetchComments();  // 댓글 목록 새로고침
    } catch (e) {
      console.error("댓글 작성 실패", e);
    }
  };

  // 페이지네이션
  const totalPages = Math.ceil(totalCount / noticesPerPage);
  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
  const pageNumbers = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
      
  return (
    <div className={styles.container}>
      <h1>
        {stockName} 커뮤니티
      </h1>
            
      <div className={styles.searchSection}>
        <input type="text" value={newComment} onChange={(e) => setNewComment(e.target.value)}/>
        <button className={styles.mainBtn} onClick={handlePostComment}>의견 남기기</button>
      </div>
      <div className={styles.commentSection}>
        <Comment ticker={ticker}/>
      </div>

      {/* 페이지네이션 */}
      <div className={styles.pagination}>
        <button onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))} disabled={currentPage === 1}>
          이전
        </button>

        {pageNumbers.map((num) => (
          <button
            key={num}
            className={num === currentPage ? styles.active : ''}
            onClick={() => setCurrentPage(num)}
          >
            {num}
          </button>
        ))}

        <button onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))} disabled={currentPage === totalPages}>
          다음
        </button>
      </div>
    </div>
  );
};

export default Community;