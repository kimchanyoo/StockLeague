"use client";

import "./notice.css";
import SearchIcon from "@mui/icons-material/Search";
import Link from "next/link";
import { useState } from "react";

const noticesPerPage = 10;
const maxPageButtons = 10;

const mockNotices = Array.from({ length: 971 }, (_, i) => ({
  id: `${i + 1}`,
  type: i % 2 === 0 ? "공지" : "업데이트",
  title: `공지사항 ${i + 1}`,
  date: `2025-04-${(i % 30 + 1).toString().padStart(2, "0")}`,
}));

export default function Notice() {
  const [currentPage, setCurrentPage] = useState(1);
  const totalPages = Math.ceil(mockNotices.length / noticesPerPage);

  const indexOfLastNotice = currentPage * noticesPerPage;
  const indexOfFirstNotice = indexOfLastNotice - noticesPerPage;
  const currentNotices = mockNotices.slice(indexOfFirstNotice, indexOfLastNotice);

  // 현재 페이지가 속한 그룹 (10개씩)
  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);

  const pageNumbers = [];
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  return (
    <div className="container">
      <h1 className="title">공지사항</h1>

      <div className="search">
        <input type="text" />
        <button><SearchIcon /></button>
      </div>

      <table className="notice-table">
        <thead>
          <tr>
            <th>번호</th>
            <th>구분</th>
            <th>제목</th>
            <th>등록일</th>
          </tr>
        </thead>
        <tbody>
          {currentNotices.map((notice, index) => (
            <tr key={notice.id}>
              <td>{mockNotices.length - (indexOfFirstNotice + index)}</td>
              <td>{notice.type}</td>
              <td>
                <Link href={`/help/notice/${notice.id}`}>{notice.title}</Link>
              </td>
              <td>{notice.date}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* 페이지네이션 */}
      <div className="pagination">
        <button onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))} disabled={currentPage === 1}>
          이전
        </button>

        {pageNumbers.map((num) => (
          <button
            key={num}
            className={num === currentPage ? 'active' : ''}
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
