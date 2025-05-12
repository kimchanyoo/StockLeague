"use client";

import { useRouter } from "next/navigation";
import "./inquiry.css";
import Link from "next/link";
import { useEffect, useState } from "react";
import MoreVert from "@/app/components/MoreVert";
import { getInquiries, Inquiry } from "@/lib/api/inquiry";

const inquiriesPerPage = 10;
const maxPageButtons = 10;

export default function InquiryList() {
  const router = useRouter();
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(0);

  const totalPages = Math.ceil(totalCount / inquiriesPerPage);

  useEffect(() => {
    const fetchInquiries = async () => {
      try {
        const res = await getInquiries(currentPage, inquiriesPerPage);
        setInquiries(res.inquiries);
        setTotalCount(res.totalCount);
      } catch (err) {
        console.error("문의 목록 불러오기 실패:", err);
      }
    };

    fetchInquiries();
  }, [currentPage]);

  const handlePageClick = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
  const pageNumbers = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);

  return (
    <div className="container">
      <h1 className="title">
        1:1 문의
        <span>StockLeague 서비스 이용 중 불편한 점이 있으신가요?</span>
      </h1>

      <div className="inquiry-list">
        {inquiries.length === 0 ? (
          <div className="no-inquiry">문의 내역이 없습니다.</div>
        ) : (
          inquiries.map((item) => (
            <div
              className="inquiry-item"
              key={item.inquiryId}
              onClick={() => router.push(`/help/inquiry/${item.inquiryId}`)}
            >
              <div className="inquiry-info">
                <h2 className="inquiry-title">{item.title}</h2>
                <span className="inquiry-date">
                  {new Date(item.createdAt).toLocaleDateString()}
                </span>
              </div>
              <div className="inquiry-right" onClick={(e) => e.stopPropagation()}>
                <div
                  className={`inquiry-status ${
                    item.status === "WAITING" ? "status-pending" : "status-completed"
                  }`}
                >
                  {item.status === "WAITING" ? "답변전" : "답변완"}
                </div>
                <MoreVert />
              </div>
            </div>
          ))
        )}
      </div>

      <div className="pagination">
        <button onClick={() => handlePageClick(Math.max(currentPage - 1, 1))} disabled={currentPage === 1}>
          이전
        </button>

        {pageNumbers.map((num) => (
          <button
            key={num}
            className={num === currentPage ? "active" : ""}
            onClick={() => handlePageClick(num)}
          >
            {num}
          </button>
        ))}

        <button
          onClick={() => handlePageClick(Math.min(currentPage + 1, totalPages))}
          disabled={currentPage === totalPages}
        >
          다음
        </button>
      </div>

      <Link href="/help/inquiry/write">
        <button className="inquiry-button">문의하기</button>
      </Link>
    </div>
  );
}
