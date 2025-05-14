"use client";

import "./inquiries.css";
import { useState } from "react";

interface Inquiry {
  id: number;
  title: string;
  content: string;
  date: string;
  category: string;
  isAnswered: boolean;
  answer?: string;
}

export default function Inquiries() {
  const [selectedInquiry, setSelectedInquiry] = useState<Inquiry | null>(null);
  const [answerText, setAnswerText] = useState("");
  const [inquiries, setInquiries] = useState<Inquiry[]>([
    {
      id: 1,
      title: "회원가입이 안돼요",
      content: "회원가입 버튼이 안 눌려요.",
      date: "2025-05-14",
      category: "회원가입", // ← 추가
      isAnswered: false,
    },
    {
      id: 2,
      title: "계정 삭제 문의",
      content: "계정을 삭제하고 싶습니다.",
      date: "2025-05-13",
      category: "계정",
      isAnswered: true,
      answer: "마이페이지에서 직접 가능합니다.",
    },
  ]);

  const handleSubmit = () => {
    if (!selectedInquiry) return;
    const updated = inquiries.map((inq) =>
      inq.id === selectedInquiry.id
        ? { ...inq, isAnswered: true, answer: answerText }
        : inq
    );
    setInquiries(updated);
    setSelectedInquiry(null);
    setAnswerText("");
  };

  return (
    <div className="inquiries-container">
      <div className="inquiries-list">
        <h1>문의 목록</h1>
        {inquiries.map((inq) => (
          <div
            key={inq.id}
            className={`inquiry-item ${inq.isAnswered ? "answered" : "pending"}`}
            onClick={() => {
              setSelectedInquiry(inq);
              setAnswerText(inq.answer || "");
            }}
          >
            <div className="inquiry-title">{inq.title}</div>
            <div className="inquiry-category">[{inq.category}]</div>
            <div className="inquiry-date">{inq.date}</div>
            <div className="inquiry-status">
              {inq.isAnswered ? "답변 완료" : "처리 중"}
            </div>
          </div>
        ))}
        {selectedInquiry && (
          <div className="inquiry-detail">
            <h2>{selectedInquiry.title}</h2>
            <p className="inquiry-content">{selectedInquiry.content}</p>

            <textarea
              rows={5}
              placeholder="답변을 입력하세요"
              value={answerText}
              onChange={(e) => setAnswerText(e.target.value)}
              className="inquiry-answer-textarea"
            />
            <div className="button-group">
              <button onClick={handleSubmit} className="submit-button">
                {selectedInquiry.isAnswered ? "답변 수정" : "답변 등록"}
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
          </div>
        )}
      </div>

      
    </div>
  );
}