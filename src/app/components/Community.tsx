"use client";

import React, {useState} from "react";
import styles from "@/app/styles/components/Community.module.css";
import Comment from "./Comment";
import Nickname from "../auth/nickname/page";

export default function Community() {

  const mockComment = Array.from({ length: 100 }, (_, i) => ({
    id: `${i + 1}`,
    parentId: null,
    nickname: `닉네임 ${i + 1}`,
    content: `이거 정말 유익한 정보네요!${i + 1}`,
    createdAt: `2025-04-${(i % 30 + 1).toString().padStart(2, "0")}`,
  }));

  const noticesPerPage = 20;
  const maxPageButtons = 10;
  const [currentPage, setCurrentPage] = useState(1);
  const totalPages = Math.ceil(mockComment.length / noticesPerPage);

  // 현재 페이지가 속한 그룹 (10개씩)
  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);

  const pageNumbers = [];
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  // 현재 페이지에 해당하는 댓글만 슬라이싱
  const currentComments = mockComment.slice(
    (currentPage - 1) * noticesPerPage,
    currentPage * noticesPerPage
  );

      
    return (
        <div className={styles.container}>
            <h1>
                종목이름 커뮤니티
            </h1>
            
            <div className={styles.searchSection}>
                <input type="text"/>
                <button className={styles.mainBtn}>의견 남기기</button>
            </div>
            <div className={styles.commentSection}>
                <Comment comments={currentComments}/>
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
}