"use client";

import { useState, useEffect } from "react";
import ReportDetailModal from "@/app/components/ReportDetailModal";
import "./reports.css";
import { fetchReports, Report, fetchReportDetail, ReportDetail, ReportStatus} from "@/lib/api/comment"; 

const reportsPerPage = 10;
const maxPageButtons = 10;

const statuses: ReportStatus[] = [null, 'WAITING', 'RESOLVED'];

export default function Reports () {
  const [selectedReport, setSelectedReport] = useState<ReportDetail | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [statusFilter, setStatusFilter] = useState<ReportStatus>(null); 

  // 페이지네이션
  const [currentPage, setCurrentPage] = useState(1);
  const [totalCount, setTotalCount] = useState(10);
  const totalPages = Math.ceil(totalCount / reportsPerPage);
  const startPage = Math.floor((currentPage - 1) / maxPageButtons) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);
  const pageNumbers = Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);

  const toggleStatus = () => {
    const currentIndex = statuses.indexOf(statusFilter);
    const nextIndex = (currentIndex + 1) % statuses.length;
    setStatusFilter(statuses[nextIndex]);
    setCurrentPage(1);
  };

  const handlePageClick = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleClick = async (report: Report) => {
    try {
      const detail = await fetchReportDetail(report.commentId); // 상세 데이터 받아오기
      setSelectedReport(detail); // 상세 객체를 모달로 전달
      setModalOpen(true);
    } catch (err) {
      alert("신고 상세 정보를 불러오지 못했습니다.");
    }
  };

  const groupedReports = reports.reduce((acc, report) => {
    const existing = acc.find(r => r.commentId === report.commentId);
    if (existing) {
      existing.reportCount += 1;
    } else {
      acc.push({ ...report, reportCount: 1 });
    }
    return acc;
  }, [] as (Report & { reportCount: number })[]);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError("");
      try {
        const params: any = {
          page: currentPage,
          size: reportsPerPage,
        };
        if (statusFilter !== null) {
          params.status = statusFilter;
        }
        const res = await fetchReports(params.page, params.size, params.status);
        setReports(res.reports);
        setTotalCount(res.totalCount);
      } catch (err: any) {
        setError("신고 목록을 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [currentPage, statusFilter]);

  return (
    <div className="reports-container">
      <div className="reports-list">
        <h1>
          신고 목록
          <button onClick={toggleStatus}>
            상태: {statusFilter === null ? "전체" : statusFilter === "WAITING" ? "미처리" : "처리완료"}
          </button>
        </h1>
        {loading && <div>불러오는 중...</div>}
        {error && <div className="error">{error}</div>}

        {groupedReports.map((report, index) => (
          <div
            key={`${report.commentId}-${index}`}
            className={`reports-item ${report.status === 'RESOLVED' ? 'resolved' : ''}`}
            onClick={() => handleClick(report)}
          >
            <div>댓글 ID: {report.commentId}</div>
            <div>작성자: {report.authorNickname}</div>
            <div>신고 수: {report.reportCount}</div> 
            <div>경고: {report.warningCount}</div>
            <div>상태: {report.status === 'WAITING' ? '미처리' : '처리완료'}</div>
          </div>
        ))}
      </div>
      <ReportDetailModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        report={selectedReport}
      />

      <div className="pagination">
        <button onClick={() => handlePageClick(Math.max(currentPage - 1, 1))} disabled={currentPage === 1}>
          이전
        </button>

        {pageNumbers.map((num) => (
          <button
            key={num}
            className={num === currentPage ? "active" : ""}
            onClick={() => handlePageClick(num)}
          >
            {num}
          </button>
        ))}

        <button
          onClick={() => handlePageClick(Math.min(currentPage + 1, totalPages))}
          disabled={currentPage === totalPages}
        >
          다음
        </button>
      </div>
    </div>
  );
}