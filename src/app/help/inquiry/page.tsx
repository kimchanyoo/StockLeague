"use client";

import { useRouter } from "next/navigation";
import "./inquiry.css";
import Link from "next/link";
import { useEffect, useState } from "react";
import MoreVert from "@/app/components/help/MoreVert";
import { getInquiries, Inquiry, deleteInquiry } from "@/lib/api/inquiry";
import { useAuth } from '@/context/AuthContext'; // 추가

const inquiriesPerPage = 10;
const maxPageButtons = 10;

export default function InquiryList() {
  const router = useRouter();
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const { user } = useAuth(); // 로그인 정보 가져오기
  const isLoggedIn = !!user;

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

  // 로그인하지 않았을 때 안내문 표시
  if (!isLoggedIn) {
    return (
      <div className="inquiry-container">
        <h1 className="inquiry-title">1:1 문의</h1>
        <div className="loginOverlay">
          <p>로그인이 필요합니다.</p>
          <Link href="/auth/login">
            <button className="loginBtn">로그인하러 가기</button>
          </Link>
        </div>
      </div>
    );
  }
  
  return (
    <div className="inquiry-container">
      <h1 className="inquiry-title">
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
                <MoreVert 
                  onEdit={() => router.push(`/help/inquiry/write?inquiryId=${item.inquiryId}`)} 
                  onDelete={async () => {
                    const confirmed = confirm("정말 삭제하시겠습니까?");
                    if (!confirmed) return;

                    try {
                      await deleteInquiry(item.inquiryId);
                      alert("삭제되었습니다.");

                      // 삭제 후 목록 갱신
                      const res = await getInquiries(currentPage, inquiriesPerPage);
                      setInquiries(res.inquiries);
                      setTotalCount(res.totalCount);
                    } catch (error) {
                      console.error("삭제 실패:", error);
                      alert("삭제에 실패했습니다.");
                    }
                  }} 
                />
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
