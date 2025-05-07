"use client";

import { notFound, useParams } from "next/navigation";
import "./noticeDetailPage.css"

const mockNotices = [
  { id: '1', type: '공지', title: '4월 서버 점검 안내', date: '2025-04-10', content: '4월 20일 서버 점검 예정입니다.\n01:00 ~ 03:00 동안 접속 불가합니다.' },
  { id: '2', type: '업데이트', title: '신규 기능 출시 안내', date: '2025-04-05', content: '새로운 기능이 추가되었습니다. 자세한 내용은 아래를 참고해주세요.' },

];

export default function NoticeDetailPage() {
  const { id } = useParams(); // useParams로 params를 가져옴
  const notice = mockNotices.find((n) => n.id === id);

  if (!notice) return notFound();

  return (
    <div className="noticeDetail_container">
      <h1 className="noticeDetail_title">공지사항
        <span>세부사항</span>
      </h1>
      <div className="mainContainer">
        <h1 className="notice-title">[{notice.type}] {notice.title}</h1>
        <div className="notice-meta">
          <span className="notice-date">{notice.date}</span>
        </div>
        <div className="notice-content">
          {notice.content.split("\n").map((line, idx) => (
            <p key={idx}>{line}</p>
          ))}
        </div>
      </div>
      <button onClick={() => history.back()} className="back-btn">목록으로</button>
    </div>
  );
}