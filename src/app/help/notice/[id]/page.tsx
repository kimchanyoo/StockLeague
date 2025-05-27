"use client";

import { useParams, useRouter } from "next/navigation"; // useRouter 추가
import { getNoticeDetail } from "@/lib/api/notice";
import { useEffect, useState } from "react";
import "./noticeDetailPage.css"

export default function NoticeDetailPage() {
  const { id } = useParams(); // useParams로 params를 가져옴
  const [notice, setNotice] = useState<any>(null);
  const [error, setError] = useState(false);
  const router = useRouter();

  useEffect(() => {
    if (!id) return;

    const fetchNotice = async () => {
      try {
        const data = await getNoticeDetail(Number(id));
        setNotice(data);
      } catch (err) {
        setError(true);
      }
    };

    fetchNotice();
  }, [id]);

  if (error) return <p>공지사항을 불러오는 데 실패했습니다.</p>;
  if (!notice) return <p>로딩 중...</p>;

  return (
    <div className="noticeDetail_container">
      <h1 className="noticeDetail_title">
        공지사항
        <span>세부사항</span>
      </h1>
      <div className="mainContainer">
        <h1 className="notice-title">
          [{notice.category}] {notice.title}
          </h1>
        <div className="notice-meta">
          <span className="notice-date">
            {new Date(notice.createdAt).toLocaleDateString()}
            </span>
        </div>
        <div className="notice-content">
          {notice.content.split("\n").map((line: string, idx: number) => (
            <p key={idx}>{line}</p>
          ))}
        </div>
      </div>
      <button onClick={() => router.push("/help/notice")} className="back-btn">목록으로</button>
    </div>
  );
}