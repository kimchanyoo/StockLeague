"use client";

import React from "react";

type Reporter = {
  id: string;
  nickname: string;
  reason: string;
  description?: string;
};

type ReportDetail = {
  id: string;
  commentContent: string;
  commentAuthor: string;
  commentAuthorId: string;
  warnings: number;
  reporters: Reporter[];
};

type Props = {
  open: boolean;
  onClose: () => void;
  report: ReportDetail | null;
};

const ReportDetailModal = ({ open, onClose, report }: Props) => {
  if (!open || !report) return null;

  const handleBackgroundClick = (e: React.MouseEvent) => {
    if ((e.target as HTMLElement).id === "modal-overlay") {
      onClose();
    }
  };

  return (
    <div
      id="modal-overlay"
      onClick={handleBackgroundClick}
      className="fixed inset-0 bg-black/50 flex justify-center items-center z-50"
    >
      <div
        className="bg-white p-6 rounded-lg w-full max-w-xl shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-xl font-bold mb-4">신고 상세</h2>

        <div className="mb-4">
          <p><strong>댓글 내용:</strong> {report.commentContent}</p>
          <p><strong>작성자:</strong> {report.commentAuthor} (ID: {report.commentAuthorId})</p>
          <p><strong>누적 경고:</strong> {report.warnings}회</p>
        </div>

        <div className="mb-4">
          <h3 className="font-semibold mb-2">신고자 목록</h3>
          <ul className="space-y-2 text-sm">
            {report.reporters.map((r) => (
              <li key={r.id} className="border p-2 rounded-md bg-gray-50">
                <p><strong>닉네임:</strong> {r.nickname}</p>
                <p><strong>사유:</strong> {r.reason}</p>
                {r.description && <p><strong>설명:</strong> {r.description}</p>}
              </li>
            ))}
          </ul>
        </div>

        <div className="mb-4 space-y-2">
          <h3 className="font-semibold">관리자 조치</h3>
          <div className="flex flex-col space-y-1">
            <label><input type="checkbox" /> 댓글 삭제</label>
            <label><input type="checkbox" /> 경고 부여</label>
            <label><input type="checkbox" /> 반려 처리</label>
          </div>
          <textarea
            placeholder="관리자 메모 (선택)"
            className="w-full border rounded-md p-2 mt-2"
            rows={3}
          />
        </div>

        <div className="text-right">
          <button
            onClick={onClose}
            className="mr-2 px-4 py-2 rounded-md border bg-gray-200"
          >
            닫기
          </button>
          <button
            className="px-4 py-2 rounded-md bg-blue-600 text-white"
          >
            조치하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReportDetailModal;