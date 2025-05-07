"use client";

import { useRouter } from 'next/navigation';
import "./inquiry.css";
import Link from 'next/link';
import { useState } from 'react';
import MoreVert from '@/app/components/MoreVert';

const inquiries = Array.from({ length: 1044 }, (_, i) => ({
  id: `${i + 1}`,
  type: i % 2 === 0 ? "랭킹킹" : "거래소소",
  title: `랭킹 점수 반영 문제 ${i + 1}`,
  date: `2025-04-${(i % 30 + 1).toString().padStart(2, "0")}`,
  status: `pending`
}));

const inquiriesPerPage = 10; // 한 페이지당 10개
const maxPageButtons = 10;

export default function Inquiry() {
  const router = useRouter();
  const [currentPage, setCurrentPage] = useState(1);
  const totalPages = Math.ceil(inquiries.length / inquiriesPerPage); // 최대 10페이지

  const startIdx = (currentPage - 1) * inquiriesPerPage;
  const currentInquiries = inquiries.slice(startIdx, startIdx + inquiriesPerPage);

  const handlePageClick = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);

  const pageNumbers = [];
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  return (
    <div className="container">
      <h1 className="title">1:1 문의
        <span>StockLeague 서비스 이용 중 불편한 점이 있으신가요?</span>
      </h1>
      
      <div className="inquiry-list">
        {currentInquiries.map((item) => (
          <div className="inquiry-item" key={item.id} onClick={() => router.push(`/help/inquiry/${item.id}`)}>
            <div className="inquiry-info">
              <h2 className="inquiry-title">{item.title}</h2>
              <span className="inquiry-date">{item.date}</span>
            </div>
            <div className='inquiry-right' onClick={(e) => { e.stopPropagation();}}>
              <div className={`inquiry-status ${item.status === 'pending' ? 'status-pending' : 'status-completed'}`}>
                {item.status === 'pending' ? '답변전' : '답변완'}
              </div>
              <MoreVert/>
            </div>
          </div>
        ))}
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

      <Link href="/help/inquiry/write">
        <button className="inquiry-button">문의하기</button>
      </Link>
    </div>
  );
}
