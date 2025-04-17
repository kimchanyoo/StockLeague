"use client";

import { useParams } from 'next/navigation';
import { useState } from 'react';
import "./inquiryDetailPage.css";

// 댓글 타입 정의
type Comment = {
  nickname: string;
  text: string;
  date: string;
};

export default function InquiryDetailPage() {
  const params = useParams();
  const { id } = params;

  const [comment, setComment] = useState("");
  const [comments, setComments] = useState<Comment[]>([]);

  const handleAddComment = () => {
    if (comment.trim() === "") return;

    const newComment: Comment = {
      nickname: "현재유저", // 이건 실제 로그인 유저 정보로 대체 가능
      text: comment,
      date: new Date().toISOString().slice(0, 10), // YYYY-MM-DD 형식
    };

    setComments([...comments, newComment]);
    setComment(""); // 입력창 초기화
  };

  // 임시 데이터 예시
  const inquiry = {
    id,
    type: '랭킹',
    title: '랭킹 점수 반영 문제',
    content: '랭킹 점수가 이상하게 계산됩니다.',
    date: '2025-04-10',
    status: 'pending',
  };

  return (
    <div className="container">
      <h1 className="title">1:1 문의
        <span>상세내용</span>
      </h1>  
      <div className="inquiry-container">
        <div className="inquiry-title">
          <h1>문의내용</h1>
          <div className="inquiry-subTitle">
            <p>문의 유형: <span>{inquiry.type}</span></p>
            <p>
              상태:{" "}
              <span className={inquiry.status === "pending" ? "status-pending" : "status-completed"}>
                {inquiry.status === "pending" ? "답변전" : "답변완"}
              </span>
            </p>
          </div>
        </div>

        <div className="inquiry-details">
          <div>
            <h1>{inquiry.title}</h1>
            <p><strong>문의 날짜:</strong> {inquiry.date}</p>
          </div>
          <p>{inquiry.content}</p>
        </div>
        
        <label>답변내용</label>
        <div className="answer-contents">
          <p><strong>답변 날짜:</strong> {inquiry.date}</p>
          <p>여기 답변 내용</p>
        </div>

        <label>추가문의</label>
        <div className="additional-inquiries">
          {comments.map((c, idx) => (
            <div key={idx} className="comment-item">
              <div className="comment-row">
                <span className="nickname">{c.nickname}</span>
                <span className="text">{c.text}</span>
                <span className="date">{c.date}</span>
              </div>
          </div>
          ))}
        </div>

        <div className="additional-comment">
          <input 
            type="text"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="추가 문의 및 댓글을 남겨주세요."
          />
          <button onClick={handleAddComment}>의견 남기기</button>
        </div>
      </div>
    </div>
  );
}
