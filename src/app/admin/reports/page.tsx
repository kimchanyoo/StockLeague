"use client";

import { useState } from "react";
import ReportDetailModal from "@/app/components/ReportDetailModal";
import "./reports.css";

// 타입 임시 정의
type ReportDetail = {
  id: string;
  commentId: string; // 추가
  commentContent: string;
  commentAuthor: string;
  commentAuthorId: string;
  warnings: number;
  reporterCount: number; // 추가
  reporters: {
    id: string;
    nickname: string;
    reason: string;
    description?: string;
  }[];
};

const dummyReports: ReportDetail[] = [
  {
    id: "r1",
    commentId: "0000",
    commentContent: "이거 좀 과한 댓글인데요?",
    commentAuthor: "닉네임",
    commentAuthorId: "u001",
    warnings: 0,
    reporterCount: 1,
    reporters: [
      {
        id: "u123",
        nickname: "신고자1",
        reason: "욕설",
        description: "과한 단어가 포함되어 있습니다.",
      },
    ],
  },
  {
    id: "r2",
    commentId: "0001",
    commentContent: "진짜 수준 떨어지네",
    commentAuthor: "badUser",
    commentAuthorId: "u002",
    warnings: 1,
    reporterCount: 2,
    reporters: [
      {
        id: "u456",
        nickname: "신고자2",
        reason: "비하",
        description: "상대방을 비하하는 표현입니다.",
      },
      {
        id: "u789",
        nickname: "신고자3",
        reason: "욕설",
        description: "욕설 포함되어 있어요.",
      },
    ],
  },
];

export default function Reports () {
const [selectedReport, setSelectedReport] = useState<ReportDetail | null>(null);
  const [modalOpen, setModalOpen] = useState(false);

  const handleClick = (report: ReportDetail) => {
    setSelectedReport(report);
    setModalOpen(true);
  };

  return (
    <div className="reports-container">
      <div className="reports-list">
        <h1>신고 목록</h1>
        {dummyReports.map((report) => (
          <div
            key={report.id}
            className="reports-item"
            onClick={() => handleClick(report)}
          >
            <div>댓글 ID: {report.commentId} | </div>
            <div>작성자: {report.commentAuthor} | </div>
            <div>신고 수: {report.reporterCount} | </div>
            <div>경고: {report.warnings} | </div>
          </div>
        ))}
      </div>
      <ReportDetailModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        report={selectedReport}
      />
    </div>
  );
}