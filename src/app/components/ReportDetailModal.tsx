"use client";

import { useState } from "react";
import styles from "@/app/styles/components/ReportDetailModal.module.css"
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowDropUpIcon from "@mui/icons-material/ArrowDropUp";
import { ReportDetail } from "@/lib/api/comment";

interface Props {
  open: boolean;
  onClose: () => void;
  report: ReportDetail | null;
}
type ActionType = "none" | "댓글삭제" | "경고부여" | "반려처리" | "계정정지";

const ReportDetailModal = ({ open, onClose, report }: Props) => {
  const [showReporters, setShowReporters] = useState(false);
  const [selectedAction, setSelectedAction] = useState<ActionType>("none");
  const [suspendDays, setSuspendDays] = useState<number>(1);
  const getAccountStatusText = (status: boolean) => (status ? "정지" : "활동 중");

  const reasonTextMap: Record<string, string> = {
    INSULT: "욕설 및 비방",
    SPAM: "광고 / 도배성 내용",
    PERSONAL_INFORMATION: "개인정보 노출",
    SEXUAL: "선정적인 내용",
    OTHER: "기타",
  };

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
          <p><strong>작성자:</strong> {report.commentAuthorNickname} (ID: {report.commentAuthorId})</p>
          <p><strong>작성일:</strong> {report.commentCreatedAt}</p>
          <p><strong>작성 커뮤니티:</strong> {report.stockName}</p>
          <p><strong>댓글 내용:</strong> {report.commentContent}</p>
        </div>

        <div className={styles.textSection}>
          <h3 className={styles.title}>사용자 정보</h3>
          <p><strong>닉네임:</strong> {report.commentAuthorNickname}</p>
          <p><strong>경고 횟수:</strong> {report.warningCount}</p>
          <p><strong>계정 상태:</strong> {getAccountStatusText(report.accountStatus)}</p>
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
              {report.reports.map((r, index) => (
                <li key={`${r.reporterNickname}-${r.reason}-${r.reportedAt}-${index}`} className={styles.reporterItem}>
                  <p><strong>신고자:</strong> {r.reporterNickname}</p>
                  <p><strong>사유:</strong> {reasonTextMap[r.reason]}</p>
                  {r.additionalInfo && <p><strong>내용:</strong> {r.additionalInfo}</p>}
                  <p><strong>신고일:</strong> {r.reportedAt}</p>
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