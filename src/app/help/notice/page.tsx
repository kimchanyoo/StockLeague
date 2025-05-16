"use client";

import "./notice.css";
import SearchIcon from "@mui/icons-material/Search";
import Link from "next/link";
import { useEffect, useState } from "react";
import { getNotices, Notice} from "@/lib/api/notice";

const noticesPerPage = 10;
const maxPageButtons = 10;

export default function NoticeList() {

  const [notices, setNotices] = useState<Notice[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState(""); // ✅ 검색어 상태
  const [isSearching, setIsSearching] = useState(false);

  const totalPages = Math.ceil(totalCount / noticesPerPage);

  useEffect(() => {
    const fetchNotices = async () => {
      try {
        const data = await getNotices(currentPage, noticesPerPage, searchKeyword);
        setNotices(data.notices);
        setTotalCount(data.totalCount);
      } catch (error) {
        console.error("공지사항을 불러오는 데 실패했습니다:", error);
      }
    };

    fetchNotices();
  }, [currentPage, searchKeyword]);

  // 페이지네이션 버튼 범위
  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
  const pageNumbers = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
  
  // 🔍 검색 버튼 클릭 시 실행
  const handleSearch = () => {
    setCurrentPage(1);
    setIsSearching(searchKeyword.trim() !== "");
  };

  return (
    <div className="container">
      <h1 className="title">공지사항</h1>

      <div className="search">
        <input type="text" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} placeholder="검색어를 입력하세요"/>
        <button onClick={handleSearch}><SearchIcon /></button>
      </div>

      <div className="notice-categorie">
        <h1>번호</h1>
        <h1>구분</h1>
        <h1>제목</h1>
        <h1>등록일</h1>
      </div> 

      <div className="notice-cards">
        {notices.length === 0 ? (
          <div className="no-notice">공지사항이 없습니다.</div>
        ) : (
          notices.map((notice, index) => (
            <Link
              href={`/help/notice/${notice.noticeId}`}
              className="notice-item"
              key={notice.noticeId}
            >
              <div className="card-no">
                {totalCount - ((currentPage - 1) * noticesPerPage + index)}
              </div>
              <div className="card-type">{notice.category}</div>
              <div className="card-title">{notice.title}</div>
              <div className="card-date">
                {new Date(notice.createdAt).toLocaleDateString()}
              </div>
            </Link>
          ))
        )}
      </div>

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
