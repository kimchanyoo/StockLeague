"use client";

import { useState } from "react";
import ReportDetailModal from "@/app/components/ReportDetailModal";
import "./reports.css";

// 타입 임시 정의
type Reporter = {
  id: string;
  nickname: string;
  reason: string;
  description?: string;
};
type ReportDetail = {
  id: string;
  commentId: string;
  commentContent: string;
  commentAuthor: string;
  commentAuthorId: string;
  commentCreatedAt: string;
  community: string;
  warnings: number;
  accountStatus: "정상" | "정지" | "경고";
  reporters: Reporter[];
};

const dummyReports: ReportDetail[] = [
  {
    id: "r1",
    commentId: "0000",
    commentContent: "이거 좀 과한 댓글인데요?",
    commentAuthor: "닉네임",
    commentAuthorId: "u001",
    commentCreatedAt: "2025-05-23 15:10",
    community: "삼성전자",
    warnings: 0,
    accountStatus: "정상",
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
    commentCreatedAt: "2025-05-22 13:45",
    community: "LG에너지솔루션",
    warnings: 1,
    accountStatus: "경고",
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
            <div>댓글 ID: {report.commentId}</div>
            <div>작성자: {report.commentAuthor}</div>
            <div>신고 수: {report.reporters.length}</div>
            <div>경고: {report.warnings}</div>
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