"use client";

import { useState } from "react";
import styles from "@/app/styles/components/ReportDetailModal.module.css"
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowDropUpIcon from "@mui/icons-material/ArrowDropUp";

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

type Props = {
  open: boolean;
  onClose: () => void;
  report: ReportDetail | null;
};
type ActionType = "none" | "댓글삭제" | "경고부여" | "반려처리" | "계정정지";

const ReportDetailModal = ({ open, onClose, report }: Props) => {
  const [showReporters, setShowReporters] = useState(false);
  const [selectedAction, setSelectedAction] = useState<ActionType>("none");
  const [suspendDays, setSuspendDays] = useState<number>(1);
  
  if (!open || !report) return null;

  const handleOverlayClick = (e: React.MouseEvent) => {
    if ((e.target as HTMLElement).id === "modal-overlay") {
      onClose();
    }
  };

  const handleActionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedAction(e.target.value as ActionType);
    if (e.target.value !== "계정정지") {
      setSuspendDays(1);
    }
  };

  return (
    <div
      id="modal-overlay"
      onClick={handleOverlayClick}
      className={styles.overlay}
    >
      <div
        className={styles.modalContent}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className={styles.title}>신고 상세</h2>

        <div className={styles.textSection}>
          <p><strong>댓글 ID:</strong> {report.commentId}</p>
          <p><strong>작성자:</strong> {report.commentAuthor} (ID: {report.commentAuthorId})</p>
          <p><strong>작성일:</strong> {report.commentCreatedAt}</p>
          <p><strong>작성 커뮤니티:</strong> {report.community}</p>
          <p><strong>댓글 내용:</strong> {report.commentContent}</p>
        </div>

        <div className={styles.textSection}>
          <h3 className={styles.title}>사용자 정보</h3>
          <p><strong>닉네임:</strong> {report.commentAuthor}</p>
          <p><strong>경고 횟수:</strong> {report.warnings}</p>
          <p><strong>계정 상태:</strong> {report.accountStatus}</p>
        </div>

        <div className={styles.textSection}>
          <button
            onClick={() => setShowReporters(!showReporters)}
            className={styles.toggleButton}
          >
            신고 내용
            {showReporters ? <ArrowDropDownIcon /> : <ArrowDropUpIcon />}
          </button>
          {showReporters && (
            <ul className={styles.reporterList}>
              {report.reporters.map((r) => (
                <li key={r.id} className={styles.reporterItem}>
                  <p><strong>신고자:</strong> {r.nickname}</p>
                  <p><strong>사유:</strong> {r.reason}</p>
                  {r.description && <p><strong>내용:</strong> {r.description}</p>}
                </li>
              ))}
            </ul>
          )}
        </div>

         <div className={styles.adminActions}>
          <h3 className={styles.title}>관리자 조치</h3>
          <label>
            <input
              type="radio"
              name="adminAction"
              value="댓글삭제"
              checked={selectedAction === "댓글삭제"}
              onChange={handleActionChange}
            />
            댓글 삭제
          </label>
          <label>
            <input
              type="radio"
              name="adminAction"
              value="경고부여"
              checked={selectedAction === "경고부여"}
              onChange={handleActionChange}
            />
            경고 부여
          </label>
          <label>
            <input
              type="radio"
              name="adminAction"
              value="반려처리"
              checked={selectedAction === "반려처리"}
              onChange={handleActionChange}
            />
            반려 처리
          </label>
          <label>
            <input
              type="radio"
              name="adminAction"
              value="계정정지"
              checked={selectedAction === "계정정지"}
              onChange={handleActionChange}
            />
            계정 정지
          </label>

          {selectedAction === "계정정지" && (
            <div className="mt-2">
              <label>
                정지 일수:
                <select
                  value={suspendDays}
                  onChange={(e) => setSuspendDays(Number(e.target.value))}
                  className="ml-2 rounded border px-2 py-1"
                >
                  {[1, 3, 7, 14, 30].map((day) => (
                    <option key={day} value={day}>
                      {day}일
                    </option>
                  ))}
                </select>
              </label>
            </div>
          )}
        </div>

        <div className={styles.buttonGroup}>
          <button
            onClick={onClose}
            className={`${styles.button} ${styles.buttonClose}`}
          >
            닫기
          </button>
          <button className={`${styles.button} ${styles.buttonConfirm}`}>
            조치하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReportDetailModal;