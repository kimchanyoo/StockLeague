"use client";

import { useEffect, useState } from "react";
import styles from "@/app/styles/components/ReportDetailModal.module.css"
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowDropUpIcon from "@mui/icons-material/ArrowDropUp";
import { ReportDetail, forceDeleteComment, deleteCommentWithWarning, banUser, rejectReport } from "@/lib/api/comment";
import { useAuth } from "@/context/AuthContext";

interface Props {
  open: boolean;
  onClose: () => void;
  report: ReportDetail | null;
}
type ActionType = "none" | "댓글삭제" | "경고부여" | "반려처리" | "계정정지";

const ReportDetailModal = ({ open, onClose, report }: Props) => {
  const { user } = useAuth();
  const [showReporters, setShowReporters] = useState(false);
  const [showWarnings, setShowWarnings] = useState(false);
  const [selectedAction, setSelectedAction] = useState<ActionType>("none");
  const [suspendDays, setSuspendDays] = useState<number>(1);
  const getAccountStatusText = (status: boolean) => (status ? "정지" : "활동 중");
  const [warningReason, setWarningReason] = useState<keyof typeof reasonTextMap>("OTHER");
  const [adminNickname, setAdminNickname] = useState<string | null>(null);
  const [actionTakenResult, setActionTakenResult] = useState<string | null>(null);
  
  const reasonTextMap: Record<string, string> = {
    INSULT: "욕설 및 비방",
    SPAM: "광고 / 도배성 내용",
    PERSONAL_INFORMATION: "개인정보 노출",
    SEXUAL: "선정적인 내용",
    OTHER: "기타",
  };
  const actionResultTextMap: Record<string, string> = {
    NO_ACTION: "조치 없음",
    COMMENT_DELETED: "댓글 삭제",
    WARNING: "경고",
    COMMENT_DELETED_AND_WARNING: "댓글 삭제 및 경고",
    USER_BANNED: "계정 정지",
    REJECTED: "반려 처리됨",
  };

  useEffect(() => {
    if (!open || !report) return;

    if (report.actionTaken) {
      setActionTakenResult(report.actionTaken);
      setAdminNickname(report.AdminNickname || null);
    } else {
      setActionTakenResult(null);
      setSelectedAction("none");
      setSuspendDays(1);
      setWarningReason("OTHER");
      setAdminNickname(null);
    }
  }, [open, report]);

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

  const handleSubmitAction = async () => {
    if (!report) return;

    try {
      let resultText = "";

      if (selectedAction === "댓글삭제") {
        await forceDeleteComment(report.commentId);
        resultText = "댓글 삭제";
      } else if (selectedAction === "경고부여") {
        await deleteCommentWithWarning(report.commentId, warningReason);
        resultText = "댓글 삭제 및 경고";
      } else if (selectedAction === "계정정지") {
        await banUser(report.commentAuthorId, "관리자 조치에 의한 계정 정지");
        resultText = "계정 정지";
      } else if (selectedAction === "반려처리") {
        await rejectReport(report.commentId)
        resultText = "반려 처리됨";
      } else {
        alert("조치 유형을 선택해주세요.");
        return;
      }

      // ✅ 조치 후 상태 업데이트
      setActionTakenResult(resultText);
      setAdminNickname(user?.nickname || "알 수 없음"); // 실제 닉네임 연결 필요

      alert(`${resultText}가 완료되었습니다.`);
    } catch (error) {
      console.error("조치 처리 중 오류 발생:", error);
      alert("조치 처리 중 오류가 발생했습니다.");
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
          <button
            onClick={() => setShowWarnings(!showWarnings)}
            className={styles.toggleButton}
          >
            경고 이력🚨
            {showWarnings ? <ArrowDropDownIcon /> : <ArrowDropUpIcon />}
          </button>

          {showWarnings && (
            <div className="warning-list" style={{ marginTop: "10px" }}>
              {report.warnings.length === 0 ? (
                <p>경고 이력이 없습니다.</p>
              ) : (
                <ul>
                  {report.warnings.map((w, i) => (
                    <li key={i} className={styles.reporterItem}>
                      <p><strong>일시:</strong> {new Date(w.warningAt).toLocaleString()}</p>
                      <p><strong>사유:</strong> {reasonTextMap[w.reason]}</p>
                      <p><strong>관련 댓글 ID:</strong> {w.commentId}</p>
                      <p><strong>조치 관리자:</strong> {w.adminNickname}</p>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>

        <div className={styles.textSection}>
          <button
            onClick={() => setShowReporters(!showReporters)}
            className={styles.toggleButton}
          >
            신고 내용🚨
            {showReporters ? <ArrowDropDownIcon /> : <ArrowDropUpIcon />}
          </button>
          {showReporters && (
            <ul className={styles.reporterList}>
              {report.reports.map((r, index) => (
                <li key={`${r.reporterNickname}-${r.reason}-${r.reportedAt}-${index}`} className={styles.reporterItem}>
                  <p><strong>신고자:</strong> {r.reporterNickname}</p>
                  <p><strong>사유:</strong> {reasonTextMap[r.reason]}</p>
                  {r.additionalInfo && <p><strong>내용:</strong> {r.additionalInfo}</p>}
                  <p><strong>신고일:</strong> {new Date(r.reportedAt).toLocaleString()}</p>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className={styles.adminActions}>
          <h3 className={styles.title}>관리자 조치</h3>

          {/* 조치 결과가 있으면 결과 메시지 출력 */}
          {actionTakenResult && actionTakenResult.trim() !== "" ? (
            <>
              <div style={{ fontWeight: "bold", fontSize: "1rem", color: "red" }}>
                조치 결과: {actionResultTextMap[actionTakenResult]} <br />
                담당 관리자: {adminNickname}
              </div>
              <button
                onClick={onClose}
                className={`${styles.button} ${styles.buttonClose}`}
              >
                닫기
              </button>
            </>
          ) : (
            // 조치 전 라디오 버튼 및 버튼 보여주기
            <>
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
              {selectedAction === "경고부여" && (
                <div style={{ marginTop: "10px" }}>
                  <label><strong>경고 사유 선택</strong></label>
                  <select
                    value={warningReason}
                    onChange={(e) => setWarningReason(e.target.value as keyof typeof reasonTextMap)}
                    style={{ marginLeft: "10px" }}
                  >
                    {Object.entries(reasonTextMap).map(([key, label]) => (
                      <option key={key} value={key}>
                        {label}
                      </option>
                    ))}
                  </select>
                </div>
              )}

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
                // 추후 정지일 입력 UI 추가 예정
                null
              )}

              <div className={styles.buttonGroup}>
                <button
                  onClick={onClose}
                  className={`${styles.button} ${styles.buttonClose}`}
                >
                  닫기
                </button>
                <button
                  className={`${styles.button} ${styles.buttonConfirm}`}
                  onClick={handleSubmitAction}
                >
                  조치하기
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ReportDetailModal;