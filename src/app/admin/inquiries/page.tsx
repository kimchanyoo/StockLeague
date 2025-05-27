"use client";

import "./inquiries.css";
import { useState, useEffect } from "react";
import {
  AdminInquiry,
  AdminInquiryDetailResponse,
  AdminAnswerRequest,
  AdminAnswerResponse,
  AdminInquiryListResponse,
} from "@/lib/api/inquiry";
import { getAdminInquiries, getAdminInquiryDetail, createInquiryAnswer } from "@/lib/api/inquiry";
import { categories } from "@/app/components/InquiryDropdown";

const inquiriesPerPage = 10;
const maxPageButtons = 10;

export default function Inquiries() {
  const [inquiries, setInquiries] = useState<AdminInquiry[]>([]);
  const [selectedInquiry, setSelectedInquiry] = useState<AdminInquiryDetailResponse | null>(null);
  const [answerText, setAnswerText] = useState("");
  const [loading, setLoading] = useState(false);

  // 페이지네이션
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(10);
  const totalPages = Math.ceil(totalCount / inquiriesPerPage);
  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
  const pageNumbers = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);

  const handlePageClick = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  // 문의 목록 불러오기
  const fetchInquiries = async (page: number) => {
    try {
      setLoading(true);
      const res: AdminInquiryListResponse = await getAdminInquiries(page, inquiriesPerPage);
      if (res.success) {
        setInquiries(res.inquiries);
        setTotalCount(res.totalCount);
      }
    } catch (error) {
      alert("문의 목록을 불러오는 중 오류가 발생했습니다.");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // 선택한 문의 상세 불러오기
  const fetchInquiryDetail = async (inquiryId: number) => {
    try {
      setLoading(true);
      const res: AdminInquiryDetailResponse = await getAdminInquiryDetail(inquiryId);
          console.log("상세 문의 응답:", res);  // 여기 추가

      if (res.success) {
        setSelectedInquiry(res);
        setAnswerText(""); // 답변 등록만 가능하므로 초기화
      }
    } catch (error) {
      alert("문의 상세를 불러오는 중 오류가 발생했습니다.");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // 답변 제출
  const handleSubmit = async () => {
    if (!selectedInquiry) return;
    if (answerText.trim() === "") {
      alert("답변을 입력해주세요.");
      return;
    }

    try {
      setLoading(true);
      const reqData: AdminAnswerRequest = { content: answerText };
      const res: AdminAnswerResponse = await createInquiryAnswer(selectedInquiry.inquiryId, reqData);

      if (res.success) {
        alert("답변이 등록되었습니다.");
        await fetchInquiries(currentPage);
        setSelectedInquiry(null);
        setAnswerText("");
      } else {
        alert(res.message || "답변 등록에 실패했습니다.");
      }
    } catch (error) {
      alert("답변 등록 중 오류가 발생했습니다.");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInquiries(currentPage);
  }, [currentPage]);

  return (
    <div className="inquiries-container">
      <div className="inquiries-list">
        <h1>문의 목록</h1>
        {loading && <p>로딩중...</p>}
        {!loading && inquiries.length === 0 && <p style={{textAlign: "center"}}>문의 내역이 없습니다.</p>}

        {inquiries.map((inq) => (
          <div
            key={inq.inquiryId}
            className={`inquiry-item ${inq.status === "ANSWERED" ? "answered" : "pending"}`}
            onClick={() => {
              fetchInquiryDetail(inq.inquiryId);
            }}
          >
            <div className="inquiry-title">{inq.title}</div>
            <div className="inquiry-category">[{categories[inq.category] || inq.category}]</div>
            <div className="inquiry-date">{new Date(inq.createdAt).toLocaleDateString()}</div>
            <div className="inquiry-status">{inq.status === "ANSWERED" ? "답변 완료" : "처리 중"}</div>
          </div>
        ))}

        {selectedInquiry && (
          <div className="inquiry-detail">
            <h2>{selectedInquiry.title}</h2>
            <p className="inquiry-content">{selectedInquiry.content}</p>

            {/* 답변이 있으면 항상 보여줌 */}
            {selectedInquiry.answers?.content && (
              <div className="my-answer">
                <h3>내 답변</h3>
                <p>{selectedInquiry.answers.content}</p>
              </div>
            )}

            {/* 답변이 없으면 입력창과 등록 버튼 표시 */}
            {!selectedInquiry.answers?.content && (
              <>
                <textarea
                  rows={5}
                  placeholder="답변을 입력하세요"
                  value={answerText}
                  onChange={(e) => setAnswerText(e.target.value)}
                  className="inquiry-answer-textarea"
                />
                <div className="button-group">
                  <button onClick={handleSubmit} className="submit-button" disabled={loading}>
                    답변 등록
                  </button>
                  <button
                    onClick={() => {
                      setSelectedInquiry(null);
                      setAnswerText("");
                    }}
                    className="cancel-button"
                  >
                    닫기
                  </button>
                </div>
              </>
            )}

            {/* 답변이 있을 땐 닫기 버튼만 표시 */}
            {selectedInquiry.answers?.content && (
              <button
                onClick={() => {
                  setSelectedInquiry(null);
                  setAnswerText("");
                }}
                className="cancel-button"
              >
                닫기
              </button>
            )}
          </div>
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
    </div>
  );
}
