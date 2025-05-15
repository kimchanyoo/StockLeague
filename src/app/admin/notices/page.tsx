"use client";

import "./notices.css";
import { useState } from "react";

interface Notice {
  id: number;
  title: string;
  content: string;
  date: string;
  category: string;
  isDeleted?: boolean;
}
const inquiriesPerPage = 10;
const maxPageButtons = 10;

export default function Notices() {
  const [editMode, setEditMode] = useState(false);
  const [currentTitle, setCurrentTitle] = useState(""); 
  const [currentContent, setCurrentContent] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [selectedNotice, setSelectedNotice] = useState<Notice | null>(null);
  const [dummyNotices, setDummyNotices] = useState<Notice[]>([
    {
      id: 1,
      title: "공지사항 제목입니다",
      content: "공지의 상세 내용입니다.\n여러 줄도 표시할 수 있어요.",
      date: "2025-05-14",
      category: "일반",
    },
    // 필요 시 추가
  ]);
  
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(10);
  const totalPages = Math.ceil(totalCount / inquiriesPerPage);
  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
  const pageNumbers = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);

  const handleEdit = (notice: any) => {
    setEditMode(true);
    setEditingId(notice.id);
    setCurrentTitle(notice.title); 
    setCurrentContent(notice.content); 
  };

  const handleCancel = () => {
    setEditMode(false);
    setEditingId(null);
    setCurrentTitle("");
    setCurrentContent("");
  };

  const handleSubmit = () => {
    if (editMode) {
      // 수정 API 호출
      console.log("수정 내용:", currentContent);
    } else {
      // 등록 API 호출
      console.log("새 공지:", currentContent);
    }

    // 초기화
    setCurrentTitle("");
    setCurrentContent("");
    setEditMode(false);
    setEditingId(null);
  };

  const handleDelete = (noticeId: number) => {
    const updated = dummyNotices.map(notice =>
      notice.id === noticeId ? { ...notice, isDeleted: true } : notice
    );
    setDummyNotices(updated);
    console.log(`공지사항 회색 처리: ${noticeId}`);
  };

  const handleRestore = (noticeId: number) => {
    const updated = dummyNotices.map(notice =>
      notice.id === noticeId ? { ...notice, isDeleted: false } : notice
    );
    setDummyNotices(updated);
    console.log(`공지사항 복구: ${noticeId}`);
  };

  const handlePageClick = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <div className="notices-container">
      <div className="notices-write">
        <h1>{editMode ? "공지 수정" : "공지 작성"}</h1>
        <input 
          placeholder="공지 제목을 작성하세요."
          className="notices-mainTitle"
          value={currentTitle}
          onChange={(e) => setCurrentTitle(e.target.value)}
        />
        <textarea
          placeholder="공지 내용을 작성하세요."
          rows={5}
          className="notices-textarea"
          value={currentContent} // 수정 시 currentContent로 바인딩
          onChange={(e) => setCurrentContent(e.target.value)} // 수정하는 동안 상태 갱신
        />
        <div className="button-group">
          <button className="submit-button" onClick={handleSubmit}>
            {editMode ? "수정완료" : "작성완료"}
          </button>
          {editMode && (
            <button className="cancel-button" onClick={handleCancel}>
              취소
            </button>
          )}
        </div>
      </div>
      <div className="notices-list">
        <h1>공지 목록</h1>
        {dummyNotices.map((notice) => (
          <div
            key={notice.id}
            className={`notices-item ${notice.isDeleted ? "deleted" : ""}`}
            onClick={() => !notice.isDeleted && setSelectedNotice(notice)}
          >
            <div className="notices">{notice.category}</div>
            <div className="notices-title">{notice.title}</div>
            <div className="notices-date">{notice.date}</div>

            {!notice.isDeleted ? (
              <>
                <button
                  className="edit-button"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleEdit(notice);
                  }}
                >
                  수정
                </button>
                <button
                  className="delete-button"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDelete(notice.id);
                  }}
                >
                  삭제
                </button>
              </>
            ) : (
              <button
                className="restore-button"
                onClick={(e) => {
                  e.stopPropagation();
                  handleRestore(notice.id);
                }}
              >
                복구
              </button>
            )}
          </div>
        ))}
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

      {selectedNotice && (
        <div className="notice-modal-overlay" onClick={() => setSelectedNotice(null)}>
          <div
            className="notice-modal"
            onClick={(e) => e.stopPropagation()} // 클릭 전파 방지
          >
            <h2>{selectedNotice.title}</h2>
            <div className="notice-modal-content">
              {selectedNotice.content.split("\n").map((line, idx) => (
                <p key={idx}>{line}</p>
              ))}
            </div>
            <div className="notice-modal-footer">
              <span>{selectedNotice.date}</span>
              <button onClick={() => setSelectedNotice(null)}>닫기</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
