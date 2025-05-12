"use client";

import { useParams } from 'next/navigation';
import { useEffect, useState } from "react";
import { getInquiryDetail, InquiryDetailResponse } from '@/lib/api/inquiryDetail';  // API 함수 임포트
import "./inquiryDetailPage.css";

export default function InquiryDetailPage() {
  const params = useParams();
  const { id } = params;

  const [inquiry, setInquiry] = useState<InquiryDetailResponse | null>(null);

  useEffect(() => {
    const fetchInquiryDetail = async () => {
      try {
        if (id) {
          const data = await getInquiryDetail(Number(id));  // 백엔드에서 데이터 요청
          setInquiry(data);  // 받아온 데이터를 상태에 저장
        }
      } catch (error) {
        console.error("문의 상세 내용을 불러오는 데 실패했습니다:", error);
      }
    };

    fetchInquiryDetail();
  }, [id]);

  if (!inquiry) {
    return <div>로딩 중...</div>;  // 데이터가 로드될 때까지 로딩 표시
  }

  return (
    <div className="container">
      <h1 className="title">1:1 문의
        <span>상세내용</span>
      </h1>  
      <div className="inquiry-container">
        <div className="inquiry-title">
          <h1>문의내용</h1>
          <div className="inquiry-subTitle">
            <p>문의 유형: <span>{inquiry.category}</span></p>
            <p>
              상태:{" "}
              <span className={inquiry.status === "WAITING" ? "status-pending" : "status-completed"}>
                {inquiry.status === "WAITING" ? "답변전" : "답변완"}
              </span>
            </p>
          </div>
        </div>

        <div className="inquiry-details">
          <div>
            <h1>{inquiry.title}</h1>
            <p><strong>문의 날짜:</strong> {new Date(inquiry.createdAt).toLocaleDateString()}</p>
          </div>
          <p>{inquiry.content}</p>
        </div>

        {inquiry.answer ? (
          <>
            <label>답변내용</label>
            <div className="answer-contents">
              <p><strong>답변 날짜:</strong> {new Date(inquiry.answer.createdAt).toLocaleDateString()}</p>
              <p>{inquiry.answer.content}</p>
            </div>
          </>
        ) : (
          <p>답변이 아직 없습니다.</p> 
        )}
      </div>
    </div>
  );
}
