"use client";

import "./notices.css";
import { useState, useEffect } from "react";
import { getAdminNotices, getNoticeDetail, NoticeDetail, AdminNotice, createAdminNotice, updateAdminNotice, deleteAdminNotice, restoreAdminNotice, } from "@/lib/api/notice";
import PushPinIcon from '@mui/icons-material/PushPin';
import PushPinOutlinedIcon from '@mui/icons-material/PushPinOutlined';

const noticesPerPage = 10;
const maxPageButtons = 10;

export default function Notices() {

  const [editMode, setEditMode] = useState(false);
  const [currentTitle, setCurrentTitle] = useState(""); 
  const [currentContent, setCurrentContent] = useState("");
  const [currentCategory, setCurrentCategory] = useState("일반");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [selectedNotice, setSelectedNotice] = useState<NoticeDetail | null>(null);
  const [notices, setNotices] = useState<AdminNotice[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(10);
  const [currentIsPinned, setCurrentIsPinned] = useState(false);
  const [loading, setLoading] = useState(false);

  const totalPages = Math.ceil(totalCount / noticesPerPage);

  // 페이지네이션
  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
  const pageNumbers = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);

  const fetchAdminNotices = async (page = currentPage) => {
    try {
      setLoading(true);
      const data = await getAdminNotices({ page, size: noticesPerPage });
      if (data.success) {
        setNotices(data.notices);
        setTotalCount(data.totalCount);
      } else {
        console.error("공지사항 목록 조회 실패");
      }
    } catch (error) {
      console.error("공지사항 API 오류", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminNotices();
  }, [currentPage]);

  const handleEdit = async (notice: AdminNotice) => {
    try {
      const detail = await getNoticeDetail(notice.noticeId);
      setEditMode(true);
      setEditingId(detail.noticeId);
      setCurrentTitle(detail.title);
      setCurrentContent(detail.content);
      setCurrentCategory(detail.category);
      setCurrentIsPinned(detail.isPinned);
    } catch (error) {
      console.error("공지 상세 조회 실패", error);
      alert("공지 상세를 불러오지 못했습니다.");
    }
  };

  const handleCancel = () => {
    setEditMode(false);
    setEditingId(null);
    setCurrentCategory("일반");
    setCurrentTitle("");
    setCurrentContent("");
    setCurrentIsPinned(false);
  };

  const handleSubmit = async () => {
    try {
      if (editMode && editingId !== null) {
        await updateAdminNotice(editingId, {
        title: currentTitle,
        content: currentContent,
        category: currentCategory,
        isPinned: currentIsPinned,
        });
        alert("공지 수정 완료");
      } else {
        await createAdminNotice({
          title: currentTitle,
          content: currentContent,
          category: currentCategory,
        });
        alert("공지 작성 완료");
      }

      handleCancel();
      await fetchAdminNotices(1);
      setCurrentPage(1);
    } catch (e) {
      alert("저장 실패");
      console.error(e);
    }
  };

  const handlePinToggle = async (notice: AdminNotice) => {
    try {
      const detail = await getNoticeDetail(notice.noticeId); // 상세 조회

      await updateAdminNotice(notice.noticeId, {
        title: detail.title,
        content: detail.content, // 중요!
        category: detail.category,
        isPinned: !detail.isPinned,
      });

      await fetchAdminNotices(currentPage); // 목록 새로고침
      alert(`공지 ${!notice.isPinned ? "고정" : "고정 해제"} 완료`);
    } catch (error) {
      console.error(error);
      alert("고정 상태 변경 실패");
    }
  };

  const handleSelectNotice = async (noticeId: number) => {
    try {
      const detail = await getNoticeDetail(noticeId);
      setSelectedNotice(detail);
    } catch (error) {
      console.error("공지 상세 조회 실패", error);
    }
  };

  const handleDelete = async (noticeId: number) => {
    try {
      await deleteAdminNotice(noticeId);
      setNotices(notices.map(n => n.noticeId === noticeId ? { ...n, isDeleted: true } : n));
    } catch (e) {
      alert("삭제 실패");
      console.error(e);
    }
  };

  const handleRestore = async (noticeId: number) => {
    try {
      await restoreAdminNotice(noticeId);
      setNotices(notices.map(n => n.noticeId === noticeId ? { ...n, isDeleted: false } : n));
    } catch (e) {
      alert("복구 실패");
      console.error(e);
    }
  }

  const handlePageClick = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <div className="notices-container">
      <div className="notices-write">
        <h1>{editMode ? "공지 수정" : "공지 작성"}</h1>
        <div className="notices-top">
          <input 
            placeholder="공지 제목을 작성하세요."
            className="notices-mainTitle"
            value={currentTitle}
            onChange={(e) => setCurrentTitle(e.target.value)}
          />
          <label className="category-label">
            카테고리:
            <select
              className="category-select"
              value={currentCategory}
              onChange={(e) => setCurrentCategory(e.target.value)}
            >
              <option value="일반">일반</option>
              <option value="이벤트">이벤트</option>
              <option value="점검">점검</option>
            </select>
          </label>
        </div>
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
        {loading && <p style={{textAlign: "center"}}>로딩중...</p>}
        {!loading && notices.length === 0 && <p style={{textAlign: "center"}}>공지 내역이 없습니다.</p>}
        {notices.map((notice) => (
          <div
            key={notice.noticeId}
            className={`notices-item ${notice.isDeleted ? "deleted" : ""}`}
            onClick={() => {
              if (!notice.isDeleted) handleSelectNotice(notice.noticeId);
            }}
          >
            <div className="notices">{notice.category}</div>
            <div className="notices-title">{notice.title}</div>
            <div className="notices-date">{notice.createdAt}</div>

            {!notice.isDeleted ? (
              <>
                <button
                  className={`pin-button ${notice.isPinned ? "pinned" : ""}`}
                  onClick={(e) => {
                    e.stopPropagation();
                    handlePinToggle(notice);
                  }}
                  title={notice.isPinned ? "고정 해제" : "고정"}
                >
                  {notice.isPinned ? (
                    <PushPinIcon style={{ color: 'red' }} />
                  ) : (
                    <PushPinOutlinedIcon style={{ color: 'gray' }} />
                  )}
                </button>
                <button className="edit-button" onClick={(e) => { e.stopPropagation(); handleEdit(notice); }}>
                  수정
                </button>
                <button className="delete-button" onClick={(e) => { e.stopPropagation(); handleDelete(notice.noticeId); }}>
                  삭제
                </button>
              </>
            ) : (
              <button
                className="restore-button"
                onClick={(e) => {
                  e.stopPropagation();
                  handleRestore(notice.noticeId);
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
          <div className="notice-modal" onClick={(e) => e.stopPropagation()}>
            <h2>{selectedNotice.title}</h2>
            <div className="notice-modal-content">
              {selectedNotice.content.split("\n").map((line, idx) => (
                <p key={idx}>{line}</p>
              ))}
            </div>
            <div className="notice-modal-footer">
              <span>{new Date(selectedNotice.createdAt).toLocaleDateString()}</span>
              <button onClick={() => setSelectedNotice(null)}>닫기</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}